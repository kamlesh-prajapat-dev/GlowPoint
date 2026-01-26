package com.example.glowpoint.ui.screens.auth.register

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.glowpoint.data.models.User
import com.example.glowpoint.domain.usecase.AuthUseCase
import com.example.glowpoint.domain.usecase.UserUseCase
import com.google.firebase.FirebaseException
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
class RegisterViewModel @Inject constructor(
    private val authUseCase: AuthUseCase,
    private val userUseCase: UserUseCase
) : ViewModel() {

    // ---------- Input Field ----------
    private val _phoneNumber = MutableStateFlow<String?>(null)
    private val _userName = MutableStateFlow<String?>(null)
    private val _gender = MutableStateFlow<String?>(null)
    val gender: StateFlow<String?> get() = _gender.asStateFlow()

    fun onSetGender(gender: String) {
        _gender.value = gender
    }

    // ---------- UI State ----------
    private val _uiState = MutableStateFlow<RegisterUIState>(RegisterUIState.Idle)
    val uiState: StateFlow<RegisterUIState> get() = _uiState.asStateFlow()

    fun registerUser(activity: Activity, phoneNumber: String, userName: String) {
        _uiState.value = RegisterUIState.Loading

        if (!authUseCase.isNetworkAvailable()) {
            _uiState.value = RegisterUIState.NotInternet
            return
        }

        // Validation
        val phoneNumberError = validatePhoneNumber(phoneNumber)
        val nameError = validateName(userName)
        val genderError = validateGender(_gender.value ?: "")

        if (phoneNumberError != null || nameError != null || genderError != null) {
            _uiState.value = RegisterUIState.ValidationError(msgForNumber = phoneNumberError, msgForName = nameError, msgForGender = genderError)
            return
        }

        _phoneNumber.value = phoneNumber
        _userName.value = userName

        viewModelScope.launch(Dispatchers.IO) {
            when (val result = userUseCase.checkUserByPhoneNumber("+91$phoneNumber")) {
                is RegisterUIState.IsUserExists -> {
                    if (!result.isExists) {
                        sendVerificationCode(activity, "+91$phoneNumber")
                    } else {
                        _uiState.value =
                            RegisterUIState.ValidationError(msgForNumber = "User already registered with this phone number.")
                    }
                }

                is RegisterUIState.Failure -> {
                    _uiState.value = result
                }

                else -> Unit
            }
        }
    }

    private fun sendVerificationCode(activity: Activity, phoneNumber: String) {
        authUseCase.sendVerificationCode(phoneNumber, activity, callbacks)
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            _uiState.value = RegisterUIState.Failure(e)
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            _uiState.value = RegisterUIState.Verification(
                verificationId,
                token,
                user = User(
                    uid = "",
                    name = _userName.value ?: "",
                    gender = _gender.value ?: "",
                    phoneNumber = _phoneNumber.value ?: ""
                )
            )
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            _uiState.value = authUseCase.signInWithPhoneAuthCredentialRegister(credential)
        }
    }

    private fun validatePhoneNumber(phoneNumber: String): String? {
        if (phoneNumber.length != 10 || phoneNumber.isBlank()) {
            return "Please enter valid phone number."
        }
        return null
    }

    private fun validateName(name: String): String? {
        if (name.isBlank()) {
            return "Please enter your name."
        }
        return null
    }

    private fun validateGender(gender: String): String? {
        if (gender.isBlank()) {
            return "Please select your gender."
        }
        return null
    }

    fun reset() {
        _uiState.value = RegisterUIState.Idle
    }
}