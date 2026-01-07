package com.example.glowpoint.ui.screens.eachshop

import com.example.glowpoint.data.models.TimeSlot

sealed interface EachShopUIState {
    object Idle: EachShopUIState
    object Loading: EachShopUIState
    data class Success(val timeSlots: List<TimeSlot>): EachShopUIState
    data class Failure(val exception: Exception): EachShopUIState
    object NoInternet: EachShopUIState
    data class Error(val message: String): EachShopUIState
}