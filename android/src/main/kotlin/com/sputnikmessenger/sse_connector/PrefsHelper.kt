package com.sputnikmessenger.sse_connector

import android.content.Context
import android.content.SharedPreferences

class PrefsHelper {
    companion object {
        private const val prefs_name: String = "com.sputnik-messenger.sse_connector"
        private const val key_wakeLockTag: String = "wakeLockTag"
        private const val key_sseNotificationsUrl: String = "sseNotificationsUrl"
        private const val key_pullNotificationUrl: String = "pullNotificationUrl"
        private const val key_notificationChannelId: String = "notificationChannelId"
        private const val key_notificationSmallIcon: String = "notificationSmallIcon"


        fun getPrefs(context: Context): SharedPreferences {
            return context.getSharedPreferences(
                    prefs_name,
                    Context.MODE_PRIVATE)
        }

        fun setWakeLockTag(editor: SharedPreferences.Editor, wakeLockTag: String) {
            editor.putString(key_wakeLockTag, wakeLockTag)
        }

        fun setSseNotificationsUrl(editor: SharedPreferences.Editor, sseNotificationsUrl: String) {
            editor.putString(key_sseNotificationsUrl, sseNotificationsUrl)
        }

        fun setPullNotificationUrl(editor: SharedPreferences.Editor, pullNotificationsUrl: String) {
            editor.putString(key_pullNotificationUrl, pullNotificationsUrl)
        }

        fun setNotificationChannelId(editor: SharedPreferences.Editor, notificationChannelId: String) {
            editor.putString(key_notificationChannelId, notificationChannelId)
        }


        fun setNotificationSmallIcon(editor: SharedPreferences.Editor, notificationSmallIcon: String) {
            editor.putString(key_notificationSmallIcon, notificationSmallIcon)
        }


        fun getWakeLockTag(prefs: SharedPreferences): String? {
            return prefs.getString(key_wakeLockTag, null)
        }

        fun getSseNotificationsUrl(prefs: SharedPreferences): String? {
            return prefs.getString(key_sseNotificationsUrl, null)
        }

        fun getPullNotificationUrl(prefs: SharedPreferences): String? {
            return prefs.getString(key_pullNotificationUrl, null)
        }

        fun getNotificationChannelId(prefs: SharedPreferences): String? {
            return prefs.getString(key_notificationChannelId, null)
        }

        fun getNotificationSmallIcon(prefs: SharedPreferences): String? {
            return prefs.getString(key_notificationSmallIcon, null)
        }


    }
}