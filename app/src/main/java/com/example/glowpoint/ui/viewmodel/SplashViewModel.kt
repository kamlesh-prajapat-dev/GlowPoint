package com.example.glowpoint.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.glowpoint.data.database.local.LocalDatabase
import com.example.glowpoint.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val localDatabase: LocalDatabase
) : ViewModel() {
    fun getLocation() = localDatabase.getLocationName()

    fun isUserLoggedIn(): Boolean {
        return repository.isUserLoggedIn()
    }
}