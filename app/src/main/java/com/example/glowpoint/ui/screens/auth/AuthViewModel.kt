package com.example.glowpoint.ui.screens.auth

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.glowpoint.data.models.User
import com.example.glowpoint.domain.usecase.AuthUseCase
import com.example.glowpoint.domain.usecase.UserUseCase
import com.example.glowpoint.ui.screens.auth.otp.OtpUISate
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
class AuthViewModel @Inject constructor(
    private val authUseCase: AuthUseCase,
    private val userUseCase: UserUseCase
) : ViewModel() {
    // --- For set location ---
    private val _isLocationSet = MutableStateFlow(false)
    val isLocationSet: StateFlow<Boolean> get() = _isLocationSet.asStateFlow()

    init {
        viewModelScope.launch {
            _isLocationSet.value = authUseCase.isLocationSet()
        }
    }

    // --- For Authentication that all initialize from Login OR Register Fragment ---
    private val _phoneNumber = MutableStateFlow<String?>(null)
    val phoneNumber: StateFlow<String?> get() = _phoneNumber.asStateFlow()

    private val _user = MutableStateFlow<User?>(null)
    private val _isRegistered = MutableStateFlow<Boolean?>(null)
    val isRegistered: StateFlow<Boolean?> get() = _isRegistered.asStateFlow()
    private val _verificationId = MutableStateFlow<String?>(null)
    val verificationId: StateFlow<String?> get() = _verificationId.asStateFlow()
    private val _resendToken = MutableStateFlow<PhoneAuthProvider.ForceResendingToken?>(null)

    fun setLoginData(
        phoneNumber: String,
        verificationId: String,
        token: PhoneAuthProvider.ForceResendingToken,
        isRegistered: Boolean
    ) {
        _phoneNumber.value = phoneNumber
        _verificationId.value = verificationId
        _resendToken.value = token
        _isRegistered.value = isRegistered
    }

    fun setRegisterData(
        user: User,
        isRegistered: Boolean,
        verificationId: String,
        token: PhoneAuthProvider.ForceResendingToken
    ) {
        _user.value = user
        _isRegistered.value = isRegistered
        _verificationId.value = verificationId
        _resendToken.value = token
    }

    fun onSetVerificationIdAndToken(
        verificationId: String,
        token: PhoneAuthProvider.ForceResendingToken
    ) {
        _verificationId.value = verificationId
        _resendToken.value = token
    }


    // --- For Authentication that all initialize from OTP Fragment ---
    private val _uiState = MutableStateFlow<OtpUISate>(OtpUISate.Idle)
    val uiState: StateFlow<OtpUISate> get() = _uiState.asStateFlow()

    fun verifyOtp(otp: String) {
        _uiState.value = OtpUISate.Loading

        if (!authUseCase.isNetworkAvailable()) {
            _uiState.value = OtpUISate.NoInternet
            return
        }

        if (otp.length != 6) {
            _uiState.value = OtpUISate.ValidationError("Invalid OTP")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val verificationId = verificationId.value
            if (verificationId != null) {
                val credential = PhoneAuthProvider.getCredential(verificationId, otp)
                signInWithPhoneAuthCredential(credential)
            }
        }
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            _uiState.value = OtpUISate.Failure(e)
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            _uiState.value = OtpUISate.Verification(verificationId, token)
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            _uiState.value = authUseCase.singInWithPhoneAuthCredentialOtp(credential)
        }
    }


    fun resendVerificationCode(activity: Activity) {
        _uiState.value = OtpUISate.Loading
        val fullPhoneNumber = "+91" + phoneNumber.value
        _resendToken.value?.let {
            authUseCase.resendVerificationCode(fullPhoneNumber, activity, it, callbacks)
        }
    }

    fun createProfile() {
        _uiState.value = OtpUISate.Loading

        val currentUser = authUseCase.getCurrentUser()
        if (currentUser != null) {
            val user = _user.value?.copy(
                uid = currentUser.uid,
                phoneNumber = currentUser.phoneNumber!!,
            )
            if (user != null) {
                viewModelScope.launch {
                    _uiState.value = userUseCase.createUser(user)
                }
            } else {
                _uiState.value = OtpUISate.ValidationError("User not here.")
            }
        } else {
            _uiState.value = OtpUISate.Failure(Exception("User not logged in"))
        }
    }

    fun onAuthEventHandled() {
        _uiState.value = OtpUISate.Idle
        _verificationId.value = null
    }
}