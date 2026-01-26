package com.example.glowpoint.domain.repository

import com.example.glowpoint.data.models.api.NotificationRequest
import com.example.glowpoint.data.remote.api.NotificationApi
import okhttp3.ResponseBody
import retrofit2.Response
import javax.inject.Singleton

@Singleton
class NotificationRepository {

    suspend fun sendNotification(notificationRequest: NotificationRequest): Response<ResponseBody> = NotificationApi.api.sendNotification(notificationRequest)
}