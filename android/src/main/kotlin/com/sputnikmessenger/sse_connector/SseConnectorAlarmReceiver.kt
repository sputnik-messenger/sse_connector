package com.sputnikmessenger.sse_connector

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL


class SseConnectorAlarmReceiver : BroadcastReceiver() {
    companion object {
        var wakeLockTag: String? = null
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("sse_connector:alarm", "start")
        if (context != null) {
            if (wakeLockTag == null) {
                wakeLockTag = PrefsHelper.getWakeLockTag(PrefsHelper.getPrefs(context))!!
            }
            val wakeLock =
                    (context.getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                        newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, wakeLockTag).apply {
                            acquire(1000 * 60)
                        }
                    }

            if (SseConnectorThread.mutex.tryAcquire()) {
                SseAlarmReceiverThread(context, wakeLock).start()
            } else {
                Log.d("sse_connector:alarm", "mutex was locked")
            }

            SseConnectorPlugin.scheduleOneTimeJob(context, fallBackAlarmInMinutes = 30)
        }
    }
}

class SseAlarmReceiverThread(private val context: Context, private val wakeLock: PowerManager.WakeLock) :
        Thread() {

    override fun run() {
        try {
            val prefs = PrefsHelper.getPrefs(context);
            val urlString = PrefsHelper.getPollNotificationUrl(prefs)
            val pushKey = PrefsHelper.getPushKey(prefs)
            val lastPushKeyTs = PrefsHelper.getLastPushKeyTs(prefs)
            val url = URL("$urlString?token=$pushKey&since=$lastPushKeyTs")
            val connection = url.openConnection() as HttpURLConnection
            val json = InputStreamReader(connection.inputStream).readText()
            if (json.isNotBlank()) {
                NotificationHelper.show(context, json)
            }
        } catch (e: FileNotFoundException) {
            // is likely to happen on connectivity issues but there is no need to worry
        } catch (e: IOException) {
            // is likely to happen on connectivity issues but there is no need to worry
        } catch (e: Exception) {
            Log.e("sse_connector", "SSE Connection failed", e)
        } finally {
            SseConnectorThread.mutex.release()
            wakeLock.release()
        }
    }

}