package com.example.glowpoint.data.models.api

import kotlinx.serialization.Serializable

@Serializable
data class NotificationRequest(
    val token: String,
    val title: String,
    val body: String,
    val bookingId: String
)