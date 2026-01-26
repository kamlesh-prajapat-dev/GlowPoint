package com.example.glowpoint.data.remote.api

import com.example.glowpoint.data.models.api.PhotonResponse
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query

private const val BASE_URL = "https://photon.komoot.io/"

interface PhotonApiService {
    @GET("api/")
    suspend fun searchPlaces(
        @Query("q") query: String,
        @Query("limit") limit: Int = 100
    ): PhotonResponse
}

private val json = Json {
    ignoreUnknownKeys = true //  VERY IMPORTANT
    isLenient = true
}


object PhotonApi {
    val api: PhotonApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(
                json.asConverterFactory("application/json".toMediaType())
            )
            .build()
            .create(PhotonApiService::class.java)
    }
}