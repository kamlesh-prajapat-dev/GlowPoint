package com.example.glowpoint.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ParentChildForShopsContainerViewModel: ViewModel() {
    private val _isParentHomeNavigate = MutableLiveData<Boolean>()
    val isParentHomeNavigate: LiveData<Boolean> get() = _isParentHomeNavigate

    private val _isParentShopsNavigate = MutableLiveData<Boolean>()
    val isParentShopsNavigate: LiveData<Boolean> get() = _isParentShopsNavigate

    private val _listOfServiceId = MutableLiveData<List<Map<String?, Boolean>>?>()
    val listOfServiceId: LiveData<List<Map<String?, Boolean>>?> get() = _listOfServiceId

    fun setParentHomeNavigate() {
        _isParentHomeNavigate.value = true
    }

    fun setParentShopsNavigate(listOfServiceId: List<Map<String?, Boolean>>?) {
        _isParentShopsNavigate.value = true
        _listOfServiceId.value = listOfServiceId
    }

    fun reset() {
        _isParentHomeNavigate.value = false
        _isParentShopsNavigate.value = false
    }
}