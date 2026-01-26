package com.example.glowpoint.ui.screens.components.services

import com.example.glowpoint.data.models.FetchedServiceItem
import com.example.glowpoint.domain.model.failure.firestore.GetReqDomainFailure

sealed class ServiceContainerUIState {

    object Idle: ServiceContainerUIState()
    object Loading: ServiceContainerUIState()
    object IsNetworkAvailable: ServiceContainerUIState()

    data class MenSuccess(val services: List<FetchedServiceItem>, val isAlreadyRequested: Boolean = false) : ServiceContainerUIState()
    data class WomenSuccess(val services: List<FetchedServiceItem>, val isAlreadyRequested: Boolean = false) : ServiceContainerUIState()
    data class Failure(val failure: GetReqDomainFailure) : ServiceContainerUIState()
}