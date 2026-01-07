package com.example.glowpoint.ui.screens.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.glowpoint.data.local.LocalDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val localDatabase: LocalDatabase
) : ViewModel() {

    private val _location = MutableStateFlow("")
    val location: StateFlow<String> get() = _location.asStateFlow()

    private val _areaOfUser = MutableStateFlow("")
    val areaOfUser: StateFlow<String> get() = _areaOfUser.asStateFlow()

    /**
     * This function is now called from the Fragment to explicitly load the location data.
     */
    init {
        loadLocationData()
    }

    private fun loadLocationData() {
        loadLocation()
        loadAreaOfUser()
    }

    private fun loadLocation() {
        val locationName = localDatabase.getLocationName()
        if (locationName.isNotEmpty()) {
            _location.value = locationName
        }
    }

    private fun loadAreaOfUser() {
        val area = localDatabase.getAreaOfUser()
        if (area.isNotEmpty()) {
            _areaOfUser.value = area
        }
    }

    private val _uiState = MutableStateFlow<HomeUIState>(HomeUIState.ServiceState)
    val uiState: StateFlow<HomeUIState> get() = _uiState.asStateFlow()

    fun onSetUIState(uiState: HomeUIState) {
        _uiState.update { uiState }
    }
}