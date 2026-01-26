package com.example.glowpoint.ui.screens.sample

sealed interface SampleDataUIState {
    object Idle: SampleDataUIState
    object Loading: SampleDataUIState
    object Success: SampleDataUIState
    object AlreadySaved: SampleDataUIState
    data class Failure(val e: Exception): SampleDataUIState
}