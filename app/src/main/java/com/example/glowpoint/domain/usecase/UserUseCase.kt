package com.example.glowpoint.domain.usecase

import android.content.Context
import android.location.Geocoder
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.glowpoint.data.local.LocalDatabase
import com.example.glowpoint.data.models.User
import com.example.glowpoint.domain.model.TokenResult
import com.example.glowpoint.domain.model.UserResult
import com.example.glowpoint.domain.repository.TokenRepository
import com.example.glowpoint.domain.repository.UserRepository
import com.example.glowpoint.ui.screens.auth.login.LoginUIState
import com.example.glowpoint.ui.screens.auth.otp.OtpUISate
import com.example.glowpoint.ui.screens.auth.register.RegisterUIState
import com.example.glowpoint.ui.screens.location.LocationUIState
import com.example.glowpoint.util.TokenManager
import javax.inject.Inject

class UserUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val tokenRepository: TokenRepository,
    private val localDatabase: LocalDatabase
) {
    suspend fun getUserByPhoneNumber(phoneNumber: String): LoginUIState {
        return when(val result = userRepository.getUserByPhoneNumber(phoneNumber)) {
            is UserResult.Success -> {
                val user = result.user
                if (user != null) {
                    localDatabase.setUser(user = user)
                    val token = TokenManager.getFCMToken()
                    if (token != null) {
                        when(val result = tokenRepository.saveFcmToken(userId = user.uid, token = token)) {
                            is TokenResult.Success -> {

                            }
                            is TokenResult.Failure -> {

                            }
                        }
                    }
                }
                LoginUIState.UserGetSuccess(result.user)
            }
            is UserResult.Failure -> {
                LoginUIState.Failure(result.exception)
            }
        }
    }

    suspend fun checkUserByPhoneNumber(phoneNumber: String): RegisterUIState {
        return when(val result = userRepository.getUserByPhoneNumber(phoneNumber)) {
            is UserResult.Success -> {
                if (result.user != null) {
                    RegisterUIState.IsUserExists(true)
                } else {
                    RegisterUIState.IsUserExists(false)
                }
            }
            is UserResult.Failure -> {
                RegisterUIState.Failure(result.exception)
            }
        }
    }

    suspend fun createUser(user: User): OtpUISate {
        return when(val result = userRepository.createUser(user)) {
            is UserResult.Success -> {
                localDatabase.setUser(user)
                val token = TokenManager.getFCMToken()
                if (token != null) {
                    when(val result = tokenRepository.saveFcmToken(userId = user.uid, token = token)) {
                        is TokenResult.Failure -> {

                        }

                        is TokenResult.Success -> {

                        }
                    }
                } else {
                    OtpUISate.Failure(Exception("Token not create due to failure."))
                }
                OtpUISate.CreateUserSuccess(result.user)
            }

            is UserResult.Failure -> {
                OtpUISate.Failure(result.exception)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun saveLocation(latitude: Double, longitude: Double, context: Context, onResult: (LocationUIState) -> Unit) {
        localDatabase.latitude = latitude.toFloat()
        localDatabase.longitude = longitude.toFloat()

        val geocoder = Geocoder(context)
        try {
            geocoder.getFromLocation(latitude, longitude, 1) {
                if (it.isNotEmpty()) {
                    localDatabase.setLocationName(it[0].locality)
                    localDatabase.setAreaOfUser(it[0].subLocality)
                    onResult(LocationUIState.Success(true))
                } else {
                    onResult(LocationUIState.Failure(Exception("No location found")))
                }
            }
        } catch (e: Exception) {
            onResult(LocationUIState.Failure(e))
        }
    }
}