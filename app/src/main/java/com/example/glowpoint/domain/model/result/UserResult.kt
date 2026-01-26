package com.example.glowpoint.domain.model.result

import com.example.glowpoint.data.models.User

sealed interface UserResult {
    data class Success(val user: User?) : UserResult
    data class Failure(val exception: Exception) : UserResult
}