package com.sputnikmessenger.sse_connector

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobInfo
import android.app.job.JobInfo.Builder
import android.app.job.JobInfo.NETWORK_TYPE_ANY
import android.app.job.JobScheduler
import android.content.Context
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar
import android.content.ComponentName
import android.content.Intent
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import android.os.Parcel
import android.os.SystemClock


class SseConnectorPlugin(private var context: Context) : MethodCallHandler {

    companion object {
        private const val jobId0 = 6000
        private const val jobId1 = 6001
        private const val alarmId0 = 6000

        @JvmStatic
        fun registerWith(registrar: Registrar) {
            val channel = MethodChannel(registrar.messenger(), "com.sputnik-messenger.sse_connector")
            channel.setMethodCallHandler(SseConnectorPlugin(registrar.context()))
        }

        fun scheduleOneTimeJob(context: Context, fallBackAlarmInMinutes: Int?) {
            val builder = Builder(jobId0, ComponentName(context, SseConnectorJobService::class.java))
            configureJobInfo(builder)
            builder.setMinimumLatency(1000 * 10)
            scheduleJob(context, builder.build(), fallBackAlarmInMinutes)
        }

        fun schedulePeriodicJob(context: Context, fallBackAlarmInMinutes: Int?) {
            val builder = Builder(jobId1, ComponentName(context, SseConnectorJobService::class.java))
            configureJobInfo(builder)

            builder.setPeriodic(1000 * 60 * 15)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder.setRequiresBatteryNotLow(true)
                        .setPeriodic(1000 * 60 * 15, 1000 * 60 * 15)
            }

            scheduleJob(context, builder.build(), fallBackAlarmInMinutes)
        }

        private fun scheduleJob(context: Context, info: JobInfo, fallBackAlarmInMinutes: Int?) {
            val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            jobScheduler.schedule(info)
            if (fallBackAlarmInMinutes != null) {
                scheduleAlarm(context, fallBackAlarmInMinutes)
            }
        }

        private fun scheduleAlarm(context: Context, inMinutes: Int) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val alarmIntent = Intent(context, SseConnectorAlarmReceiver::class.java).let { intent ->
                val wakeLockTag = PrefsHelper.getWakeLockTag(PrefsHelper.getPrefs(context))!!
                intent.putExtra("wakeLockTag", wakeLockTag)
                PendingIntent.getBroadcast(context, alarmId0, intent, 0)
            }


            val alarmTime = SystemClock.elapsedRealtime() + 1000 * 60 * inMinutes
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(
                        AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        alarmTime,
                        alarmIntent)
            } else {
                alarmManager.set(
                        AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        alarmTime,
                        alarmIntent)
            }
        }

        private fun configureJobInfo(infoBuilder: Builder) {
            infoBuilder.setRequiredNetworkType(NETWORK_TYPE_ANY)
                    .setPersisted(true)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                infoBuilder.setEstimatedNetworkBytes(500, 0)
                        .setRequiredNetwork(NetworkRequest.Builder()
                                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build())
            }
        }
    }


    override fun onMethodCall(call: MethodCall, result: Result) {
        if (call.method == "initSseConnection") {
            val wakeLockTag = call.argument<String>("wakeLockTag")
            val pushKey = call.argument<String>("pushKey")
            val sseNotificationsUrl = call.argument<String>("sseNotificationsUrl")
            val pollNotificationUrl = call.argument<String>("pollNotificationUrl")
            val notificationChannelId = call.argument<String>("notificationChannelId")
            val notificationChannelName = call.argument<String>("notificationChannelName")
            val notificationChannelDescription = call.argument<String>("notificationChannelDescription")
            val notificationChannelImportance = call.argument<Int>("notificationChannelImportance")
            val notificationSmallIcon = call.argument<String>("notificationSmallIcon")

            val editor = PrefsHelper.getPrefs(context).edit()
            PrefsHelper.setWakeLockTag(editor, wakeLockTag!!)
            PrefsHelper.setNotificationChannelId(editor, notificationChannelId!!)
            PrefsHelper.setSseNotificationsUrl(editor, sseNotificationsUrl!!)
            PrefsHelper.setPollNotificationUrl(editor, pollNotificationUrl!!)
            PrefsHelper.setNotificationSmallIcon(editor, notificationSmallIcon!!)
            PrefsHelper.setPushKey(editor, pushKey!!)
            editor.apply()


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationChannel = NotificationChannel(
                        notificationChannelId,
                        notificationChannelName,
                        notificationChannelImportance!!).apply {
                    description = notificationChannelDescription
                }
                val notificationManager: NotificationManager =
                        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(notificationChannel)
            }

            scheduleOneTimeJob(context, null)
            schedulePeriodicJob(context, fallBackAlarmInMinutes = 15)
        } else {
            result.notImplemented()
        }
    }
}
