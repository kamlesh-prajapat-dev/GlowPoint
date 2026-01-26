package com.example.glowpoint.ui.screens.location

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.glowpoint.data.models.LocationSuggestion
import com.example.glowpoint.domain.usecase.ShopUseCase
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
    private val userUseCase: UserUseCase,
    private val shopUseCase: ShopUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow<String?>(null)
    val searchQuery: StateFlow<String?> get() = _searchQuery.asStateFlow()

    private val _locationSuggestions = MutableStateFlow<List<LocationSuggestion>?>(null)
    val locationSuggestions: StateFlow<List<LocationSuggestion>?> get() = _locationSuggestions.asStateFlow()

    fun onChangeLocationSuggestions(suggestions: List<LocationSuggestion>) {
        _locationSuggestions.value = suggestions
        cachedSuggestions = suggestions
    }

    private var lastApiPrefix: String? = null
    private var cachedSuggestions: List<LocationSuggestion> = emptyList()

    fun searchLocation(query: String) {
        _searchQuery.value = query

        // 1️⃣ Ignore very short input
        if (query.length <= 3) {
            _locationSuggestions.value = emptyList()
            _uiState.value = LocationUIState.Idle
            lastApiPrefix = null
            return
        }

        val currentPrefix = query.take(3).lowercase()
        val suggestions = _locationSuggestions.value

        // 2️⃣ Same prefix → filter locally
        if (currentPrefix == lastApiPrefix && suggestions != null && suggestions.isNotEmpty()) {
            _locationSuggestions.value = cachedSuggestions.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.details.contains(query, ignoreCase = true)
            }
            return
        }

        // 3️⃣ New prefix → fresh API call
        lastApiPrefix = currentPrefix
        _uiState.value = LocationUIState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                userUseCase.searchPlaces(query)
            }.onSuccess { result ->
                _uiState.value = result
            }
        }
    }


    fun onLocationSelected(suggestion: LocationSuggestion) {
        // Handle the selected location
        _uiState.value = LocationUIState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                userUseCase.searchAddress(suggestion)
            }.onSuccess { result ->
                _uiState.value = result
            }
        }
    }

    fun loadNearBySalons(suggestion: LocationSuggestion) {
        _uiState.value = LocationUIState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = shopUseCase.getNearBySalon(suggestion)
        }
    }

    private val _uiState = MutableStateFlow<LocationUIState>(LocationUIState.Idle)
    val uiState: StateFlow<LocationUIState> get() = _uiState.asStateFlow()

    fun saveLocation(suggestion: LocationSuggestion) {
        _uiState.value = userUseCase.saveLocation(suggestion)
    }

    fun saveLocation(latitude: Double, longitude: Double, context: Context) {
        _uiState.value = LocationUIState.Loading

        userUseCase.saveLocation(latitude, longitude, context) {
            _uiState.value = it
        }
    }

    fun reset() {
        _uiState.value = LocationUIState.Idle
        _searchQuery.value = null
        _locationSuggestions.value = null
        lastApiPrefix = null
        cachedSuggestions = emptyList()
    }
}