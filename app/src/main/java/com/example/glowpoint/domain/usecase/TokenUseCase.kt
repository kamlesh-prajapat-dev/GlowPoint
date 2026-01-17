package com.example.glowpoint.domain.usecase

import android.util.Log
import com.example.glowpoint.data.local.LocalDatabase
import com.example.glowpoint.domain.model.TokenResult
import com.example.glowpoint.domain.repository.TokenRepository
import javax.inject.Inject

class TokenUseCase @Inject constructor(
    private val tokenRepository: TokenRepository,
    private val localDatabase: LocalDatabase
) {
    suspend fun saveNewToken(token: String) {
        val user = localDatabase.getUser()
        if (user != null) {
            when(val result = tokenRepository.saveFcmToken(userId = user.uid, token = token)) {
                is TokenResult.Success -> {

                }
                is TokenResult.Failure -> {
                    result.e.message?.let { Log.e("TOKEN_USE_CASE", it) }
                }
            }
        }
    }
}