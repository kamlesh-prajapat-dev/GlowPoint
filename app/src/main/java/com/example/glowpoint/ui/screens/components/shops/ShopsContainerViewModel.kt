package com.example.glowpoint.ui.screens.components.shops

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.glowpoint.data.local.LocalDatabase
import com.example.glowpoint.data.models.ServiceItem
import com.example.glowpoint.data.models.ShopDetails
import com.example.glowpoint.domain.usecase.ShopUseCase
import com.example.glowpoint.util.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class ShopsContainerViewModel @Inject constructor(
    private val localDatabase: LocalDatabase,
    private val shopUseCase: ShopUseCase,
    private val networkUtils: NetworkUtils
) : ViewModel() {

    private val _uiState = MutableStateFlow<ShopContainerUIState>(ShopContainerUIState.Idle)
    val uiState: StateFlow<ShopContainerUIState> get() = _uiState.asStateFlow()

    private val _nearBySalons = MutableStateFlow<List<ShopDetails>>(emptyList())
    val nearBySalons: StateFlow<List<ShopDetails>> get() = _nearBySalons.asStateFlow()

    fun onSetNearBySalons(salons: List<ShopDetails>) {
        _nearBySalons.value = salons
    }

    fun loadNearBySalons(selectedServices: List<ServiceItem>) {
        _uiState.value = ShopContainerUIState.Loading

        val latitude = localDatabase.latitude.toDouble()
        val longitude = localDatabase.longitude.toDouble()

        viewModelScope.launch(Dispatchers.IO) {
            val isNewUser = localDatabase.isNewUser()
            val timeSinceLastCache = System.currentTimeMillis() - localDatabase.lastCacheTimestampOfSalons
            val fiveDaysInMillis = TimeUnit.DAYS.toMillis(5)
            val shouldFetchFromFirebase = localDatabase.isLocationSet() && (isNewUser || timeSinceLastCache > fiveDaysInMillis)

            if (shouldFetchFromFirebase) {
                if (networkUtils.isInternetAvailable()) {
                    val fetchResult = fetchFromFirebaseAndCache(latitude, longitude, selectedServices)
                    when (fetchResult) {
                        is ShopContainerUIState.Failure -> loadFromCacheOrShowError(selectedServices)
                        else -> Unit
                    }
                    _uiState.value = fetchResult
                } else {
                    _uiState.value = ShopContainerUIState.NoInternet
                    loadFromCacheOrShowError(selectedServices)
                }
            } else {
                loadFromCacheOrShowError(selectedServices)
            }
        }
    }

    fun loadNearBySalons() {
        _uiState.value = ShopContainerUIState.Loading

        val latitude = localDatabase.latitude.toDouble()
        val longitude = localDatabase.longitude.toDouble()

        viewModelScope.launch(Dispatchers.IO) {
            val isNewUser = localDatabase.isNewUser()
            val timeSinceLastCache = System.currentTimeMillis() - localDatabase.lastCacheTimestampOfSalons
            val fiveDaysInMillis = TimeUnit.DAYS.toMillis(5)
            val shouldFetchFromFirebase = localDatabase.isLocationSet() && (isNewUser || timeSinceLastCache > fiveDaysInMillis)

            if (shouldFetchFromFirebase) {
                if (networkUtils.isInternetAvailable()) {
                    val fetchResult = fetchFromFirebaseAndCache(latitude, longitude)
                    when (fetchResult) {
                        is ShopContainerUIState.Failure -> loadFromCacheOrShowError()
                        else -> Unit
                    }
                    _uiState.value = fetchResult
                } else {
                    _uiState.value = ShopContainerUIState.NoInternet
                    loadFromCacheOrShowError()
                }
            } else {
                loadFromCacheOrShowError()
            }
        }
    }

    private fun loadFromCacheOrShowError(selectedServices: List<ServiceItem>? = null) {
        val cachedSalons = shopUseCase.loadSalonFromCache(selectedServices)
        if (cachedSalons.isNotEmpty()) {
            _uiState.value = ShopContainerUIState.Success(cachedSalons)
        } else {
           _uiState.value = ShopContainerUIState.NotServiceable
        }
    }

    private suspend fun fetchFromFirebaseAndCache(latitude: Double, longitude: Double, selectedServices: List<ServiceItem>? = null): ShopContainerUIState {
        return shopUseCase.getNearBySalon(latitude, longitude, selectedServices)
    }
}