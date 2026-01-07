package com.example.glowpoint.domain.model

import com.example.glowpoint.data.models.ShopDetails
import com.example.glowpoint.data.models.TimeSlot

sealed class FetchSalonsResult {
    data class Success(val salons: List<ShopDetails>) : FetchSalonsResult()
    object NotServiceable : FetchSalonsResult()
    data class Failure(val exception: Exception) : FetchSalonsResult()
    data class GetTimeSlotSuccess(val timeSlots: List<TimeSlot>) : FetchSalonsResult()
    data class UpdateTimeSlotSuccess(val isSuccess: Boolean) : FetchSalonsResult()
}
