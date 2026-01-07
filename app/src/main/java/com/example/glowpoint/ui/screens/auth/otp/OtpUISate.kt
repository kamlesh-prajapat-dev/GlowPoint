package com.example.glowpoint.ui.screens.auth.otp

import com.example.glowpoint.data.models.User
import com.google.firebase.auth.PhoneAuthProvider

sealed interface OtpUISate {
    object Idle: OtpUISate
    object Loading: OtpUISate
    data class Failure(val e: Exception): OtpUISate
    data class Success(val isNewUser: Boolean): OtpUISate
    data class ValidationError(val message: String): OtpUISate
    object NoInternet: OtpUISate
    data class CreateUserSuccess(val user: User?): OtpUISate
    data class Verification(val verificationId: String, val token: PhoneAuthProvider.ForceResendingToken): OtpUISate
}