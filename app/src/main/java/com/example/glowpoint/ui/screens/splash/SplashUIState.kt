package com.example.glowpoint.ui.screens.splash

sealed interface SplashUIState {
    object Idle: SplashUIState
    object HomeState: SplashUIState
    object LocationState: SplashUIState
    object LoginState: SplashUIState
}