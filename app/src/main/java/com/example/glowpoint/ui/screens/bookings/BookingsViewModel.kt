package com.example.glowpoint.ui.screens.bookings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.glowpoint.data.models.FetchedBooking
import com.example.glowpoint.domain.repository.BookingRepository
import com.example.glowpoint.domain.usecase.BookingUseCase
import com.example.glowpoint.util.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

@HiltViewModel
class BookingsViewModel @Inject constructor(
    private val bookingUseCase: BookingUseCase,
    private val networkUtils: NetworkUtils
) : ViewModel() {
    private val _uiState = MutableStateFlow<BookingsUIState>(BookingsUIState.Idle)
    val uiState: StateFlow<BookingsUIState> get() = _uiState.asStateFlow()

    private val _bookings = MutableStateFlow<List<FetchedBooking>>(emptyList())
    val bookings: StateFlow<List<FetchedBooking>> get() = _bookings.asStateFlow()

    fun onSetBookings(bookings: List<FetchedBooking>) {
        _bookings.value = bookings
    }

    init {
        observeBookings()
    }

    private fun observeBookings() {

        if (!networkUtils.isInternetAvailable()) {
            _uiState.value = BookingsUIState.NoInternet
        }

        bookingUseCase.observeBookings()
            .onStart {
                _uiState.value = BookingsUIState.Loading
            }
            .onEach {
                _uiState.value = it
            }
            .launchIn(viewModelScope)
    }
}