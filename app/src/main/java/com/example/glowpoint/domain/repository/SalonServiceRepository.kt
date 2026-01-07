package com.example.glowpoint.domain.repository

import com.example.glowpoint.domain.model.SalonServicesResult

interface SalonServiceRepository {

    suspend fun getServices(collectionName: String): SalonServicesResult
}