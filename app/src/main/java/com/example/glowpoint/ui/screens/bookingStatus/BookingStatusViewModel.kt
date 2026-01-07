package com.example.glowpoint.ui.screens.bookingStatus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.glowpoint.data.models.FetchedBooking
import com.example.glowpoint.data.models.TimeSlot
import com.example.glowpoint.domain.usecase.BookingUseCase
import com.example.glowpoint.ui.screens.bookings.BookingsUIState
import com.example.glowpoint.util.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookingStatusViewModel @Inject constructor(
    private val bookingUseCase: BookingUseCase,
    private val networkUtils: NetworkUtils
) : ViewModel() {
    private val _uiState = MutableStateFlow<BookingStatusUIState>(BookingStatusUIState.Idle)
    val uiState: StateFlow<BookingStatusUIState> get() = _uiState.asStateFlow()

    private val _fetchedBooking = MutableStateFlow<FetchedBooking?>(null)
    val fetchedBooking: StateFlow<FetchedBooking?> get() = _fetchedBooking.asStateFlow()

    fun onSetFetchedBooking(fetchedBooking: FetchedBooking) {
        _fetchedBooking.value = fetchedBooking
    }

    fun observeFetchedBooking(bookingId: String) {
        if (!networkUtils.isInternetAvailable()) {
            _uiState.value = BookingStatusUIState.NoInternet
        }

        bookingUseCase.observeBooking(bookingId = bookingId)
            .onStart {
                _uiState.value = BookingStatusUIState.Loading
            }
            .onEach {
                _uiState.value = it
            }
            .launchIn(viewModelScope)
    }

    fun cancelBooking(previousStatus: String, bookingId: String, salonId: String, selectedTimeSlot: List<String>) {
        _uiState.value = BookingStatusUIState.Loading

        if (!networkUtils.isInternetAvailable()) {
            _uiState.value = BookingStatusUIState.NoInternet
            return
        }

        viewModelScope.launch {
            _uiState.value = bookingUseCase.cancelBooking(bookingId = bookingId, previousStatus = previousStatus, salonId = salonId, selectedTimeSlot = selectedTimeSlot)
        }
    }
}