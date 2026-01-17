package com.example.glowpoint.domain.model

sealed interface TokenResult {
    data class Success(val token: String): TokenResult
    data class Failure(val e: Exception): TokenResult
}