package com.example.glowpoint.domain.model

sealed interface NotificationResult {
    data class Success(val isSuccess: Boolean = false): NotificationResult
    data class Error(val e: Exception): NotificationResult
}