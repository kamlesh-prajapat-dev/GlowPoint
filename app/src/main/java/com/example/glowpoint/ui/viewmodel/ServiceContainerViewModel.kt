package com.example.glowpoint.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.glowpoint.data.database.local.LocalDatabase
import com.example.glowpoint.data.models.User
import com.example.glowpoint.domain.model.FetchSalonServicesResult
import com.example.glowpoint.domain.model.ServiceItem
import com.example.glowpoint.domain.repository.SalonServiceRepository
import com.example.glowpoint.domain.repository.UserRepository
import com.example.glowpoint.util.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class ServiceContainerViewModel @Inject constructor(
    private val localDatabase: LocalDatabase,
    private val salonServiceRepository: SalonServiceRepository, // Corrected
    private val networkUtils: NetworkUtils
) : ViewModel() {

    // Error and Loading State
    private val _isLoading = MutableLiveData(true)
    val isLoading: LiveData<Boolean> get() = _isLoading
    fun onSetLoading(flag: Boolean) {
        _isLoading.value = flag
    }

    private val _showEmptyState = MutableLiveData(false)
    val showEmptyState: LiveData<Boolean> get() = _showEmptyState

    private val _errorState = MutableLiveData<String?>(null)
    val errorState: LiveData<String?> get() = _errorState

    private val _menServices = MutableLiveData<List<ServiceItem>>(emptyList())
    val menServices: LiveData<List<ServiceItem>> get() = _menServices

    private val _womenServices = MutableLiveData<List<ServiceItem>>(emptyList())
    val womenServices: LiveData<List<ServiceItem>> get() = _womenServices

    // New LiveData for the current user
    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> get() = _user

    init {
        _user.postValue(loadCurrentUser())
        loadMenServices()
        loadWomenServices()
    }

    fun loadCurrentUser(): User? {
        return  try {
            localDatabase.getUser()
        } catch (e: Exception) {
            null
        }
    }

    // women - false, men - true
    private fun loadWomenServices() {
        loadDataFromFirebase(false)
    }
    private fun loadMenServices() {
        loadDataFromFirebase(true)
    }

    private fun loadDataFromFirebase(genderCategory: Boolean) {
        _isLoading.postValue(true)
        _showEmptyState.postValue(false)
        _errorState.postValue(null)

        viewModelScope.launch(Dispatchers.IO) {
            val isNewUser = localDatabase.isNewUser()
            val timeSinceLastCache = System.currentTimeMillis() - localDatabase.lastCacheTimestampOfSalonServices
            val fiveDaysInMillis = TimeUnit.DAYS.toMillis(5)
            val shouldFetchFromFirebase = isNewUser || timeSinceLastCache > fiveDaysInMillis

            if (shouldFetchFromFirebase) {
                if (networkUtils.isInternetAvailable()) {
                    when (val fetchResult = fetchFromFirebaseAndCache(genderCategory)) {
                        is FetchSalonServicesResult.Success -> updateLiveData(fetchResult.services, genderCategory)
                        else -> loadFromCacheOrShowError(fetchResult = fetchResult, genderCategory = genderCategory) // Covers Failure and NotServiceable
                    }
                } else {
                    _errorState.postValue("No internet connection. Please check your network.")
                    loadFromCacheOrShowError(genderCategory = genderCategory)
                }
            } else {
                // Not supposed to fetch, just load from cache
                loadFromCacheOrShowError(genderCategory = genderCategory)
            }
        }
    }

    private fun loadFromCacheOrShowError(
        fetchResult: FetchSalonServicesResult? = null,
        genderCategory: Boolean
    ) {
        val cachedSalons = if (genderCategory) localDatabase.getMenServices() else localDatabase.getWomenServices()
        if (cachedSalons.isNotEmpty()) {
            updateLiveData(cachedSalons, genderCategory)
        } else {
            // Cache is also empty, determine final state
            when (fetchResult) {
                is FetchSalonServicesResult.NotServiceable -> _showEmptyState.postValue(true)
                else -> {
                    if (_errorState.value == null) { // Don't overwrite a specific network error
                        _errorState.postValue("Services could not be loaded at this time.")
                    }
                }
            }
            _isLoading.postValue(false)
        }
    }

    private suspend fun fetchFromFirebaseAndCache(genderCategory: Boolean): FetchSalonServicesResult {
        return try {
            val result = if (genderCategory) salonServiceRepository.getMenSalonServices() else salonServiceRepository.getWomenSalonServices()
            if (result is FetchSalonServicesResult.Success) {
                if (genderCategory) localDatabase.setMenServices(result.services) else localDatabase.setWomenServices(result.services)
                localDatabase.lastCacheTimestampOfSalonServices = System.currentTimeMillis()
            }
            result
        } catch (e: Exception) {
            Log.e("ServiceContainerViewModel", "Failed to fetch from Firebase", e)
            FetchSalonServicesResult.Failure(e)
        }
    }

    private fun updateLiveData(
        services: List<com.example.glowpoint.data.models.ServiceItem>,
        genderCategory: Boolean
    ) {
        if (genderCategory) _menServices.postValue(
            services.map {
                ServiceItem(
                    id = it.id,
                    name = it.name,
                    description = it.description,
                    genderCategory = true,
                    price = it.price
                )
            }
        ) else _womenServices.postValue(
            services.map {
                ServiceItem(
                    id = it.id,
                    name = it.name,
                    description = it.description,
                    genderCategory = false,
                    price = it.price
                )
            }
        )

        _isLoading.postValue(false)
        _showEmptyState.postValue(services.isEmpty()) // Show empty state if the final list is empty
    }

    fun toggleServiceSelection(selectedService: ServiceItem, genderCategory: Boolean) {

        if (genderCategory) {
            val menServices = _menServices.value ?: emptyList()

            _menServices.value = menServices.map {
                if (it.id == selectedService.id) {
                    it.copy(isSelected = !it.isSelected)
                } else {
                    it
                }
            }
        } else {
            val womenServices = _womenServices.value ?: emptyList()

            _womenServices.value = womenServices.map {
                if (it.id == selectedService.id) {
                    it.copy(isSelected = !it.isSelected)
                } else {
                    it
                }
            }
        }

        onSetVisibleSearchForShopButton()
    }

    private val _isVisibleSearchForShopButton = MutableLiveData<Boolean>()
    val isVisibleSearchForShopButton: LiveData<Boolean> get() = _isVisibleSearchForShopButton

    fun onSetVisibleSearchForShopButton() {
        val womenServices = _womenServices.value ?: emptyList()
        val menServices = _menServices.value ?: emptyList()

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

    fun getSelectedServices(): List<Map<String?, Boolean>>? {
        val menServices = _menServices.value ?: emptyList()
        val womenServices = _womenServices.value ?: emptyList()

        val listOfMenServicesId = menServices.filter { it.isSelected }.map { hashMapOf(Pair(it.id, true)) }

        val listOfWomenServicesId = womenServices.filter { it.isSelected }.map { hashMapOf(Pair(it.id, false)) }

        return listOfWomenServicesId + listOfMenServicesId
    }
}
