package com.example.glowpoint.ui.screens.bookings

import com.example.glowpoint.data.models.FetchedBooking

sealed interface BookingsUIState {
    object Idle: BookingsUIState
    object Loading: BookingsUIState
    data class GetSuccess(val bookings: List<FetchedBooking>): BookingsUIState
    data class Failure(val exception: Exception): BookingsUIState
    object NoInternet: BookingsUIState
}