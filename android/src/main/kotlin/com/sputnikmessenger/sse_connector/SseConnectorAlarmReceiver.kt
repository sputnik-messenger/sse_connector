package com.sputnikmessenger.sse_connector

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager


class SseConnectorAlarmReceiver : BroadcastReceiver() {
    companion object {
        var wakeLockTag: String? = null
    }

    override fun onReceive(context: Context?, intent: Intent?) {
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
            }

            SseConnectorPlugin.scheduleOneTimeJob(context)
        }
    }
}

class SseAlarmReceiverThread(private val context: Context, private val wakeLock: PowerManager.WakeLock) :
        Thread() {

    override fun run() {
        println("${currentThread()} run")

        try {
            sleep(5000)
            // todo: poll update
            // todo: show notification if one exists
        } finally {
            SseConnectorThread.mutex.release()
            wakeLock.release()
        }
    }

}