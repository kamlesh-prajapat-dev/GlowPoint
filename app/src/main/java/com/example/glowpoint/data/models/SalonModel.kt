package com.example.glowpoint.data.models

import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.PropertyName

data class SalonModel(
    val id: String? = "",
    val name: String? = "",
    val address: String? = "",
    val phone: String? = "",
    val rating: Double? = 0.0,
    val gender: String? = "",
    val isOpen: Boolean? = false,
    val openTime: String? = "",
    val closeTime: String? = "",
    val geoHash: String? = "",
    val distance: Double? = 0.0,

    val location: GeoPoint? = null,

    val offered_services: List<String>? = emptyList()
)