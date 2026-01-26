package com.example.glowpoint.ui.screens.auth.login

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.glowpoint.domain.usecase.AuthUseCase
import com.example.glowpoint.domain.usecase.UserUseCase
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authUseCase: AuthUseCase,
    private val userUseCase: UserUseCase
) : ViewModel() {
    // ----- Language Selection Dialog State -----
    private val _language = MutableStateFlow<String?>(null)
    val language: StateFlow<String?> get() = _language.asStateFlow()

    fun setLanguage() {
        _language.value = authUseCase.getLanguage()
    }

    private val _phoneNumber = MutableStateFlow<String?>(null)
    val phoneNumber: StateFlow<String?> get() = _phoneNumber.asStateFlow()

    private val _uiState = MutableStateFlow<LoginUIState>(LoginUIState.Idle)
    val uiState: StateFlow<LoginUIState> get() = _uiState.asStateFlow()

    fun loginUser(activity: Activity, phoneNumber: String) {
        _uiState.value = LoginUIState.Loading

        if (!authUseCase.isNetworkAvailable()) {
            _uiState.value = LoginUIState.NotInternet
            return
        }

        // Validation
        if (phoneNumber.length != 10 || phoneNumber.isBlank()) {
            _uiState.value = LoginUIState.ValidationError("Please enter valid phone number.")
            return
        }

        _phoneNumber.value = phoneNumber

        viewModelScope.launch(Dispatchers.IO) {
            when(val result = userUseCase.getUserByPhoneNumber("+91$phoneNumber")) {
                is LoginUIState.UserGetSuccess -> {
                    if (result.user != null) {
                        sendVerificationCode(activity, "+91$phoneNumber")
                    } else {
                        _uiState.value = LoginUIState.ValidationError("User not registered with this phone number.")
                    }
                }

                is LoginUIState.Failure -> {
                    _uiState.value = result
                }

                else -> Unit
            }
        }
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
            _uiState.value = LoginUIState.Failure(e)
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            _uiState.value = LoginUIState.Verification(verificationId, token, phoneNumber.value ?: "")
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            authUseCase.signInWithPhoneAuthCredential(credential)
        }
    }

    private fun sendVerificationCode(activity: Activity, phoneNumber: String) {
        val fullPhoneNumber = phoneNumber
        authUseCase.sendVerificationCode(fullPhoneNumber, activity, callbacks)
    }

    fun reset() {
        _uiState.value = LoginUIState.Idle
    }
}