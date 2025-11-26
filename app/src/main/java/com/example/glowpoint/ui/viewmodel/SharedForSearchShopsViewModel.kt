package com.example.glowpoint.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SharedForSearchShopsViewModel @Inject constructor(): ViewModel() {

    private val _isNavigate = MutableLiveData<Boolean>()
    val isNavigate: LiveData<Boolean> get() = _isNavigate
    private val _listOfServiceId = MutableLiveData<List<Map<String?, Boolean>>?>()
    val listOfServiceId: LiveData<List<Map<String?, Boolean>>?> get() = _listOfServiceId
    fun setListOfServiceId(listOfServiceId: List<Map<String?, Boolean>>?) {
        _listOfServiceId.value = listOfServiceId
        _isNavigate.value = true
    }
    fun reset() {
        _isNavigate.value = false
    }
}