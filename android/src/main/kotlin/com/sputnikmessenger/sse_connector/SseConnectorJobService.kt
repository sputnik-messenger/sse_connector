package com.sputnikmessenger.sse_connector

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.util.Log
import java.io.BufferedReader
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
        if (SseConnectorThread.mutex.tryAcquire()) {
            SseConnectorThread(this).start()
        }
        return false
    }
}

class SseConnectorThread(private val context: Context) : Thread() {

    companion object {
        val mutex = Semaphore(1)
    }

    override fun run() {

        try {
            val urlString = PrefsHelper.getSseNotificationsUrl(PrefsHelper.getPrefs(context))
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.readTimeout = 0
            connection.setRequestProperty("Accept-Encoding", "identity")

            val input = BufferedReader(InputStreamReader(connection.inputStream))
            try {
                while (true) {
                    val line = input.readLine()
                    NotificationHelper.show(context, line)
                }
            } catch (e: IOException) {
                // is likely to happen frequently but there is no need to worry
            } catch (e: Exception) {
                Log.e("sse_connector", "SSE Connection failed", e)
            }

            SseConnectorPlugin.scheduleOneTimeJob(context)
        } finally {
            mutex.release()
        }
    }


}