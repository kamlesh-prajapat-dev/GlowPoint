package com.example.glowpoint.ui.viewmodel

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.glowpoint.data.database.local.LocalDatabase
import com.example.glowpoint.data.models.User
import com.example.glowpoint.domain.repository.AuthRepository
import com.example.glowpoint.domain.model.AuthResult
import com.example.glowpoint.domain.repository.UserRepository
import com.example.glowpoint.util.NetworkUtils
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val localDatabase: LocalDatabase,
    private val networkUtils: NetworkUtils
) : ViewModel() {

    // --- Public Properties ---
    val phoneNumber = MutableLiveData<String>()
    val userName = MutableLiveData<String>()
    val gender = MutableLiveData<String>()

    // --- LiveData States ---
    private val _isRegistered = MutableLiveData(false)
    val isRegistered: LiveData<Boolean> get() = _isRegistered

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _verificationId = MutableLiveData<String?>()
    val verificationId: LiveData<String?> get() = _verificationId

    private val _resendToken = MutableLiveData<PhoneAuthProvider.ForceResendingToken?>()

    // --- LiveData Events ---
    private val _authResult = MutableLiveData<AuthResult?>()
    val authResult: LiveData<AuthResult?> get() = _authResult

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    private val _userCreated = MutableLiveData<Boolean?>()
    val userCreated: LiveData<Boolean?> get() = _userCreated

    // Check Internet is available
    fun isInternetAvailable(): Boolean = networkUtils.isInternetAvailable()

    fun registerUser(activity: Activity) {
        _isLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            val userByPhoneNumber = userRepository.getUserByPhoneNumber(phoneNumber.value)
            if (userByPhoneNumber == null) {
                _isRegistered.postValue(true)
                sendVerificationCode(activity)
            } else {
                _error.postValue("User already registered with this phone number.")
                _isLoading.postValue(false)
            }
        }
    }

    fun loginUser(activity: Activity) {
        _isLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            val userByPhoneNumber = userRepository.getUserByPhoneNumber("+91" + phoneNumber.value)
            if (userByPhoneNumber != null) {
                _isRegistered.postValue(false) // It's a login flow
                sendVerificationCode(activity)
                if (localDatabase.getUser() == null) {
                    localDatabase.setUser(userByPhoneNumber)
                }
                _isLoading.postValue(false)
            } else {
                _error.postValue("User not registered with this phone number.")
                _isLoading.postValue(false)
            }
        }
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
            _isLoading.postValue(false)
            _error.postValue(e.message)
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            _isLoading.postValue(false)
            _verificationId.postValue(verificationId)
            _resendToken.postValue(token)
        }
    }

    fun isLocationAvailable(): Boolean {
        return localDatabase.isLocationSet()
    }

    private fun sendVerificationCode(activity: Activity) {
        val fullPhoneNumber = "+91" + phoneNumber.value
        authRepository.sendVerificationCode(fullPhoneNumber, activity, callbacks)
    }

    fun resendVerificationCode(activity: Activity) {
        _isLoading.postValue(true)
        val fullPhoneNumber = "+91" + phoneNumber.value
        _resendToken.value?.let {
            authRepository.resendVerificationCode(fullPhoneNumber, activity, it, callbacks)
        }
    }

    fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        _isLoading.postValue(true)
        authRepository.signInWithPhoneAuthCredential(credential) { result ->
            _isLoading.postValue(false)
            _authResult.postValue(result)
        }
    }

    fun createProfile() {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser != null) {
            val user = User(
                uid = currentUser.uid,
                name = userName.value,
                gender = gender.value,
                phoneNumber = currentUser.phoneNumber!!
            )
            viewModelScope.launch {
                userRepository.createUser(user) { success ->
                    _userCreated.postValue(success)
                    localDatabase.setUser(user)
                }
            }
        } else {
            _userCreated.postValue(false)
        }
    }

    /**
     * Call this after an event (like auth, user creation, or an error) has been handled in the UI.
     * This prevents the event from being re-triggered on screen rotation.
     */
    fun onAuthEventHandled() {
        _authResult.value = null
        _userCreated.value = null
        _error.value = null
        _verificationId.value = null
    }

    fun reset() {
        _isRegistered.value = false
        _isLoading.value = false
        onAuthEventHandled()
    }
}
