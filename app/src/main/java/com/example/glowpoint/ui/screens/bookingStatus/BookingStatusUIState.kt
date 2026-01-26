package com.example.glowpoint.ui.screens.bookingStatus

import com.example.glowpoint.data.models.FetchedBooking
import com.example.glowpoint.domain.model.failure.realtime.GetReqDomainFailure
import com.example.glowpoint.domain.model.failure.realtime.WriteReqDomainFailure

sealed interface BookingStatusUIState {
    object Idle: BookingStatusUIState
    object Loading: BookingStatusUIState
    object NoInternet: BookingStatusUIState
    data class Success(val fetchedBooking: FetchedBooking): BookingStatusUIState
    data class CancelSuccess(val isSuccess: Boolean): BookingStatusUIState
    data class GetFailure(val failure: GetReqDomainFailure): BookingStatusUIState
    data class WriteFailure(val failure: WriteReqDomainFailure): BookingStatusUIState
}