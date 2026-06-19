package com.example.ui

import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class PandaNotificationListenerService : NotificationListenerService() {

    companion object {
        var instance: PandaNotificationListenerService? = null
            private set

        @Volatile
        private var _currentNotifications: List<NotificationInfo> = emptyList()

        val currentNotifications: List<NotificationInfo>
            get() = _currentNotifications
    }

    data class NotificationInfo(
        val packageName: String,
        val title: String,
        val text: String,
        val postTime: Long
    )

    override fun onListenerConnected() {
        instance = this
        refreshNotifications()
    }

    override fun onListenerDisconnected() {
        instance = null
        _currentNotifications = emptyList()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        refreshNotifications()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        refreshNotifications()
    }

    private fun refreshNotifications() {
        _currentNotifications = activeNotifications.map { it.toInfo() }
    }

    private fun StatusBarNotification.toInfo(): NotificationInfo {
        val extras = notification.extras
        val title = extras.getString(android.app.Notification.EXTRA_TITLE) ?: ""
        val text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            extras.getCharSequence(android.app.Notification.EXTRA_TEXT)?.toString() ?: ""
        } else {
            @Suppress("DEPRECATION")
            extras.getString(android.app.Notification.EXTRA_TEXT) ?: ""
        }
        return NotificationInfo(packageName, title, text, postTime)
    }
}
