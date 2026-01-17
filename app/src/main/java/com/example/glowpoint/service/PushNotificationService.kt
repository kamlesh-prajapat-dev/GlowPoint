package com.example.glowpoint.service

import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.glowpoint.util.NotificationHelper
import com.example.glowpoint.worker.FCMTokenWorker
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PushNotificationService : FirebaseMessagingService() {
    @Inject
    lateinit var notificationHelper: NotificationHelper

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.notification?.let {
            val orderId = remoteMessage.data["orderId"] ?: "Unknown Order"
            val body = it.body ?: "Order Status"
            val title = it.title ?: "Order Status"
            notificationHelper.showOrderStatusNotification(applicationContext, orderId, body, title)
        }
    }

    override fun onNewToken(token: String) {

        val request = OneTimeWorkRequestBuilder<FCMTokenWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setInputData(
                workDataOf(FCMTokenWorker.KEY_FCM_TOKEN to token)
            )
            .build()

        WorkManager.getInstance(applicationContext)
            .enqueueUniqueWork(
                "FCM_TOKEN_SYNC",
                ExistingWorkPolicy.REPLACE,
                request
            )
    }
}