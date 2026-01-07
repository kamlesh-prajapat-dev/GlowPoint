package com.example.glowpoint.ui.sharedviewmodel

import androidx.lifecycle.ViewModel
import com.example.glowpoint.data.models.ServiceItem
import com.example.glowpoint.data.models.ShopDetails
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SalonWithSelection(val salon: ShopDetails? = null, val selectedIds: List<ServiceItem>? = null)

class SharedForEachSalonViewModel : ViewModel() {
    private val _salon = MutableStateFlow<ShopDetails?>(null)
    val salon: StateFlow<ShopDetails?> get() = _salon.asStateFlow()
    private val _listOfServiceId = MutableStateFlow<List<ServiceItem>?>(null)
    val listOfServiceId: StateFlow<List<ServiceItem>?> get() = _listOfServiceId.asStateFlow()

    private val _salonWithSelection = MutableStateFlow<SalonWithSelection?>(null)
    val salonWithSelection: StateFlow<SalonWithSelection?> get() = _salonWithSelection.asStateFlow()


    fun onSetInitialData(salon: ShopDetails? = null, selectedServices: List<ServiceItem>? = null) {
        if (salon != null) {
            _salon.value = salon
            setSalonWithSelection()
        }

        if (selectedServices != null) {
            _listOfServiceId.value = selectedServices
        }
    }

    private fun setSalonWithSelection() {
        val salon = _salon.value
        val selectedServices = _listOfServiceId.value
        if (salon != null && selectedServices != null) {
            _salonWithSelection.value = SalonWithSelection(salon, selectedServices)
        } else if (salon != null) {
            _salonWithSelection.value = SalonWithSelection(salon)
        }
    }

    fun salonReset() {
        _salon.value = null
    }

    fun reset() {
        _salon.value = null
        _listOfServiceId.value = null
    }
}