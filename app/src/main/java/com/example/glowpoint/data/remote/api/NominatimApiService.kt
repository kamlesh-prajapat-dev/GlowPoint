package com.example.glowpoint.data.remote.api

import com.example.glowpoint.data.models.api.NominatimResponse
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query

private const val BASE_URL = "https://nominatim.openstreetmap.org/"

interface NominatimApiService {
    @GET("search")
    suspend fun searchAddress(
        @Query("addressdetails") addressDetails: Int = 1,
        @Query("q") query: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 1
    ): List<NominatimResponse>
}


private val json = Json {
    ignoreUnknownKeys = true // 🔥 VERY IMPORTANT
    isLenient = true
}

object NominatimApi {
    val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header(
                    "User-Agent",
                    "GlowPoint/1.0 (Android; contact: kamlesh.prajapat.dev@gmail.com)"
                )
                .header("Accept", "application/json")
                .build()
            chain.proceed(request)
        }
        .build()


    val api: NominatimApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(
                json.asConverterFactory("application/json".toMediaType())
            )
            .client(okHttpClient)
            .build()
            .create(NominatimApiService::class.java)
    }
}