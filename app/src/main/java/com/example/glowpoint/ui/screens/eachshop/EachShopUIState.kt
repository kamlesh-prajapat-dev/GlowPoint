package com.example.glowpoint.ui.screens.eachshop

import com.example.glowpoint.data.models.TimeSlot
import com.example.glowpoint.domain.model.failure.realtime.GetReqDomainFailure

sealed interface EachShopUIState {
    object Idle: EachShopUIState
    object Loading: EachShopUIState
    data class Success(val timeSlots: List<TimeSlot>): EachShopUIState
    data class Failure(val failure: GetReqDomainFailure): EachShopUIState
    object NoInternet: EachShopUIState
    data class Error(val message: String): EachShopUIState
}