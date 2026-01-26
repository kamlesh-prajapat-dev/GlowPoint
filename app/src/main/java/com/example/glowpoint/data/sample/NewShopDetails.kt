package com.example.glowpoint.data.sample

import com.google.firebase.firestore.GeoPoint

data class NewShopDetails(
    val name: String = "",
    val address: String = "",
    val phone: String = "",
    val rating: Double = 0.0,
    val gender: String = "",
    val isOpen: Boolean = false,
    val openTime: String = "",
    val closeTime: String = "",
    val geoHash: String = "",
    val location: GeoPoint? = null,
    val offered_services: List<String> = emptyList()
)