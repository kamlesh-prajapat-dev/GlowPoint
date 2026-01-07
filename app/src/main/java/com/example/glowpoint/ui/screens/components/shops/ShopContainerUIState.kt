package com.example.glowpoint.ui.screens.components.shops

import com.example.glowpoint.data.models.ShopDetails

sealed interface ShopContainerUIState {
    object Idle: ShopContainerUIState
    object Loading: ShopContainerUIState
    data class Success(val salons: List<ShopDetails>): ShopContainerUIState
    data class Failure(val exception: Exception): ShopContainerUIState
    object NoInternet: ShopContainerUIState
    object NotServiceable: ShopContainerUIState
}