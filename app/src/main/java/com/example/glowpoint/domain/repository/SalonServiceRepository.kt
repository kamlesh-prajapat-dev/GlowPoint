package com.example.glowpoint.domain.repository

import com.example.glowpoint.data.sample.ServiceModel
import com.example.glowpoint.domain.model.result.SalonServicesResult
import com.example.glowpoint.ui.screens.sample.SampleDataUIState

interface SalonServiceRepository {
    suspend fun getServices(collectionName: String): SalonServicesResult
    suspend fun createServices(services: List<ServiceModel>, collectionName: String): SampleDataUIState
}