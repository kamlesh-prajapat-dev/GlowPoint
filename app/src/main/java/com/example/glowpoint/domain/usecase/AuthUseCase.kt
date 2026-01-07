package com.example.glowpoint.domain.usecase

import android.app.Activity
import com.example.glowpoint.data.local.LocalDatabase
import com.example.glowpoint.domain.repository.AuthRepository
import com.example.glowpoint.domain.repository.UserRepository
import com.example.glowpoint.domain.model.AuthResult
import com.example.glowpoint.ui.screens.auth.login.LoginUIState
import com.example.glowpoint.ui.screens.auth.otp.OtpUISate
import com.example.glowpoint.ui.screens.auth.register.RegisterUIState
import com.example.glowpoint.util.NetworkUtils
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import javax.inject.Inject

class AuthUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val localDatabase: LocalDatabase,
    private val networkUtils: NetworkUtils
) {
    // Splash method
    fun getLocationName(): String {
        return localDatabase.getLocationName()
    }

    fun isUserLoggedIn(): Boolean {
        return authRepository.isUserLoggedIn()
    }

    // Login method and Register method
    fun getLanguage() = localDatabase.getLanguage()

    fun isNetworkAvailable() = networkUtils.isInternetAvailable()

    fun isLocationSet() = localDatabase.isLocationSet()

    fun sendVerificationCode(phoneNumber: String, activity: Activity, callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks) {
        authRepository.sendVerificationCode(phoneNumber, activity, callbacks)
    }

    fun resendVerificationCode(phoneNumber: String, activity: Activity, token: PhoneAuthProvider.ForceResendingToken, callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks) {
        authRepository.resendVerificationCode(phoneNumber, activity, token, callbacks)
    }

    fun getCurrentUser() = authRepository.getCurrentUser()

    suspend fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential): LoginUIState {
        return when(val result = authRepository.signInWithPhoneAuthCredential(credential)) {
            is AuthResult.Success -> {
                LoginUIState.Success
            }

            is AuthResult.Failure -> {
                LoginUIState.Failure(result.exception)
            }
        }
    }

    suspend fun signInWithPhoneAuthCredentialRegister(credential: PhoneAuthCredential): RegisterUIState {
        return when(val result = authRepository.signInWithPhoneAuthCredential(credential)) {
            is AuthResult.Success -> {
                RegisterUIState.Success
            }
            is AuthResult.Failure -> {
                RegisterUIState.Failure(result.exception)
            }
        }
    }

    suspend fun singInWithPhoneAuthCredentialOtp(credential: PhoneAuthCredential): OtpUISate {
        return when(val result = authRepository.signInWithPhoneAuthCredential(credential)) {
            is AuthResult.Success -> {
                OtpUISate.Success(result.isNewUser)
            }

            is AuthResult.Failure -> {
                OtpUISate.Failure(result.exception)
            }
        }
    }
}