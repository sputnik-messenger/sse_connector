package com.sputnikmessenger.sse_connector

import android.content.Context
import android.content.SharedPreferences

class PrefsHelper {
    companion object {
        private const val prefs_name: String = "com.sputnik-messenger.sse_connector"
        private const val key_wakeLockTag: String = "wakeLockTag"
        private const val key_sseNotificationsUrl: String = "sseNotificationsUrl"
        private const val key_pollllNotificationUrl: String = "pollNotificationUrl"
        private const val key_notificationChannelId: String = "notificationChannelId"
        private const val key_notificationSmallIcon: String = "notificationSmallIcon"
        private const val key_lastPushKeyTs: String = "lastPushKeyTs"
        private const val key_pushKey: String = "pushKey"
        private const val key_enabled: String = "enabled"


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

        fun setPollNotificationUrl(editor: SharedPreferences.Editor, pullNotificationsUrl: String) {
            editor.putString(key_pollllNotificationUrl, pullNotificationsUrl)
        }

        fun setNotificationChannelId(editor: SharedPreferences.Editor, notificationChannelId: String) {
            editor.putString(key_notificationChannelId, notificationChannelId)
        }


        fun setNotificationSmallIcon(editor: SharedPreferences.Editor, notificationSmallIcon: String) {
            editor.putString(key_notificationSmallIcon, notificationSmallIcon)
        }

        fun setLastPushKeyTs(editor: SharedPreferences.Editor, lastPushKeyTs: Int) {
            editor.putInt(key_lastPushKeyTs, lastPushKeyTs)
        }

        fun setPushKey(editor: SharedPreferences.Editor, pushKey: String) {
            editor.putString(key_pushKey, pushKey)
        }

        fun setEnabled(editor: SharedPreferences.Editor, enabled: Boolean) {
            editor.putBoolean(key_enabled, enabled);
        }


        fun getWakeLockTag(prefs: SharedPreferences): String? {
            return prefs.getString(key_wakeLockTag, null)
        }

        fun getSseNotificationsUrl(prefs: SharedPreferences): String? {
            return prefs.getString(key_sseNotificationsUrl, null)
        }

        fun getPollNotificationUrl(prefs: SharedPreferences): String? {
            return prefs.getString(key_pollllNotificationUrl, null)
        }

        fun getNotificationChannelId(prefs: SharedPreferences): String? {
            return prefs.getString(key_notificationChannelId, null)
        }

        fun getNotificationSmallIcon(prefs: SharedPreferences): String? {
            return prefs.getString(key_notificationSmallIcon, null)
        }

        fun getLastPushKeyTs(prefs: SharedPreferences): Int {
            return prefs.getInt(key_lastPushKeyTs, 0)
        }

        fun getPushKey(prefs: SharedPreferences): String? {
            return prefs.getString(key_pushKey, null)
        }

        fun getEnabled(prefs: SharedPreferences): Boolean {
            return prefs.getBoolean(key_enabled, false)
        }


    }
}