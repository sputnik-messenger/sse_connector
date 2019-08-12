package com.sputnikmessenger.sse_connector

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
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

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("sse_connector:alarm", "start")
        if (context != null && intent != null) {
            val wakeLockTag = intent.getStringExtra("wakeLockTag")

            val wakeLock =
                    (context.getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                        newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, wakeLockTag).apply {
                            acquire(1000 * 60)
                        }
                    }
            Log.d("sse_connector:alarm", "got wakeLock")
            SseConnectorPlugin.scheduleOneTimeJob(context, fallBackAlarmInMinutes = 30)


            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
            val isConnected: Boolean = activeNetwork?.isConnected == true

            if (isConnected && SseConnectorThread.mutex.tryAcquire()) {
                SseAlarmReceiverThread(context, wakeLock).start()
            } else {
                Log.d("sse_connector:alarm", "mutex was locked")
                Log.d("sse_connector:alarm", "release wakeLock")
                wakeLock.release()
            }


        }
        Log.d("sse_connector:alarm", "end")
    }
}

class SseAlarmReceiverThread(private val context: Context, private val wakeLock: PowerManager.WakeLock) :
        Thread() {

    override fun run() {
        Log.d("sse_connector:alarmTh", "start")
        try {
            val prefs = PrefsHelper.getPrefs(context);
            val urlString = PrefsHelper.getPollNotificationUrl(prefs)
            val pushKey = PrefsHelper.getPushKey(prefs)
            val lastPushKeyTs = PrefsHelper.getLastPushKeyTs(prefs)
            val url = URL("$urlString?token=$pushKey&since=$lastPushKeyTs")
            val connection = url.openConnection() as HttpURLConnection
            val json = InputStreamReader(connection.inputStream).readText()
            Log.d("sse_connector:alarmTh", "pulled event")
            if (json.isNotBlank()) {
                Log.d("sse_connector:alarmTh", "showing notification")
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
        Log.d("sse_connector:alarmTh", "end")
    }

}
