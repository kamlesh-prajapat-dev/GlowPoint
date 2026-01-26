package com.example.glowpoint.ui.screens.location

import com.example.glowpoint.data.models.LocationSuggestion
import com.example.glowpoint.domain.model.failure.firestore.GetReqDomainFailure

sealed interface LocationUIState {
    object Idle: LocationUIState
    object Loading: LocationUIState
    data class Success(val isSuccess: Boolean): LocationUIState
    data class Failure(val failure: GetReqDomainFailure): LocationUIState
    data class FetchedLocationsSuccess(val suggestions: List<LocationSuggestion>): LocationUIState
    data class FetchedLocationSuccess(val suggestion: LocationSuggestion): LocationUIState
    data class GetNearBySalonSuccess(val suggestion: LocationSuggestion): LocationUIState
}