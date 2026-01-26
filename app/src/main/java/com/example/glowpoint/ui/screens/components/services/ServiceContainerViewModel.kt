package com.example.glowpoint.ui.screens.components.services

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.glowpoint.data.models.ServiceItem
import com.example.glowpoint.data.models.User
import com.example.glowpoint.domain.usecase.SalonServiceUseCase
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
class ServiceContainerViewModel @Inject constructor(
    private val salonServiceUseCase: SalonServiceUseCase,
    private val networkUtils: NetworkUtils
) : ViewModel() {
    private val _uiState = MutableStateFlow<ServiceContainerUIState>(ServiceContainerUIState.Idle)
    val uiState: StateFlow<ServiceContainerUIState> get() = _uiState.asStateFlow()

    private val _isFirstRequestedCompleteForMen = MutableStateFlow(false)
    val isFirstRequestedCompleteForMen: StateFlow<Boolean> get() = _isFirstRequestedCompleteForMen.asStateFlow()

    private val _isFirstRequestedCompletedForWomen = MutableStateFlow(false)
    val isFirstRequestedCompletedForWomen: StateFlow<Boolean> get() = _isFirstRequestedCompletedForWomen.asStateFlow()


    // ---- For Load Initial State ----
    private val _menServices = MutableStateFlow<List<ServiceItem>>(emptyList())
    val menServices: StateFlow<List<ServiceItem>> get() = _menServices.asStateFlow()
    private val _womenServices = MutableStateFlow<List<ServiceItem>>(emptyList())
    val womenServices: StateFlow<List<ServiceItem>> get() = _womenServices.asStateFlow()
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> get() = _user.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        _user.value = salonServiceUseCase.getCurrentUser()
    }

    fun loadServices(genderCategory: Boolean) {
        loadDataFromFirebase(genderCategory)
    }

    private fun loadDataFromFirebase(genderCategory: Boolean) {
        _uiState.value = ServiceContainerUIState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            val isNewUser = salonServiceUseCase.isNewUser()
            val timeSinceLastCache =
                if (!genderCategory)
                    System.currentTimeMillis() - salonServiceUseCase.getLastTemeCacheOfWomenServices()
                else
                    System.currentTimeMillis() - salonServiceUseCase.getLastTemeCacheOfMenServices()
            val fiveDaysInMillis = TimeUnit.DAYS.toMillis(5)
            val shouldFetchFromFirebase = isNewUser || timeSinceLastCache > fiveDaysInMillis

            if (shouldFetchFromFirebase) {
                if (networkUtils.isInternetAvailable()) {
                    val fetchResult = fetchFromFirebaseAndCache(genderCategory)
                    when (fetchResult) {
                        is ServiceContainerUIState.Failure -> loadFromCacheOrShowError(
                            genderCategory = genderCategory
                        )

                        else -> Unit
                    }
                    _uiState.value = fetchResult
                } else {
                    _uiState.value = ServiceContainerUIState.IsNetworkAvailable
                    loadFromCacheOrShowError(genderCategory = genderCategory)
                }
            } else {
                loadFromCacheOrShowError(genderCategory = genderCategory)
            }
        }
    }

    fun loadFromCacheOrShowError(
        genderCategory: Boolean
    ) {
        val cachedSalons =
            if (genderCategory) salonServiceUseCase.getMenServicesFromCache() else salonServiceUseCase.getWomenServicesFromCache()

        if (genderCategory) {
            _uiState.value = ServiceContainerUIState.MenSuccess(cachedSalons)
        } else {
            _uiState.value = ServiceContainerUIState.WomenSuccess(cachedSalons)
        }
    }

    private suspend fun fetchFromFirebaseAndCache(genderCategory: Boolean): ServiceContainerUIState {
        val result =
            if (genderCategory) salonServiceUseCase.getMenSalonServices() else salonServiceUseCase.getWomenSalonServices()
        return result
    }

    @Synchronized
    fun updateLiveData(
        services: List<com.example.glowpoint.data.models.FetchedServiceItem>,
        genderCategory: Boolean
    ) {
        if (genderCategory) {
            _menServices.value =
                services.map {
                    ServiceItem(
                        id = it.id,
                        name = it.name,
                        description = it.description,
                        genderCategory = true,
                        price = it.price
                    )
                }
            _isFirstRequestedCompleteForMen.value = true
        } else {
            _womenServices.value =
                services.map {
                    ServiceItem(
                        id = it.id,
                        name = it.name,
                        description = it.description,
                        genderCategory = false,
                        price = it.price
                    )
                }
            _isFirstRequestedCompletedForWomen.value = true
        }
    }

    fun toggleServiceSelection(selectedService: ServiceItem, genderCategory: Boolean) {
        if (genderCategory) {
            val menServices = _menServices.value

            _menServices.value = menServices.map {
                if (it.id == selectedService.id) {
                    selectedService.copy(isSelected = !it.isSelected)
                } else {
                    it
                }
            }
        } else {
            val womenServices = _womenServices.value

            _womenServices.value = womenServices.map {
                if (it.id == selectedService.id) {
                    selectedService.copy(isSelected = !it.isSelected)
                } else {
                    it
                }
            }
        }

        onSetVisibleSearchForShopButton()
    }

    private val _isVisibleSearchForShopButton = MutableStateFlow(false)
    val isVisibleSearchForShopButton: StateFlow<Boolean> get() = _isVisibleSearchForShopButton.asStateFlow()

    private fun onSetVisibleSearchForShopButton() {
        val womenServices = _womenServices.value
        val menServices = _menServices.value

        menServices.forEach {
            if (it.isSelected) {
                _isVisibleSearchForShopButton.value = true
                return
            }
        }

        womenServices.forEach {
            if (it.isSelected) {
                _isVisibleSearchForShopButton.value = true
                return
            }
        }

        _isVisibleSearchForShopButton.value = false
    }

    fun getSelectedServices(): List<ServiceItem> {
        val menServices = _menServices.value
        val womenServices = _womenServices.value

        val listOfMenServicesId =
            menServices.filter { it.isSelected }

        val listOfWomenServicesId =
            womenServices.filter { it.isSelected }

        return listOfWomenServicesId + listOfMenServicesId
    }
}