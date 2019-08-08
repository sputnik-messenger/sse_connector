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
        fun show(context: Context, line: String) {
            println("received line $line")
            try {
                val json = line.substringAfter(":")
                println("json $json")
                val parsed = JSONObject(json)


                if (parsed.getJSONObject("notification").getString("event_id").isNotBlank()) {

                    val senderName = try {
                        val displayName = parsed.getJSONObject("notification").getString("sender_display_name")
                        if (displayName.isBlank()) {
                            "New message"
                        } else {
                            displayName
                        }
                    } catch (e: Exception) {
                        "New message"
                    }

                    val content = try {
                        val body = parsed.getJSONObject("notification").getJSONObject("content").getString("body")
                        if (body.isBlank()) {
                            "Open to read"
                        } else {
                            body
                        }
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
                }
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
                    .setAutoCancel(false)
                    .setContentIntent(pendingIntent)


            with(NotificationManagerCompat.from(context)) {
                notify(id, notification.build())
            }
        }
    }


}