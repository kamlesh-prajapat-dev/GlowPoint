package com.example.glowpoint.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.glowpoint.domain.usecase.AuthUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authUseCase: AuthUseCase
) : ViewModel() {
    private val _splashNavigateState = MutableStateFlow<SplashUIState>(SplashUIState.Idle)
    val splashNavigateState: StateFlow<SplashUIState> get() = _splashNavigateState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val location = authUseCase.getLocationName()
            val isUserLoggedIn = authUseCase.isUserLoggedIn()

            if (isUserLoggedIn && location.isEmpty()) {
                _splashNavigateState.value = SplashUIState.LocationState
            } else if (isUserLoggedIn && location.isNotEmpty()) {
                _splashNavigateState.value = SplashUIState.HomeState
            } else {
                _splashNavigateState.value = SplashUIState.LoginState
            }
        }
    }
}