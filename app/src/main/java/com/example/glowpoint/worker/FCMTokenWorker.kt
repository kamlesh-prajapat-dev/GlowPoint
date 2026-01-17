package com.example.glowpoint.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.glowpoint.domain.usecase.TokenUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class FCMTokenWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val tokenUseCase: TokenUseCase
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val token = inputData.getString(KEY_FCM_TOKEN)
            ?: return Result.failure()

        return try {
            tokenUseCase.saveNewToken(token)
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving FCM token", e)

            // Guard against infinite retries
            if (runAttemptCount >= 3) {
                Result.failure()
            } else {
                Result.retry()
            }
        }
    }

    companion object {
        const val KEY_FCM_TOKEN = "FCM_TOKEN"
        private const val TAG = "FCMTokenWorker"
    }
}