package com.sputnikmessenger.sse_connector

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.util.Log
import okhttp3.*
import okhttp3.internal.wait
import okio.ByteString
import java.io.*
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
            .pingInterval(15, TimeUnit.MINUTES)
            .retryOnConnectionFailure(false)
            .build()


    companion object {
        @Volatile
        var mutex = Semaphore(1);
    }

    override fun run() {
        Log.d("sse_connector:sseThread", "start")
        val prefs = PrefsHelper.getPrefs(context);
        val urlString = PrefsHelper.getSseNotificationsUrl(prefs)
        val pushKey = PrefsHelper.getPushKey(prefs)
        val lastPushKeyTs = PrefsHelper.getLastPushKeyTs(prefs)
        val url = URL("$urlString?token=$pushKey&since=$lastPushKeyTs")

        val wakeMe = WakeMeThread(this, 1000 * 60 * 120);
        try {
            val request = Request.Builder().url(url).build();

            val socket = okHttpClient.newWebSocket(request, SseWebSocket(context, wakeMe))
            Log.d("sse_connector", "executing request")

            wakeMe.start()

            while (okHttpClient.dispatcher.runningCallsCount() > 0) {
                okHttpClient.dispatcher.executorService.awaitTermination(20, TimeUnit.MINUTES)
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
        Log.d("sse_connector:sseThread", "end")
    }


}

class WakeMeThread(private val other: Thread, private val inMillis: Long) : Thread() {

    @Volatile
    var resetFlag = false

    @Volatile
    var cancelFlag = false

    override fun run() {

        do {
            resetFlag = false
            try {
                Log.d("sse_connector", "wake me goes to sleep")
                sleep(inMillis)
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
        } while (!cancelFlag && resetFlag)
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

class SseWebSocket(val context: Context, val wakeMe: WakeMeThread) : WebSocketListener() {

    override fun onOpen(webSocket: WebSocket, response: Response) {
        Log.d("sse_connector", "onOpen")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        Log.d("sse_connector", "onMessage")
        wakeMe.resetTimer()
        if (text.isNotBlank()) {
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
        wakeMe.cancel()
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Log.d("sse_connector", "onFailure", t)
        wakeMe.wakeNow()
        webSocket.close(1000, t.message)
        webSocket.cancel()
    }


}
