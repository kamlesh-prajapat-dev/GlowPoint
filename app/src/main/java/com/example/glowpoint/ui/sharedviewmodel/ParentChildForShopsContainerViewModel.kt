package com.example.glowpoint.ui.sharedviewmodel

import androidx.lifecycle.ViewModel
import com.example.glowpoint.data.models.ServiceItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ParentChildForShopsContainerViewModel: ViewModel() {
    private val _listOfServiceId = MutableStateFlow<List<ServiceItem>?>(null)
    val listOfServiceId: StateFlow<List<ServiceItem>?> get() = _listOfServiceId.asStateFlow()

    fun setListOfServiceId(listOfServiceId: List<ServiceItem>?) {
        _listOfServiceId.value = listOfServiceId
    }

    fun reset() {
        _listOfServiceId.value = null
    }
}