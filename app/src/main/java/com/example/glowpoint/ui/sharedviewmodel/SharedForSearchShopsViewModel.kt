package com.example.glowpoint.ui.sharedviewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.glowpoint.data.models.ServiceItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SharedForSearchShopsViewModel @Inject constructor(): ViewModel() {
    private val _listOfServiceId = MutableStateFlow<List<ServiceItem>?>(null)
    val listOfServiceId: StateFlow<List<ServiceItem>?> get() = _listOfServiceId.asStateFlow()
    fun setListOfServiceId(listOfServiceId: List<ServiceItem>?) {
        _listOfServiceId.value = listOfServiceId
    }
    fun reset() {
        _listOfServiceId.value = null
    }
}