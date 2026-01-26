package com.example.glowpoint.data.remote.api

import com.example.glowpoint.data.models.api.NotificationRequest
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.POST

private const val BASE_URL = "https://riverine-danna-uncannily.ngrok-free.dev/api/notifications/"

interface NotificationApiService {
    @POST("send")
    suspend fun sendNotification(@Body notificationRequest: NotificationRequest): Response<ResponseBody>
}

object NotificationApi {
    val api: NotificationApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(
                Json.asConverterFactory("application/json".toMediaType())
            )
            .build()
            .create(NotificationApiService::class.java)
    }
}