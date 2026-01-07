package com.example.glowpoint.ui.screens.auth.login

import com.example.glowpoint.data.models.User
import com.google.firebase.auth.PhoneAuthProvider

sealed interface LoginUIState {
    object Idle: LoginUIState
    object Loading: LoginUIState
    data class Verification(val verificationId: String, val token: PhoneAuthProvider.ForceResendingToken, val phoneNumber: String): LoginUIState
    data class Failure(val e: Exception): LoginUIState
    object Success: LoginUIState
    data class ValidationError(val message: String): LoginUIState
    object NotInternet: LoginUIState
    data class UserGetSuccess(val user: User?): LoginUIState
}