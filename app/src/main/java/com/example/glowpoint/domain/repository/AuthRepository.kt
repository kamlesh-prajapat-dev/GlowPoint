package com.example.glowpoint.domain.repository

import android.app.Activity
import com.example.glowpoint.domain.model.AuthResult
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider

interface AuthRepository {
    fun sendVerificationCode(
        phoneNumber: String,
        activity: Activity,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    )

    fun resendVerificationCode(
        phoneNumber: String,
        activity: Activity,
        token: PhoneAuthProvider.ForceResendingToken,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    )

    fun getCurrentUser(): FirebaseUser?

    suspend fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential): AuthResult

    fun isUserLoggedIn(): Boolean
    fun logout()
}