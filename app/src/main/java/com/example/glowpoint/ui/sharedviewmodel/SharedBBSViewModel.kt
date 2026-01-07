package com.example.glowpoint.ui.sharedviewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SharedBBSViewModel: ViewModel() {
    private val _fetchedBooking = MutableStateFlow<String?>(null)
    val fetchedBooking: StateFlow<String?> get() = _fetchedBooking.asStateFlow()

    fun onSetFetchedBooking(fetchedBookingId: String) {
        _fetchedBooking.value = fetchedBookingId
    }

    fun reset() {
        _fetchedBooking.value = null
    }
}