package com.example.glowpoint.ui.screens.home

sealed class HomeUIState {

    object Idle: HomeUIState()
    object ServiceState: HomeUIState()
    object ShopState: HomeUIState()
    object AccountState: HomeUIState()
    object LocationState: HomeUIState()
    object BookingsState: HomeUIState()
}