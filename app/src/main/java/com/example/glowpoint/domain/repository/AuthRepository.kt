package com.example.glowpoint.domain.repository

import android.app.Activity
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider

interface AuthRepository {
    fun sendVerificationCode(phoneNumber: String, activity: Activity, callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks)
    fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential, onResult: (AuthResult) -> Unit)
    fun isUserLoggedIn(): Boolean
    fun logout()
}