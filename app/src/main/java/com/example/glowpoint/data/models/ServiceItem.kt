package com.example.glowpoint.data.models

data class ServiceItem(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val isSelected: Boolean = false,
    val price: Float = 0.0f,
    val duration: String = "30 min",
    val genderCategory: Boolean = false // Men - true and women - false
)