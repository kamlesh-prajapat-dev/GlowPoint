package com.example.glowpoint.ui.screens.auth.register

import com.example.glowpoint.data.models.User
import com.google.firebase.auth.PhoneAuthProvider

sealed interface RegisterUIState {
    object Idle: RegisterUIState
    object Loading: RegisterUIState
    object NotInternet: RegisterUIState
    data class ValidationError(val message: String): RegisterUIState
    object Success: RegisterUIState
    data class Failure(val exception: Exception): RegisterUIState
    data class Verification(val verificationId: String, val token: PhoneAuthProvider.ForceResendingToken, val user: User): RegisterUIState
    data class IsUserExists(val isExists: Boolean): RegisterUIState
}