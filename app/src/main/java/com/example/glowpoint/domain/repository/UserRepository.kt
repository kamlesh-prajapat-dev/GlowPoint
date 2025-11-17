package com.example.glowpoint.domain.repository

import com.example.glowpoint.data.models.User

interface UserRepository {
    suspend fun createUser(user: User, onResult: (Boolean) -> Unit)
}