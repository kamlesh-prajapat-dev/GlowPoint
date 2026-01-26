package com.example.glowpoint.domain.usecase

import com.example.glowpoint.data.local.LocalDatabase
import com.example.glowpoint.data.models.LocationSuggestion
import com.example.glowpoint.data.models.ServiceItem
import com.example.glowpoint.data.models.ShopDetails
import com.example.glowpoint.domain.mapper.FirestoreFailureMapper
import com.example.glowpoint.domain.mapper.toGetReqDomainFailure
import com.example.glowpoint.domain.model.result.FetchSalonsResult
import com.example.glowpoint.domain.repository.SalonRepository
import com.example.glowpoint.ui.screens.components.shops.ShopContainerUIState
import com.example.glowpoint.ui.screens.eachshop.EachShopUIState
import com.example.glowpoint.ui.screens.location.LocationUIState
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class ShopUseCase @Inject constructor(
    private val shopRepository: SalonRepository,
    private val localDatabase: LocalDatabase
) {
    private val earthRadius = 6371000.0 // Earth radius in meters
    private val radiusInMeters: Double = 5000.0

    fun observeTimeSlot(salonId: String, date: Long, openTime: String, closeTime: String): Flow<EachShopUIState> {
        return shopRepository.observeTimeSlot(salonId, date, openTime, closeTime)
            .map { result ->
                when (result) {
                    is FetchSalonsResult.GetTimeSlotSuccess -> {
                        EachShopUIState.Success(result.timeSlots)
                    }

                    is FetchSalonsResult.Failure -> EachShopUIState.Failure(result.exception.toGetReqDomainFailure(salonId))

                    else -> EachShopUIState.Idle
                }
            }.catch {
                emit(EachShopUIState.Failure(it.toGetReqDomainFailure(salonId)))
            }
    }

    fun loadMenServices() = localDatabase.getMenServices()

    fun loadWomenServices() = localDatabase.getWomenServices()

    suspend fun getNearBySalon(
        suggestion: LocationSuggestion
    ): LocationUIState {
        val centerLat = suggestion.latitude
        val centerLng = suggestion.longitude
        val bounds = GeoFireUtils.getGeoHashQueryBounds(
            GeoLocation(centerLat, centerLng),
            radiusInMeters
        )

        return when (val result = shopRepository.getNearBySalon(centerLat, centerLng, bounds)) {
            is FetchSalonsResult.Success -> {
                val nearBySalons = result.salons
                    .mapNotNull { salon ->
                        val location = salon.location ?: return@mapNotNull null

                        val distance = haversineDistance(
                            centerLat,
                            centerLng,
                            location.latitude,
                            location.longitude
                        )

                        salon.copy(distance = distance)
                    }
                    .sortedBy { it.distance }
                if (nearBySalons.isNotEmpty()) {
                    localDatabase.setSalonModel(nearBySalons)
                    localDatabase.lastCacheTimestampOfSalons = System.currentTimeMillis()
                }

                LocationUIState.GetNearBySalonSuccess(suggestion)
            }
            is FetchSalonsResult.Failure -> {
                LocationUIState.Failure(FirestoreFailureMapper.map(result.exception, suggestion))
            }

            is FetchSalonsResult.NotServiceable -> {
                localDatabase.setSalonModel(emptyList())
                localDatabase.lastCacheTimestampOfSalons = System.currentTimeMillis()
                LocationUIState.GetNearBySalonSuccess(suggestion)
            }
            else -> LocationUIState.Idle
        }
    }

    suspend fun getNearBySalon(
        centerLat: Double,
        centerLng: Double,
        selectedServices: List<ServiceItem>? = null
    ): ShopContainerUIState {

        val bounds = GeoFireUtils.getGeoHashQueryBounds(
            GeoLocation(centerLat, centerLng),
            radiusInMeters
        )

        return when (val result = shopRepository.getNearBySalon(centerLat, centerLng, bounds)) {

            is FetchSalonsResult.Success -> {
                val nearBySalons = result.salons
                    .mapNotNull { salon ->
                        val location = salon.location ?: return@mapNotNull null

                        val distance = haversineDistance(
                            centerLat,
                            centerLng,
                            location.latitude,
                            location.longitude
                        )

                        salon.copy(distance = distance)
                    }
                    .sortedBy { it.distance }

                val salons = if (selectedServices != null) {
                    val genderPreference = checkGender(selectedServices)
                    val requestedServices = selectedServices.map { it.id }
                    nearBySalons.filter { salon ->
                        when (genderPreference) {
                            0 -> salon.gender == "Male" || salon.gender == "Both"
                            1 -> salon.gender == "Female" || salon.gender == "Both"
                            else -> salon.gender == "Both"
                        }
                    }.filter { salon ->
                        requestedServices.any { it in salon.offered_services }
                    }
                } else {
                    nearBySalons
                }
                if (salons.isNotEmpty()) {
                    localDatabase.setSalonModel(nearBySalons)
                    localDatabase.lastCacheTimestampOfSalons = System.currentTimeMillis()
                }
                ShopContainerUIState.Success(salons)
            }

            is FetchSalonsResult.Failure -> {
                ShopContainerUIState.Failure(FirestoreFailureMapper.map(result.exception, "Shops"))
            }

            is FetchSalonsResult.NotServiceable -> {
                ShopContainerUIState.NotServiceable
            }

            else -> ShopContainerUIState.Idle
        }
    }

    private fun checkGender(selectedServices: List<ServiceItem>): Int {
        return when {
            selectedServices.all { it.genderCategory } -> 0        // All true → Male
            selectedServices.all { !it.genderCategory } -> 1       // All false → Female
            else -> 2                         // Mixed → Both
        }
    }

    private fun haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(lat1)) *
                cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2.0)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }

    fun loadSalonFromCache(selectedServices: List<ServiceItem>? = null): List<ShopDetails> {
        val salons = localDatabase.getSalonModel()
        return if (selectedServices != null) {
            val genderPreference = checkGender(selectedServices)
            val requestedServices = selectedServices.map { it.id }
            salons.filter { salon ->
                when (genderPreference) {
                    0 -> salon.gender == "Male" || salon.gender == "Both"
                    1 -> salon.gender == "Female" || salon.gender == "Both"
                    else -> salon.gender == "Both"
                }
            }.filter { salon ->
                requestedServices.any { it in salon.offered_services }
            }
        } else {
            salons
        }
    }

    fun getUser() = localDatabase.getUser()
}