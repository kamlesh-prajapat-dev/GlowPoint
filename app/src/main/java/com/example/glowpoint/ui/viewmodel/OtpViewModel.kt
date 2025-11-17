package com.example.glowpoint.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.glowpoint.domain.repository.AuthRepository
import com.example.glowpoint.domain.repository.AuthResult
import com.google.firebase.auth.PhoneAuthCredential
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OtpViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _authResult = MutableLiveData<AuthResult>()
    val authResult: LiveData<AuthResult> = _authResult

    fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        repository.signInWithPhoneAuthCredential(credential) {
            _authResult.value = it
        }
    }
}