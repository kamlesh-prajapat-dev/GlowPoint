package com.example.glowpoint.ui.screens.location

import android.content.Context
import android.location.Geocoder
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.glowpoint.data.local.LocalDatabase
import com.example.glowpoint.domain.usecase.UserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocationViewModel @Inject constructor(
    private val userUseCase: UserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<LocationUIState>(LocationUIState.Idle)
    val uiState: StateFlow<LocationUIState> get() = _uiState.asStateFlow()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun saveLocation(latitude: Double, longitude: Double, context: Context) {
        _uiState.value = LocationUIState.Loading


        userUseCase.saveLocation(latitude, longitude, context) {
            _uiState.value = it
        }
    }
}