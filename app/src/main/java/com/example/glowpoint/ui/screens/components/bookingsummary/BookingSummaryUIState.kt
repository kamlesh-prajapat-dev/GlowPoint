package com.example.glowpoint.ui.screens.components.bookingsummary

import com.example.glowpoint.data.models.FetchedBooking

sealed interface BookingSummaryUIState {
    object Idle: BookingSummaryUIState
    object Loading: BookingSummaryUIState
    data class Success(val bookingSummary: FetchedBooking): BookingSummaryUIState
    data class Failure(val exception: Exception): BookingSummaryUIState
    object NoInternet: BookingSummaryUIState
}