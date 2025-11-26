package com.example.glowpoint.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.glowpoint.data.models.SalonModel

data class SalonWithSelection(val salon: SalonModel, val selectedIds: List<String> = emptyList())

class SharedForEachSalonViewModel : ViewModel() {

    private val _isNavigate = MutableLiveData<Boolean>()
    val isNavigate: LiveData<Boolean> get() = _isNavigate

    private val _salon = MutableLiveData<SalonModel?>()
    val salon: LiveData<SalonModel?> get() = _salon

    private val _listOfServiceId = MutableLiveData<List<String>>(emptyList())

    // This MediatorLiveData will only fire when both salon and service IDs are set.
    val salonWithSelection = MediatorLiveData<SalonWithSelection?>().apply {
        var currentSalon: SalonModel? = null
        var currentIds: List<String>? = null

        val updater = { ->
            if (currentSalon != null && currentIds != null) {
                value = SalonWithSelection(currentSalon!!, currentIds!!)
            }
        }

        addSource(_salon) { salon ->
            currentSalon = salon
            updater()
        }
        addSource(_listOfServiceId) { ids ->
            currentIds = ids
            updater()
        }
    }

    fun onSetSalon(salon: SalonModel) {
        _salon.value = salon
        _isNavigate.value = true
    }

    fun onSetListOfServiceId(id: List<String>?) {
        _listOfServiceId.value = id ?: emptyList()
    }

    fun reset() {
        _isNavigate.value = false
        _salon.value = null
        _listOfServiceId.value = emptyList()
        salonWithSelection.value = null
    }
}