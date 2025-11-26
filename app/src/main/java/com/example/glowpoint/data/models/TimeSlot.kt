package com.example.glowpoint.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class TimeSlot(
    val time: String,
    val isAvailable: Boolean,
    var isSelected: Boolean = false
)
