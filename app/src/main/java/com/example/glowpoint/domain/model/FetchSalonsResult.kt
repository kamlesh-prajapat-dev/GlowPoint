package com.example.glowpoint.domain.model

import com.example.glowpoint.data.models.SalonModel

sealed class FetchSalonsResult {
    data class Success(val salons: List<SalonModel>) : FetchSalonsResult()
    object NotServiceable : FetchSalonsResult()
    data class Failure(val exception: Exception) : FetchSalonsResult()
}
