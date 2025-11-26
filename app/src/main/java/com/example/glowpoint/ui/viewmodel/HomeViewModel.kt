package com.example.glowpoint.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.glowpoint.data.database.local.LocalDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val localDatabase: LocalDatabase
) : ViewModel() {

    private val _location = MutableLiveData<String>()
    val location: LiveData<String> get() = _location

    private val _areaOfUser = MutableLiveData<String>()
    val areaOfUser: LiveData<String> get() = _areaOfUser

    /**
     * This function is now called from the Fragment to explicitly load the location data.
     */
    fun loadLocationData() {
        viewModelScope.launch(Dispatchers.IO) {
            delay(1000)
            loadLocation()
            loadAreaOfUser()
        }
    }

    private fun loadLocation() {
        val locationName = localDatabase.getLocationName()
        if (locationName.isNotEmpty()) {
            _location.postValue(locationName)
        }
    }

    private fun loadAreaOfUser() {
        val area = localDatabase.getAreaOfUser()
        if (area.isNotEmpty()) {
            _areaOfUser.postValue(area)
        }
    }
}