package com.example.glowpoint.ui.viewmodel

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.glowpoint.domain.repository.AuthRepository
import com.example.glowpoint.domain.repository.AuthResult
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    val phoneNumber = MutableLiveData<String>()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _verificationId = MutableLiveData<String?>()
    val verificationId: LiveData<String?> = _verificationId

    private val _resendToken = MutableLiveData<PhoneAuthProvider.ForceResendingToken?>()
    val resendToken: LiveData<PhoneAuthProvider.ForceResendingToken?> = _resendToken

    private val _authResult = MutableLiveData<AuthResult>()
    val authResult: LiveData<AuthResult> = _authResult

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
            _isLoading.value = false
            _error.value = e.message
        }

        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
            _isLoading.value = false
            _verificationId.value = verificationId
            _resendToken.value = token
        }
    }

    fun sendVerificationCode(activity: Activity) {
        _isLoading.value = true
        val fullPhoneNumber = "+91" + phoneNumber.value
        repository.sendVerificationCode(fullPhoneNumber, activity, callbacks)
    }

    fun resendVerificationCode(activity: Activity) {
        _isLoading.value = true
        val fullPhoneNumber = "+91" + phoneNumber.value
        resendToken.value?.let {
            repository.resendVerificationCode(fullPhoneNumber, activity, it, callbacks)
        }
    }

    fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        _isLoading.value = true
        repository.signInWithPhoneAuthCredential(credential) {
            _isLoading.value = false
            _authResult.value = it
        }
    }

    fun onNavigationComplete() {
        _verificationId.value = null
    }
}