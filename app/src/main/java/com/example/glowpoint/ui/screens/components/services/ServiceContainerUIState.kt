package com.example.glowpoint.ui.screens.components.services

import com.example.glowpoint.data.models.FetchedServiceItem

sealed class ServiceContainerUIState {

    object Idle: ServiceContainerUIState()
    object Loading: ServiceContainerUIState()
    object IsNetworkAvailable: ServiceContainerUIState()

    data class Success(val services: List<FetchedServiceItem>, val genderCategory: Boolean) : ServiceContainerUIState()
    data class Failure(val exception: Exception) : ServiceContainerUIState()
}