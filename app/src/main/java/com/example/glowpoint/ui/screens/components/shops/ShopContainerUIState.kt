package com.example.glowpoint.ui.screens.components.shops

import com.example.glowpoint.data.models.ShopDetails
import com.example.glowpoint.domain.model.failure.firestore.GetReqDomainFailure

sealed interface ShopContainerUIState {
    object Idle: ShopContainerUIState
    object Loading: ShopContainerUIState
    data class Success(val salons: List<ShopDetails>): ShopContainerUIState
    data class Failure(val failure: GetReqDomainFailure): ShopContainerUIState
    object NoInternet: ShopContainerUIState
    object NotServiceable: ShopContainerUIState
}