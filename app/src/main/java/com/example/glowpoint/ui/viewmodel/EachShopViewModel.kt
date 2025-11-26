package com.example.glowpoint.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.glowpoint.data.database.local.LocalDatabase
import com.example.glowpoint.data.models.SalonModel
import com.example.glowpoint.data.models.TimeSlot
import com.example.glowpoint.domain.model.ServiceItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.collections.contains

@HiltViewModel
class EachShopViewModel @Inject constructor(
    private val localDatabase: LocalDatabase
) : ViewModel() {

    // LiveData for UI states
    private val _isLoading = MutableLiveData(true)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _showEmptyState = MutableLiveData(false)
    val showEmptyState: LiveData<Boolean> get() = _showEmptyState

    private val _errorState = MutableLiveData<String?>(null)
    val errorState: LiveData<String?> get() = _errorState

    // LiveData for data
    private val _salonServices = MutableLiveData<List<ServiceItem>>(emptyList())
    val salonServices: LiveData<List<ServiceItem>> get() = _salonServices

    private val _timeSlots = MutableLiveData<List<TimeSlot>>(emptyList())
    val timeSlots: LiveData<List<TimeSlot>> get() = _timeSlots

    private val _isVisibleBookSlotsButton = MutableLiveData<Boolean>()
    val isVisibleBookSlotsButton: LiveData<Boolean> get() = _isVisibleBookSlotsButton

    /**
     * This is the main entry point for loading all data for this screen.
     * It processes the salon and pre-selected services.
     */
    fun loadSalonData(salon: SalonModel?, selectedServiceIds: List<String>? = null) {
        _isLoading.value = true
        _showEmptyState.value = false
        _errorState.value = null

        if (salon == null) {
            _errorState.postValue("Salon data is not available.")
            _isLoading.postValue(false)
            _showEmptyState.postValue(true)
            return
        }

        if (selectedServiceIds != null) {
            _isTimeSlotRecyclerViewVisible.value = selectedServiceIds.isNotEmpty()
        }

        viewModelScope.launch {
            try {
                // Process the services from the salon object itself
                val allServicesFromSalon = getAllServicesFromSalon(salon)

                // Set the selection state based on the IDs passed from the previous screen
                val servicesWithSelection = allServicesFromSalon.map { service ->
                    val isSelected = selectedServiceIds?.contains(service.id) == true
                    service.copy(isSelected = isSelected)
                }.sortedByDescending { it.isSelected }

                if (servicesWithSelection.isNotEmpty()) {
                    _salonServices.postValue(servicesWithSelection)
                    _showEmptyState.postValue(false)
                } else {
                    _showEmptyState.postValue(true)
                }

                // TODO: Replace this with your actual time slot fetching logic
                val dummyTimeSlots = generateTimeSlots(salon.openTime, salon.closeTime)
                _timeSlots.postValue(dummyTimeSlots)

            } catch (e: Exception) {
                Log.e("EachShopViewModel", "Error loading services", e)
                _errorState.postValue("Could not load services for this salon.")
            }
            _isLoading.postValue(false)
        }
    }

    private fun getAllServicesFromSalon(salon: SalonModel): List<ServiceItem> {
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

    private fun loadMenServices(salon: SalonModel): List<ServiceItem> {
        return localDatabase.getMenServices()
            .filter { service -> salon.offered_services?.contains(service.id) ?: false }
            .map { ServiceItem(id = it.id, name = it.name, description = it.description, price = it.price, genderCategory = true) } // Ensure correct category
    }

    private fun loadWomenServices(salon: SalonModel): List<ServiceItem> {
        return localDatabase.getWomenServices()
            .filter { service -> salon.offered_services?.contains(service.id) ?: false }
            .map { ServiceItem(id = it.id, name = it.name, description = it.description, price = it.price, genderCategory = false) } // Ensure correct category
    }

    fun toggleServiceSelection(selectedService: ServiceItem, genderCategory: Boolean) {
        val currentServices = _salonServices.value ?: return
        val updatedServices = currentServices.map {
            if (it.id == selectedService.id) it.copy(isSelected = !it.isSelected) else it
        }
        _salonServices.value = updatedServices
        updateTimeSlotVisibility()
    }

    private val _isTimeSlotRecyclerViewVisible = MutableLiveData<Boolean>()
    val isTimeSlotRecyclerViewVisible: LiveData<Boolean> get() = _isTimeSlotRecyclerViewVisible

    private fun updateTimeSlotVisibility() {
        val isAnyServiceSelected = _salonServices.value?.any { it.isSelected } ?: false
        _isTimeSlotRecyclerViewVisible.value = isAnyServiceSelected
    }

    fun toggleTimeSlotSelection(selectedTimeSlot: TimeSlot) {
        val currentTimeSlots = _timeSlots.value ?: return
        val updatedTimeSlots = currentTimeSlots.map {
            // Logic for single selection: deselect others when a new one is selected.
            if (it.time == selectedTimeSlot.time) {
                it.copy(isSelected = !it.isSelected) // Toggle the clicked one
            } else {
                it.copy(isSelected = false) // Deselect all others
            }
        }
        _timeSlots.value = updatedTimeSlots
        updateBookButtonVisibility()
    }

    private fun updateBookButtonVisibility() {
        val isAnyServiceSelected = _salonServices.value?.any { it.isSelected } ?: false
        val isAnyTimeSlotSelected = _timeSlots.value?.any { it.isSelected } ?: false
        _isVisibleBookSlotsButton.value = isAnyServiceSelected && isAnyTimeSlotSelected
    }

    // Dummy function for time slots, replace with your logic
    private fun generateTimeSlots(open: String?, close: String?): List<TimeSlot> {
        val openTime = LocalTime.parse(open, DateTimeFormatter.ofPattern("HH:mm"))
        val closeTime = LocalTime.parse(close, DateTimeFormatter.ofPattern("HH:mm"))

        val slots = mutableListOf<TimeSlot>()
        var time = openTime
        while (time < closeTime) {
            slots.add(TimeSlot(
                time = time.format(DateTimeFormatter.ofPattern("hh:mm a"),),
                isAvailable = true,
                isSelected = false
            ))
            time = time.plusMinutes(30)
        }
        return slots
    }
}
