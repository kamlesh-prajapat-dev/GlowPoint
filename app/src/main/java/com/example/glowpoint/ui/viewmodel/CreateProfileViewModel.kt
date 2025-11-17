package com.example.glowpoint.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.glowpoint.data.models.User
import com.example.glowpoint.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _profileCreated = MutableLiveData<Boolean>()
    val profileCreated: LiveData<Boolean> = _profileCreated

    fun createProfile(name: String, email: String) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val user = User(
                uid = currentUser.uid,
                name = name,
                email = email,
                phoneNumber = currentUser.phoneNumber!!
            )
            viewModelScope.launch {
                userRepository.createUser(user) {
                    _profileCreated.value = it
                }
            }
        }
    }
}