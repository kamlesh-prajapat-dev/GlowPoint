package com.example.glowpoint.workerscheduler

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.glowpoint.worker.SenderNotificationWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SenderNotificationWorkerScheduler @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    fun retryNotification(bookingId: String, shopId: String) {

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<SenderNotificationWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                1,
                TimeUnit.MINUTES
            )
            .setInputData(
                workDataOf(
                    "BOOKING_ID" to bookingId,
                    "SHOP_ID" to shopId
                )
            )
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                "ORDER_NOTIFY_$bookingId",
                ExistingWorkPolicy.KEEP,
                request
            )
    }
}