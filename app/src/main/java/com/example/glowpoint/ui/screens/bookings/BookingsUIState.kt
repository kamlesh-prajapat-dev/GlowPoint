package com.example.glowpoint.ui.screens.bookings

import com.example.glowpoint.data.models.FetchedBooking
import com.example.glowpoint.domain.model.failure.realtime.GetReqDomainFailure

sealed interface BookingsUIState {
    object Idle: BookingsUIState
    object Loading: BookingsUIState
    data class GetSuccess(val bookings: List<FetchedBooking>): BookingsUIState
    data class Failure(val failure: GetReqDomainFailure): BookingsUIState
    object NoInternet: BookingsUIState
}