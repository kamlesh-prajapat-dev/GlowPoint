package com.example.glowpoint.domain.repository

import java.lang.Exception

sealed class AuthResult {
    data class Success(val isNewUser: Boolean) : AuthResult()
    data class Failure(val exception: Exception) : AuthResult()
}