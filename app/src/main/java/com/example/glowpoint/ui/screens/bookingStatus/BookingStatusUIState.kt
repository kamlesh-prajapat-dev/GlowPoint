package com.example.glowpoint.ui.screens.bookingStatus

import com.example.glowpoint.data.models.FetchedBooking

sealed interface BookingStatusUIState {
    object Idle: BookingStatusUIState
    object Loading: BookingStatusUIState
    object NoInternet: BookingStatusUIState
    data class Success(val fetchedBooking: FetchedBooking): BookingStatusUIState
    data class CancelSuccess(val isSuccess: Boolean): BookingStatusUIState
    data class Failure(val exception: Exception): BookingStatusUIState
}