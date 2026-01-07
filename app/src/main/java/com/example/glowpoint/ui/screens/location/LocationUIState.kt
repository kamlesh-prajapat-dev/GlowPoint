package com.example.glowpoint.ui.screens.location

sealed interface LocationUIState {
    object Idle: LocationUIState
    object Loading: LocationUIState
    data class Success(val isSuccess: Boolean): LocationUIState
    data class Failure(val e: Exception): LocationUIState
}