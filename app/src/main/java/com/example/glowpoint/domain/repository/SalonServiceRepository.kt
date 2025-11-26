package com.example.glowpoint.domain.repository

import com.example.glowpoint.domain.model.FetchSalonServicesResult

interface SalonServiceRepository {

    suspend fun getMenSalonServices(): FetchSalonServicesResult

    suspend fun getWomenSalonServices(): FetchSalonServicesResult
}