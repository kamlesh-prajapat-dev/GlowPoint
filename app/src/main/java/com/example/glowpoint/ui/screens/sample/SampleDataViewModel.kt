package com.example.glowpoint.ui.screens.sample

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.glowpoint.data.sample.NewShopDetails
import com.example.glowpoint.data.sample.ServiceModel
import com.example.glowpoint.domain.repository.SalonRepository
import com.example.glowpoint.domain.repository.SalonServiceRepository
import com.example.glowpoint.util.SalonServicesRepositoryConstant
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SampleDataViewModel @Inject constructor(
    private val repository: SalonServiceRepository,
    private val shopRepository: SalonRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<SampleDataUIState>(SampleDataUIState.Idle)
    val uiState: StateFlow<SampleDataUIState> get() = _uiState.asStateFlow()

    fun saveMenServices(services: List<ServiceModel>) {
        _uiState.value = SampleDataUIState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.createServices(services, SalonServicesRepositoryConstant.MEN_SERVICES_COLLECTION)
            _uiState.value = result
        }
    }

    fun saveWomenServices(services: List<ServiceModel>) {
        _uiState.value = SampleDataUIState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.createServices(services, SalonServicesRepositoryConstant.WOMEN_SERVICES_COLLECTION)
            _uiState.value = result
        }
    }

    fun saveShopData(salonShops: List<NewShopDetails>) {
        _uiState.value = SampleDataUIState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            val result = shopRepository.saveShopData(salonShops)
            _uiState.value = result
        }
    }
}