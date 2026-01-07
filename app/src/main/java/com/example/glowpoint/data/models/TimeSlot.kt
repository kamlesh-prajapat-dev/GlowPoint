package com.example.glowpoint.data.models

data class TimeSlot(
    val time: String = "",
    val isAvailable: Boolean = false,
    var isSelected: Boolean = false
)