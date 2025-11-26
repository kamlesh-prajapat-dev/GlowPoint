package com.example.glowpoint.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.glowpoint.data.database.local.LocalDatabase
import com.example.glowpoint.data.models.SalonModel
import com.example.glowpoint.domain.model.FetchSalonsResult
import com.example.glowpoint.domain.repository.SalonRepository
import com.example.glowpoint.util.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class ShopsContainerViewModel @Inject constructor(
    private val localDatabase: LocalDatabase,
    private val salonRepository: SalonRepository,
    private val networkUtils: NetworkUtils
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading
    private val _nearBySalons = MutableLiveData<List<SalonModel>>()
    val nearBySalons: LiveData<List<SalonModel>> get() = _nearBySalons

    private val _showEmptyState = MutableLiveData(false)
    val showEmptyState: LiveData<Boolean> get() = _showEmptyState

    private val _errorState = MutableLiveData<String?>(null)
    val errorState: LiveData<String?> get() = _errorState


    fun loadNearBySalons(maps: List<Map<String?, Boolean>>?) {
        val latitude = localDatabase.latitude.toDouble()
        val longitude = localDatabase.longitude.toDouble()

        _isLoading.postValue(true)
        _showEmptyState.postValue(false)
        _errorState.postValue(null)

        viewModelScope.launch(Dispatchers.IO) {
            val isNewUser = localDatabase.isNewUser()
            val timeSinceLastCache = System.currentTimeMillis() - localDatabase.lastCacheTimestampOfSalons
            val fiveDaysInMillis = TimeUnit.DAYS.toMillis(5)
            val shouldFetchFromFirebase = localDatabase.isLocationSet() && (isNewUser || timeSinceLastCache > fiveDaysInMillis)

            if (shouldFetchFromFirebase) {
                if (networkUtils.isInternetAvailable()) {
                    when (val fetchResult = fetchFromFirebaseAndCache(latitude, longitude)) {
                        is FetchSalonsResult.Success -> {
                            updateLiveData(
                                fetchResult.salons.filter { it ->
                                    when (checkGender(maps?.flatMap { it.values } ?: emptyList())) {
                                        0 -> {
                                            it.gender == "Male"|| it.gender == "Both"
                                        }
                                        1 -> {
                                            it.gender == "Female" || it.gender == "Both"
                                        }
                                        else -> {
                                            it.gender == "Both"
                                        }
                                    }
                                }.filter { it ->
                                    it.offered_services?.contains(maps?.map { it.keys } ?: false) ?: false
                                }
                            )
                        }
                        else -> loadFromCacheOrShowError(fetchResult, maps)
                    }
                } else {
                    _errorState.postValue("No internet connection. Please check your network.")
                    loadFromCacheOrShowError()
                }
            } else {
                loadFromCacheOrShowError(maps = maps)
            }
        }

    }

    private fun checkGender(genderList: List<Boolean>): Int {
        if (genderList.all { it }) {
            return 0
        }

        if (genderList.all { it}) {
            return 1
        }

        return 2
    }

    fun loadNearBySalons() {
        val latitude = localDatabase.latitude.toDouble()
        val longitude = localDatabase.longitude.toDouble()

        _isLoading.postValue(true)
        _showEmptyState.postValue(false)
        _errorState.postValue(null)

        viewModelScope.launch(Dispatchers.IO) {
            val isNewUser = localDatabase.isNewUser()
            val timeSinceLastCache = System.currentTimeMillis() - localDatabase.lastCacheTimestampOfSalons
            val fiveDaysInMillis = TimeUnit.DAYS.toMillis(5)
            val shouldFetchFromFirebase = localDatabase.isLocationSet() && (isNewUser || timeSinceLastCache > fiveDaysInMillis)

            if (shouldFetchFromFirebase) {
                if (networkUtils.isInternetAvailable()) {
                    when (val fetchResult = fetchFromFirebaseAndCache(latitude, longitude)) {
                        is FetchSalonsResult.Success -> updateLiveData(fetchResult.salons)
                        else -> loadFromCacheOrShowError(fetchResult)
                    }
                } else {
                    _errorState.postValue("No internet connection. Please check your network.")
                    loadFromCacheOrShowError()
                }
            } else {
                loadFromCacheOrShowError()
            }
        }
    }

    private fun loadFromCacheOrShowError(fetchResult: FetchSalonsResult? = null, maps: List<Map<String?, Boolean>>? = null) {
        val cachedSalons = localDatabase.getSalonModel()
        if (cachedSalons.isNotEmpty()) {
            if (maps != null) {
                updateLiveData(cachedSalons.filter { it ->
                    when (checkGender(maps.flatMap { it.values })) {
                        0 -> {
                            it.gender == "Male"|| it.gender == "Both"
                        }
                        1 -> {
                            it.gender == "Female" || it.gender == "Both"
                        }
                        else -> {
                            it.gender == "Both"
                        }
                    }
                }.filter { it ->
                    it.offered_services?.any { it in maps.flatMap { m -> m.keys } } ?: false
                })
            } else {
                updateLiveData(cachedSalons)
            }
        } else {
            when (fetchResult) {
                is FetchSalonsResult.NotServiceable -> _showEmptyState.postValue(true)
                is FetchSalonsResult.Failure -> _errorState.postValue("Salons could not be loaded at this time.")
                else -> {
                    // If there was no specific error and cache is empty, show empty state.
                    if (_errorState.value == null) { 
                        _showEmptyState.postValue(true)
                    }
                }
            }
            _isLoading.postValue(false)
        }
    }

    private suspend fun fetchFromFirebaseAndCache(latitude: Double, longitude: Double): FetchSalonsResult {
        return try {
            val result = salonRepository.getNearBySalon(latitude, longitude)
            if (result is FetchSalonsResult.Success) {
                localDatabase.setSalonModel(result.salons)
                localDatabase.lastCacheTimestampOfSalons = System.currentTimeMillis()
            }
            result
        } catch (e: Exception) {
            Log.e("ShopsViewModel", "Failed to fetch from Firebase", e)
            FetchSalonsResult.Failure(e)
        }
    }

    private fun updateLiveData(salons: List<SalonModel>) {
        _nearBySalons.postValue(salons)
        _isLoading.postValue(false)
        _showEmptyState.postValue(salons.isEmpty())
    }
}