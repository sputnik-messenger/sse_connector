package com.sputnikmessenger.sse_connector

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.util.Log
import okhttp3.*
import okio.ByteString
import java.lang.Exception
import java.net.URL
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit


class SseConnectorJobService : JobService() {
    override fun onStopJob(params: JobParameters?): Boolean {
        return false
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        Log.d("sse_connector:job", "start")
        if (SseConnectorThread.mutex.tryAcquire()) {
            SseConnectorThread(this).start()
        } else {
            Log.d("sse_connector:job", "mutex was locked, going back to sleep")
        }
        return false
    }
}

class SseConnectorThread(private val context: Context) : Thread() {

    init {
        setUncaughtExceptionHandler { _: Thread, e: Throwable ->
            Log.e("sse_connector:sseThread", "uncaught exception", e)
            mutex.release()
        }
    }

    private val okHttpClient = OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .cache(null)
            .pingInterval(2, TimeUnit.MINUTES)
            .retryOnConnectionFailure(false)
            .build()


    companion object {
        @Volatile
        var mutex = Semaphore(1);

        @Volatile
        var restart = false;
    }


    override fun run() {
        restart = false;
        Log.d("sse_connector:sseThread", "start")
        val prefs = PrefsHelper.getPrefs(context)
        val enabled = PrefsHelper.getEnabled(prefs)

        val urlString = PrefsHelper.getSseNotificationsUrl(prefs)
        val pushKey = PrefsHelper.getPushKey(prefs)
        val lastPushKeyTs = PrefsHelper.getLastPushKeyTs(prefs)
        val url = URL("$urlString?token=$pushKey&since=$lastPushKeyTs")

        if (enabled) {
            val wakeMe = WakeMeThread(this, 1000 * 60 * 120);
            try {
                val request = Request.Builder().url(url).build();

                val socket = okHttpClient.newWebSocket(request, SseWebSocket(context, wakeMe))
                Log.d("sse_connector", "executing request")

                wakeMe.start()

                while (!restart && okHttpClient.dispatcher.runningCallsCount() > 0) {
                    okHttpClient.dispatcher.executorService.awaitTermination(10, TimeUnit.MINUTES)
                }

            } catch (e: Exception) {
                Log.d("sse_connector", "SSE Connection failed", e)
            } finally {
                Log.d("sse_connector", "finally")
                mutex.release()
                wakeMe.cancel()
                okHttpClient.dispatcher.cancelAll()
                okHttpClient.dispatcher.executorService.shutdownNow()
            }
            SseConnectorPlugin.scheduleOneTimeJob(context, fallBackAlarmInMinutes = 15)
        } else {
            Log.d("sse_connector", "disabled ... shutting down")
            okHttpClient.dispatcher.cancelAll()
            okHttpClient.dispatcher.executorService.shutdownNow()
            mutex.release()
        }
        Log.d("sse_connector:sseThread", "end")
    }


}

class WakeMeThread(private val other: Thread, private val inMillis: Long) : Thread() {

    companion object {
        val globalWakeTrigger = Semaphore(1)
    }

    @Volatile
    var resetFlag = false

    @Volatile
    var cancelFlag = false

    override fun run() {
        globalWakeTrigger.tryAcquire()

        var globalWakeTriggered = false;
        do {
            resetFlag = false
            try {
                Log.d("sse_connector", "wake me goes to sleep")
                globalWakeTriggered = globalWakeTrigger.tryAcquire(inMillis, TimeUnit.MILLISECONDS)
                if (globalWakeTriggered) {
                    Log.d("sse_connector", "wake me got global wake trigger")
                }
            } catch (e: InterruptedException) {
                if (resetFlag) {
                    Log.d("sse_connector", "wake me got reset")
                } else if (cancelFlag) {
                    Log.d("sse_connector", "wake me got cancelled")
                } else {
                    Log.d("sse_connector", "wake me got interrupted")

                }
                isInterrupted // clear interrupt
            }
        } while (!globalWakeTriggered && !cancelFlag && resetFlag)
        if (!cancelFlag) {
            Log.d("sse_connector", "wake other")
            other.interrupt()
        }
    }

    fun resetTimer() {
        Log.d("sse_connector", "reset wake me")
        resetFlag = true
        interrupt()
    }

    fun cancel() {
        Log.d("sse_connector", "cancel wake me")
        cancelFlag = true
        interrupt()
    }

    fun wakeNow() {
        interrupt()
    }

}

class SseWebSocket(private val context: Context, private val wakeMe: WakeMeThread) : WebSocketListener() {

    private val prefs = PrefsHelper.getPrefs(context);

    override fun onOpen(webSocket: WebSocket, response: Response) {
        Log.d("sse_connector", "onOpen")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        Log.d("sse_connector", "onMessage")
        wakeMe.resetTimer()
        val enabled = PrefsHelper.getEnabled(prefs)
        if (enabled && text.isNotBlank()) {
            NotificationHelper.show(context, text)
        }
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        Log.d("sse_connector", "onMessage")
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        Log.d("sse_connector", "onClosing")
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        Log.d("sse_connector", "onClosed")
        SseConnectorThread.restart = true;
        wakeMe.wakeNow()
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Log.d("sse_connector", "onFailure", t)
        SseConnectorThread.restart = true;
        wakeMe.wakeNow()
    }


}
