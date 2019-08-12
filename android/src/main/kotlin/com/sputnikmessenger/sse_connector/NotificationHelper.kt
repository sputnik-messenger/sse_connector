package com.sputnikmessenger.sse_connector

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import org.json.JSONException
import org.json.JSONObject
import java.lang.Exception


class NotificationHelper {

    companion object {
        fun show(context: Context, json: String) {
            try {

                val parsed = JSONObject(json)

                val hasEventId = parsed.getJSONObject("notification").getString("event_id").isNotBlank()


                val timestamp = try {
                    parsed.getInt("timestamp")
                } catch (e: Exception) {
                    0
                }

                if (timestamp > 0) {
                    val prefs = PrefsHelper.getPrefs(context)
                    val editor = prefs.edit();
                    PrefsHelper.setLastPushKeyTs(editor, timestamp)
                    editor.apply()
                }


                val unread = try {
                    parsed.getJSONObject("notification").getJSONObject("counts").getInt("unread")
                } catch (e: Exception) {
                    0
                }

                val content = "Open to read"

                val title = if (unread > 1) {
                    "$unread rooms with new messages" //todo: unread count is not reliable
                } else {
                    "New message"
                }


                val prio = NotificationCompat.PRIORITY_MAX


                if (hasEventId) {
                    showNotification(context, 0, title, content, prio, false)
                } else if (unread > 0) {
                    showNotification(context, 0, title, content, prio, true)
                } else {
                    cancelNotification(context, 0)
                }

            } catch (e: Throwable) {
                Log.e("JSON Parser", "Error parsing data $e")
            }
        }

        private fun showNotification(context: Context,
                                     id: Int,
                                     title: String,
                                     contentText: String,
                                     priority: Int,
                                     alertOnce: Boolean
        ) {

            val prefs = PrefsHelper.getPrefs(context)

            val notificationsChannelId = PrefsHelper.getNotificationChannelId(prefs)!!
            val smallIcon = PrefsHelper.getNotificationSmallIcon(prefs)!!
            //todo: how to set the icon?!


            val pm = context.packageManager
            var intent = pm.getLaunchIntentForPackage(context.packageName)!!;

            val className = intent.component!!.className
            try {
                val clazz = Class.forName(className)
                intent = Intent(context, clazz)
            } catch (e: ClassNotFoundException) {
                Log.e("sse_connector", "couldn't find class for $className", e)
            }
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT


            val pendingIntent = PendingIntent.getActivity(context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT)

            val notification = NotificationCompat.Builder(context, notificationsChannelId)
                    .setSmallIcon(R.drawable.notification_tile_bg)
                    .setContentTitle(title)
                    .setContentText(contentText)
                    .setPriority(priority)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setOnlyAlertOnce(alertOnce)


            with(NotificationManagerCompat.from(context)) {
                notify(id, notification.build())
            }
        }


        private fun cancelNotification(context: Context,
                                       id: Int
        ) {
            with(NotificationManagerCompat.from(context)) {
                cancel(id)
            }
        }
    }



}