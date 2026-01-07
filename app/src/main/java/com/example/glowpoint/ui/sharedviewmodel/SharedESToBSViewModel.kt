package com.example.glowpoint.ui.sharedviewmodel

import androidx.lifecycle.ViewModel
import com.example.glowpoint.data.models.ServiceItem
import com.example.glowpoint.data.models.ShopDetails
import com.example.glowpoint.data.models.TimeSlot
import com.example.glowpoint.data.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SharedESToBSViewModel: ViewModel() {
    private val _selectedServices = MutableStateFlow<List<ServiceItem>>(emptyList())
    private val _shopDetails = MutableStateFlow<ShopDetails?>(null)
    private val _selectedTimeSlot = MutableStateFlow<List<TimeSlot>>(emptyList())
    private val _userDetails = MutableStateFlow<User?>(null)

    val selectedServices: StateFlow<List<ServiceItem>> get() = _selectedServices.asStateFlow()
    val shopDetails: StateFlow<ShopDetails?> get() = _shopDetails.asStateFlow()
    val selectedTimeSlot: StateFlow<List<TimeSlot>> get() = _selectedTimeSlot.asStateFlow()
    val userDetails: StateFlow<User?> get() = _userDetails.asStateFlow()

    fun setBookingDetails(selectedServices: List<ServiceItem>, shopDetails: ShopDetails?, selectedTimeSlot: List<TimeSlot>, userDetails: User?) {
        _selectedServices.value = selectedServices
        _shopDetails.value = shopDetails
        _selectedTimeSlot.value = selectedTimeSlot
        _userDetails.value = userDetails
    }
}