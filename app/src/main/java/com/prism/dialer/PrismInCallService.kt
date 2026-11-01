package com.prism.dialer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.InCallService
import androidx.core.app.NotificationCompat

class PrismInCallService : InCallService() {
    override fun onCreate() {
        super.onCreate()
        activeService = this
        createCallChannel()
    }

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        activeCall = call
        call.registerCallback(callback)
        showCallNotification(call)
    }

    override fun onCallRemoved(call: Call) {
        call.unregisterCallback(callback)
        if (activeCall == call) activeCall = null
        getSystemService(NotificationManager::class.java).cancel(NOTIFICATION_ID)
        super.onCallRemoved(call)
    }

    private val callback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            showCallNotification(call)
            CallActivity.refreshFromService()
        }
        override fun onDetailsChanged(call: Call, details: Call.Details) {
            CallActivity.refreshFromService()
        }
    }

    private fun createCallChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Calls", NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Incoming and ongoing call controls"
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun showCallNotification(call: Call) {
        val intent = Intent(this, CallActivity::class.java).addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        )
        val pending = PendingIntent.getActivity(
            this, 100, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val incoming = call.state == Call.STATE_RINGING
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.sym_action_call)
            .setContentTitle(if (incoming) "Incoming call" else "Call in progress")
            .setContentText("Prism Dialer")
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(true)
            .setFullScreenIntent(pending, incoming)
            .setContentIntent(pending)
            .build()
        getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val CHANNEL_ID = "prism_calls"
        const val NOTIFICATION_ID = 42
        @Volatile var activeService: PrismInCallService? = null
            private set
        @Volatile var activeCall: Call? = null
            private set

        fun answer() { activeCall?.answer(android.telecom.VideoProfile.STATE_AUDIO_ONLY) }
        fun reject() { activeCall?.reject(false, null) }
        fun disconnect() { activeCall?.disconnect() }
        fun setMuted(muted: Boolean) { activeService?.setMuted(muted) }
        fun setSpeaker(enabled: Boolean) {
            activeService?.setAudioRoute(
                if (enabled) CallAudioState.ROUTE_SPEAKER else CallAudioState.ROUTE_EARPIECE
            )
        }
    }
}
