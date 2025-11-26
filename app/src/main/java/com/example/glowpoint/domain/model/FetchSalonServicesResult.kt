package com.example.glowpoint.domain.model

import com.example.glowpoint.data.models.ServiceItem

sealed class FetchSalonServicesResult {
    data class Success(val services: List<ServiceItem>) : FetchSalonServicesResult()
    object NotServiceable : FetchSalonServicesResult()
    data class Failure(val exception: Exception) : FetchSalonServicesResult()
}
