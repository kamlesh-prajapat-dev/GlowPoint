package com.example.glowpoint.domain.model

data class ServiceItem(
    val id: String? = null,
    val name: String? = null,
    val description: String? = null,
    val isSelected: Boolean = false,
    val price: Float? = null,
    val duration: String? = "30 min",
    val genderCategory: Boolean = false // Men - true and women - false
)
