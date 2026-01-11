package com.example.glowpoint.ui.screens.eachshop

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.glowpoint.data.models.ShopDetails
import com.example.glowpoint.data.models.TimeSlot
import com.example.glowpoint.data.models.ServiceItem
import com.example.glowpoint.data.models.User
import com.example.glowpoint.domain.usecase.ShopUseCase
import com.example.glowpoint.util.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class EachShopViewModel @Inject constructor(
    private val shopUseCase: ShopUseCase,
    private val networkUtils: NetworkUtils
) : ViewModel() {

    private val _uiState = MutableStateFlow<EachShopUIState>(EachShopUIState.Idle)
    val uiState: StateFlow<EachShopUIState> get() = _uiState.asStateFlow()

    private val _salon = MutableStateFlow<ShopDetails?>(null)
    val salon: StateFlow<ShopDetails?> get() = _salon.asStateFlow()

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> get() = _user.asStateFlow()

    private val _salonServices = MutableStateFlow<List<ServiceItem>>(emptyList())
    val salonServices: StateFlow<List<ServiceItem>> get() = _salonServices.asStateFlow()
    private val _timeSlots = MutableStateFlow<List<TimeSlot>>(emptyList())
    private val _filteredSlots = MutableStateFlow<List<TimeSlot>>(emptyList())
    val filteredSlots: StateFlow<List<TimeSlot>> get() = _filteredSlots.asStateFlow()

    private val _isVisibleBookSlotsButton = MutableStateFlow(false)
    val isVisibleBookSlotsButton: StateFlow<Boolean> get() = _isVisibleBookSlotsButton.asStateFlow()

    fun loadSalonData(salon: ShopDetails?, selectedServiceIds: List<ServiceItem>? = null) {
        _uiState.value = EachShopUIState.Loading

        if (salon == null) {
            _uiState.value = EachShopUIState.Error("Invalid salon data")
            return
        }

        _salon.value = salon

        if (selectedServiceIds != null) {
            _isTimeSlotRecyclerViewVisible.value = selectedServiceIds.isNotEmpty()
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                _user.value = shopUseCase.getUser()
                val allServicesFromSalon = getAllServicesFromSalon(salon)

                val servicesWithSelection = allServicesFromSalon.map { service ->
                    val isSelected = selectedServiceIds?.any { it.id == service.id } ?: false
                    service.copy(isSelected = isSelected)
                }.sortedByDescending { it.isSelected }

                if (servicesWithSelection.isNotEmpty()) {
                    _salonServices.value = servicesWithSelection
                }

                observeTimeSlots(salon.id, System.currentTimeMillis(), salon.openTime, salon.closeTime)

            } catch (e: Exception) {
                Log.e("EachShopViewModel", "Error loading services", e)
                _uiState.value = EachShopUIState.Error("Could not load services for this salon.")
            }
        }
    }

    private fun getAllServicesFromSalon(salon: ShopDetails): List<ServiceItem> {
        return when (salon.gender) {
            "Male" -> loadMenServices(salon)
            "Female" -> loadWomenServices(salon)
            "Both" -> {
                val menServices = loadMenServices(salon)
                val womenServices = loadWomenServices(salon)
                menServices + womenServices
            }

            else -> emptyList()
        }
    }

    private fun loadMenServices(salon: ShopDetails): List<ServiceItem> {
        return shopUseCase.loadMenServices()
            .filter { service -> salon.offered_services.contains(service.id) }
            .map {
                ServiceItem(
                    id = it.id,
                    name = it.name,
                    description = it.description,
                    price = it.price,
                    genderCategory = true
                )
            } // Ensure correct category
    }

    private fun loadWomenServices(salon: ShopDetails): List<ServiceItem> {
        return shopUseCase.loadWomenServices()
            .filter { service -> salon.offered_services.contains(service.id) }
            .map {
                ServiceItem(
                    id = it.id,
                    name = it.name,
                    description = it.description,
                    price = it.price,
                    genderCategory = false
                )
            }
    }

    fun toggleServiceSelection(selectedService: ServiceItem, genderCategory: Boolean) {
        val currentServices = _salonServices.value
        val updatedServices = currentServices.map {
            if (it.id == selectedService.id) it.copy(isSelected = !it.isSelected) else it
        }
        _salonServices.value = updatedServices
        updateTimeSlotVisibility()
    }

    private val _isTimeSlotRecyclerViewVisible = MutableStateFlow<Boolean>(false)
    val isTimeSlotRecyclerViewVisible: StateFlow<Boolean> get() = _isTimeSlotRecyclerViewVisible.asStateFlow()

    private fun updateTimeSlotVisibility() {
        val isAnyServiceSelected = _salonServices.value.any { it.isSelected }
        if (isAnyServiceSelected) {
            val baseSlots = _timeSlots.value
            filterTimeSlotsByService(baseSlots)
        }
        _isTimeSlotRecyclerViewVisible.value = isAnyServiceSelected
    }

    fun toggleTimeSlotSelection(selectedTimeSlot: TimeSlot) {
        val currentTimeSlots = _filteredSlots.value
        val updatedTimeSlots = currentTimeSlots.map {
            if (it.time == selectedTimeSlot.time) {
                it.copy(isSelected = !it.isSelected)
            } else {
                it
            }
        }
        _filteredSlots.value = updatedTimeSlots
        updateBookButtonVisibility()
    }

    private fun updateBookButtonVisibility() {
        val isAnyServiceSelected = _salonServices.value.any { it.isSelected }
        val isAnyTimeSlotSelected = _filteredSlots.value.any { it.isSelected }

        _isVisibleBookSlotsButton.value = isAnyServiceSelected && isAnyTimeSlotSelected
    }

    // Dummy function for time slots, replace with your logic
    private fun generateTimeSlots(open: String?, close: String?): List<TimeSlot> {
        val openTime = LocalTime.parse(open, DateTimeFormatter.ofPattern("HH:mm"))
        val closeTime = LocalTime.parse(close, DateTimeFormatter.ofPattern("HH:mm"))

        val slots = mutableListOf<TimeSlot>()
        var time = openTime
        while (time < closeTime) {
            slots.add(
                TimeSlot(
                    time = time.format(DateTimeFormatter.ofPattern("HH:mm")),
                    isAvailable = time > LocalTime.now(),
                    isSelected = false
                )
            )
            time = time.plusMinutes(30)
        }

        return slots
    }

    private fun observeTimeSlots(salonId: String, date: Long, openTime: String, closeTime: String) {
        val timeSlots = generateTimeSlots(openTime, closeTime)

        if (!networkUtils.isInternetAvailable()) {
            _uiState.value = EachShopUIState.NoInternet
        }

        _timeSlots.value = timeSlots

        shopUseCase.observeTimeSlot(salonId, date, openTime, closeTime)
            .onStart {
                _uiState.value = EachShopUIState.Loading
            }
            .onEach {
               _uiState.value = it
            }
            .launchIn(viewModelScope)
    }

    fun onSetTimeSlot(firebaseSlots: List<TimeSlot>) {
        val generatedSlots = _timeSlots.value
        val currentIndex = getCurrentSlotIndex(generatedSlots)

        // First pass: base availability
        val baseSlots = generatedSlots.mapIndexed { index, slot ->
            when {
                index <= currentIndex + 1 ->
                    slot.copy(isAvailable = false)

                else -> {
                    val firebaseSlot = firebaseSlots.find { it.time == slot.time }
                    slot.copy(isAvailable = firebaseSlot?.isAvailable ?: false)
                }
            }
        }

        _timeSlots.value = baseSlots
        // Second pass: continuity check
        filterTimeSlotsByService(baseSlots)
    }

    private fun filterTimeSlotsByService(baseSlots: List<TimeSlot>) {
        if (baseSlots.isEmpty()) return

        val requiredSlots = _salonServices.value.count { it.isSelected }

        if (requiredSlots <= 1) {
            _filteredSlots.value = baseSlots
            return
        }

//        val finalSlots = baseSlots.mapIndexed { index, slot ->
//            if (!slot.isAvailable) return@mapIndexed slot
//
//            val canFitAllServices =
//                (index until index + requiredSlots).all { i ->
//                    i < baseSlots.size && baseSlots[i].isAvailable
//                }
//
//            slot.copy(isAvailable = canFitAllServices)
//        }

        _filteredSlots.value = baseSlots
    }


    fun getSelectedTimeSlots(): List<TimeSlot> {
        return _filteredSlots.value.filter { it.isSelected }
    }

    fun getSelectedSalonServices(): List<ServiceItem> {
        return _salonServices.value.filter { it.isSelected }
    }

    private fun getCurrentSlotIndex(slots: List<TimeSlot>): Int {
        val formatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
        val now = LocalTime.now()

        return slots.indexOfLast { slot ->
            val slotTime = LocalTime.parse(slot.time, formatter)
            slotTime.isBefore(now)
        }
    }
}
