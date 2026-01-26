package com.example.glowpoint.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.glowpoint.data.models.api.NotificationRequest
import com.example.glowpoint.domain.model.result.TokenResult
import com.example.glowpoint.domain.repository.NotificationRepository
import com.example.glowpoint.domain.repository.TokenRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SenderNotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val tokenRepository: TokenRepository,
    private val notificationRepository: NotificationRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val bookingId = inputData.getString("BOOKING_ID") ?: return Result.failure()
        val shopId = inputData.getString("SHOP_ID") ?: return Result.failure()

        return try {
            when(val result = tokenRepository.getShopFcmToken(shopId)) {
                is TokenResult.Success -> {
                    val token = result.token
                    if (token.isBlank()) return Result.failure()

                    notificationRepository.sendNotification(
                        NotificationRequest(
                            token = token,
                            title = "New Order Received",
                            body = "You have a new order! Order ID: $bookingId",
                            bookingId = bookingId
                        )
                    )
                    Result.success()
                }

                is TokenResult.Failure -> {
                    return Result.failure()
                }
            }
        } catch (e: Exception) {
            Log.e("SenderNotificationWorker", "Error sending notification", e)
            Result.retry()
        }
    }
}
