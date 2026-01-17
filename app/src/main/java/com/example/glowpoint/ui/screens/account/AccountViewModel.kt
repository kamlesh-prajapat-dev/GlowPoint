package com.example.glowpoint.ui.screens.account

import androidx.lifecycle.ViewModel
import com.example.glowpoint.data.models.User
import com.example.glowpoint.domain.usecase.AuthUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val authUseCase: AuthUseCase,

) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> get() = _user.asStateFlow()

    fun loadUser() {
        _user.value = authUseCase.getCachedUser()
    }

    fun logout() {
        authUseCase.logout()
    }
}