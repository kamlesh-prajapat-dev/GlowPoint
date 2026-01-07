package com.example.glowpoint.domain.repository

import com.example.glowpoint.data.models.User
import com.example.glowpoint.domain.model.UserResult
import com.example.glowpoint.domain.model.AuthResult

interface UserRepository {
    suspend fun createUser(user: User): UserResult
    suspend fun getUserByPhoneNumber(phoneNumber: String): UserResult
}