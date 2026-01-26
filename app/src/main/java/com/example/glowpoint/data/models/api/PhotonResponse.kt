package com.example.glowpoint.data.models.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PhotonResponse(
    @SerialName("features")
    val features: List<PhotonFeature> = emptyList()
)

@Serializable
data class PhotonFeature(
    @SerialName("properties")
    val properties: PhotonProperties,
    @SerialName("geometry")
    val geometry: PhotonGeometry
)

@Serializable
data class PhotonProperties(
    @SerialName("name")
    val name: String? = null,
    @SerialName("city")
    val city: String? = null,
    @SerialName("country")
    val country: String? = null
)

@Serializable
data class PhotonGeometry(
    @SerialName("coordinates")
    val coordinates: List<Double> = emptyList() // [lon, lat]
)