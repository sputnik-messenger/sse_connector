package com.sputnikmessenger.sse_connector

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Semaphore


class SseConnectorJobService : JobService() {
    override fun onStopJob(params: JobParameters?): Boolean {
        return false
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        Log.d("sse_connector:job", "start")

        if (SseConnectorThread.mutex.tryAcquire()) {
            SseConnectorThread(this).start()
        } else {
            Log.d("sse_connector:job", "mutex was locked")
        }
        return false
    }
}

class SseConnectorThread(private val context: Context) : Thread() {

    companion object {
        val mutex = Semaphore(1)
    }

    override fun run() {
        Log.d("sse_connector:sseThread", "start")
        try {
            val prefs = PrefsHelper.getPrefs(context);
            val urlString = PrefsHelper.getSseNotificationsUrl(prefs)
            val pushKey = PrefsHelper.getPushKey(prefs)
            val url = URL("$urlString?token=$pushKey")

            val connection = url.openConnection() as HttpURLConnection
            connection.readTimeout = 0
            connection.setRequestProperty("Accept-Encoding", "identity")

            val input = BufferedReader(InputStreamReader(connection.inputStream))
            do {
                val line = input.readLine()
                if (line != null) {
                    NotificationHelper.show(context, line.substringAfter(":"))
                }
            } while (line != null)

        } catch (e: FileNotFoundException) {
            // is likely to happen on connectivity issues but there is no need to worry
        } catch (e: IOException) {
            // is likely to happen on connectivity issues but there is no need to worry
        } catch (e: Exception) {
            Log.e("sse_connector", "SSE Connection failed", e)
        } finally {
            mutex.release()
        }
        SseConnectorPlugin.scheduleOneTimeJob(context, fallBackAlarmInMinutes = 15)
        Log.d("sse_connector:sseThread", "end")
    }


}
