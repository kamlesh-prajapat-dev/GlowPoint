package com.example.glowpoint.domain.model.result

import com.example.glowpoint.data.models.FetchedServiceItem

sealed class SalonServicesResult {
    data class Success(val services: List<FetchedServiceItem>) : SalonServicesResult()
    data class Failure(val exception: Exception) : SalonServicesResult()
}
