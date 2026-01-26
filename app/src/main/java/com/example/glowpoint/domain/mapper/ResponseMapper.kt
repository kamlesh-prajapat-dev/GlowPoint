package com.example.glowpoint.domain.mapper

import com.example.glowpoint.data.models.LocationSuggestion
import com.example.glowpoint.data.models.api.NominatimResponse
import com.example.glowpoint.data.models.api.PhotonFeature

fun NominatimResponse.toLocationSuggestion(): LocationSuggestion {
    val safeLat = lat.toDoubleOrNull()
    val safeLon = lon.toDoubleOrNull()

    require(safeLat != null && safeLon != null) {
        "Invalid coordinates from Nominatim"
    }

    return LocationSuggestion(
        name = displayName.substringBefore(","),
        details = displayName,
        latitude = safeLat,
        longitude = safeLon
    )
}

fun PhotonFeature.toLocationSuggestion(): LocationSuggestion {
    val lat = geometry.coordinates.getOrNull(1)
    val lon = geometry.coordinates.getOrNull(0)

    return LocationSuggestion(
        name = properties.name ?: properties.city ?: "Unknown place",
        details = listOfNotNull(
            properties.name,
            properties.city,
            properties.country
        ).joinToString(", "),
        latitude = lat ?: 0.0,
        longitude = lon ?: 0.0
    )
}
