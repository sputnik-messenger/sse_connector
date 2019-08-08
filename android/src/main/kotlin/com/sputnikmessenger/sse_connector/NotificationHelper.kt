package com.sputnikmessenger.sse_connector

import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import org.json.JSONException
import org.json.JSONObject
import java.lang.Exception
import android.content.res.AssetFileDescriptor
import android.content.res.AssetManager




class NotificationHelper {

    companion object {
        fun show(context: Context, line: String) {
            try {
                val json = line.substringAfter(":")
                val parsed = JSONObject(json)

                val senderName = try {
                    parsed.getJSONObject("notification").getString("sender_display_name")
                } catch (e: Exception) {
                    "New message"
                }

                val content = try {
                    parsed.getJSONObject("notification").getJSONObject("content").getString("body")
                } catch (e: Exception) {
                    "Open to read"
                }

                val matrixPrio = try {
                    parsed.getJSONObject("notification").getString("prio")
                } catch (e: Exception) {
                    "high"
                }

                val droidPrio = if (matrixPrio == "low") NotificationCompat.PRIORITY_LOW else NotificationCompat.PRIORITY_HIGH

                showNotification(context, 0, senderName, content, droidPrio)
            } catch (e: JSONException) {
                Log.e("JSON Parser", "Error parsing data $e")
            }
        }

        private fun showNotification(context: Context,
                                     id: Int,
                                     title: String,
                                     contentText: String,
                                     priority: Int
        ) {

            val prefs = PrefsHelper.getPrefs(context)

            val notificationsChannelId = PrefsHelper.getNotificationChannelId(prefs)!!
            val smallIcon = PrefsHelper.getNotificationSmallIcon(prefs)!!
            //todo: how to set the icon?!


            val notification = NotificationCompat.Builder(context, notificationsChannelId)
                    .setSmallIcon(R.drawable.notification_tile_bg)
                    .setContentTitle(title)
                    .setContentText(contentText)
                    .setPriority(priority)

            with(NotificationManagerCompat.from(context)) {
                notify(id, notification.build())
            }
        }
    }


}