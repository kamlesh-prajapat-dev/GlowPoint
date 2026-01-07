package com.example.glowpoint.ui.screens.components.bookingsummary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.glowpoint.data.models.BookingDetails
import com.example.glowpoint.data.models.PaymentDetails
import com.example.glowpoint.data.models.ServiceItem
import com.example.glowpoint.data.models.ShopDetails
import com.example.glowpoint.data.models.TimeSlot
import com.example.glowpoint.data.models.User
import com.example.glowpoint.domain.usecase.BookingUseCase
import com.example.glowpoint.util.BookingStatus
import com.example.glowpoint.util.NetworkUtils
import com.example.glowpoint.util.PaymentStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookingSummaryViewModel @Inject constructor(
    private val bookingUseCase: BookingUseCase,
    private val networkUtils: NetworkUtils
): ViewModel() {
    private val _uiState = MutableStateFlow<BookingSummaryUIState>(BookingSummaryUIState.Idle)
    val uiState: StateFlow<BookingSummaryUIState> get() = _uiState.asStateFlow()

    private val _bookingDetails = MutableStateFlow<BookingDetails?>(null)
    val bookingDetails: StateFlow<BookingDetails?> get() = _bookingDetails.asStateFlow()

    private val _userDetails = MutableStateFlow<User?>(null)
    val userDetails: StateFlow<User?> get() = _userDetails.asStateFlow()

    private val _selectedTimeSlots = MutableStateFlow<List<TimeSlot>>(emptyList())
    val selectedTimeSlots: StateFlow<List<TimeSlot>> get() = _selectedTimeSlots.asStateFlow()

    private val _selectedServices = MutableStateFlow<List<ServiceItem>>(emptyList())
    val selectedServices: StateFlow<List<ServiceItem>> get() = _selectedServices.asStateFlow()

    private val _shopDetails = MutableStateFlow<ShopDetails?>(null)
    val shopDetails: StateFlow<ShopDetails?> get() = _shopDetails.asStateFlow()

    fun setBookingDetails(user: User, shopDetails: ShopDetails, selectedTimeSlots: List<TimeSlot>, selectedServices: List<ServiceItem>) {
        _userDetails.value = user
        _shopDetails.value = shopDetails
        _selectedServices.value = selectedServices
        _selectedTimeSlots.value = selectedTimeSlots
        _bookingDetails.value = BookingDetails(
            userId = user.uid,
            shopId = shopDetails.id,
            shopName = shopDetails.name,
            shopAddress = shopDetails.address,
            distance = shopDetails.distance,
            selectedServices = selectedServices,
            selectedTimeSlot = selectedTimeSlots.map { it.time },
            createdAt = System.currentTimeMillis(),
            bookingStatus = BookingStatus.PENDING,
            paymentDetails = PaymentDetails()
        )
    }

    fun bookService(paymentMethod: String) {
        _uiState.value = BookingSummaryUIState.Loading

        if (!networkUtils.isInternetAvailable()) {
            _uiState.value = BookingSummaryUIState.NoInternet
            return
        }

        var totalAmount = 0.0
        _selectedServices.value.forEach { service ->
            totalAmount += service.price
        }

        viewModelScope.launch {
            val bookingDetails = _bookingDetails.value
            if(bookingDetails != null) {
                val bookingDetailsWithPaymentDetails = bookingDetails.copy(
                    paymentDetails = PaymentDetails(
                        paymentMethod = paymentMethod,
                        paymentAmount = totalAmount,
                        paymentStatus = PaymentStatus.PENDING,
                        paymentTimestamp = System.currentTimeMillis()
                    )
                )
                _uiState.value = bookingUseCase.bookService(bookingDetailsWithPaymentDetails)
                _bookingDetails.value = bookingDetailsWithPaymentDetails
            }
        }
    }
}