package com.example.glowpoint.ui.viewmodel

import android.content.Context
import android.location.Geocoder
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.glowpoint.data.database.local.LocalDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocationViewModel @Inject constructor(
    private val localDatabase: LocalDatabase
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun onChange(flag: Boolean){
        _isLoading.value = flag
    }
    fun storeLatitude(latitude: Double) {
        localDatabase.latitude = latitude.toFloat()
    }

    fun storeLongitude(longitude: Double) {
        localDatabase.longitude = longitude.toFloat()
    }

    fun setLocationName(locationName: String) {
        localDatabase.setLocationName(locationName)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun setLocationName(context: Context, latitude: Double, longitude: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val geocoder = Geocoder(context)
            try {
                geocoder.getFromLocation(latitude, longitude, 1) {
                    if (it.isNotEmpty()) {
                        localDatabase.setLocationName(it[0].locality)
                        localDatabase.setAreaOfUser(it[0].subLocality)
                    } else {
                        localDatabase.setLocationName("Not Found")
                    }
                }
            } catch (e: Exception) {
                localDatabase.setLocationName("Error")
            }
        }
    }
}