package com.example.glowpoint.data.models.api


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NominatimResponse(
    @SerialName("display_name")
    val displayName: String,
    @SerialName("lat")
    val lat: String,
    @SerialName("lon")
    val lon: String
)

