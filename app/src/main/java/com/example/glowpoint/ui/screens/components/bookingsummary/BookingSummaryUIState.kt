package com.example.glowpoint.ui.screens.components.bookingsummary

import com.example.glowpoint.data.models.FetchedBooking
import com.example.glowpoint.domain.model.failure.realtime.WriteReqDomainFailure

sealed interface BookingSummaryUIState {
    object Idle: BookingSummaryUIState
    object Loading: BookingSummaryUIState
    data class Success(val bookingSummary: FetchedBooking): BookingSummaryUIState
    data class Failure(val failure: WriteReqDomainFailure): BookingSummaryUIState
    object NoInternet: BookingSummaryUIState
}