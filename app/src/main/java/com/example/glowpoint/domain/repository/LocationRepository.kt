package com.example.glowpoint.domain.repository

import com.example.glowpoint.data.remote.api.NominatimApi
import com.example.glowpoint.data.remote.api.PhotonApi
import javax.inject.Singleton

@Singleton
class LocationRepository {
    suspend fun searchAddress(query: String) = NominatimApi.api.searchAddress(query = query)

    suspend fun searchPlaces(query: String) = PhotonApi.api.searchPlaces(query = query)
}