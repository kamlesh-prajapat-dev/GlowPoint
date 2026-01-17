package com.example.glowpoint.domain.usecase

import com.example.glowpoint.data.local.LocalDatabase
import com.example.glowpoint.domain.model.SalonServicesResult
import com.example.glowpoint.domain.repository.SalonServiceRepository
import com.example.glowpoint.ui.screens.components.services.ServiceContainerUIState
import com.example.glowpoint.util.SalonServicesRepositoryConstant
import javax.inject.Inject

class SalonServiceUseCase @Inject constructor(
    private val salonServiceRepository: SalonServiceRepository,
    private val localDatabase: LocalDatabase
) {
    suspend fun getMenSalonServices(): ServiceContainerUIState {
        return when(val result = salonServiceRepository.getServices(SalonServicesRepositoryConstant.MEN_SERVICES_COLLECTION)) {
            is SalonServicesResult.Success -> {
                val services = result.services
                if (services.isNotEmpty()) {
                    localDatabase.setMenServices(result.services)
                }
                ServiceContainerUIState.Success(result.services, true)
            }

            is SalonServicesResult.Failure -> {
                ServiceContainerUIState.Failure(result.exception)
            }
        }
    }

    suspend fun getWomenSalonServices(): ServiceContainerUIState {
        return when(val result = salonServiceRepository.getServices(SalonServicesRepositoryConstant.WOMEN_SERVICES_COLLECTION)) {
            is SalonServicesResult.Success -> {
                val services = result.services
                if (services.isNotEmpty()) {
                    localDatabase.setWomenServices(result.services)
                    localDatabase.lastCacheTimestampOfSalonServices = System.currentTimeMillis()
                }
                ServiceContainerUIState.Success(result.services, false)
            }

            is SalonServicesResult.Failure -> {
                ServiceContainerUIState.Failure(result.exception)
            }
        }
    }

    fun getCurrentUser() = localDatabase.getUser()

    fun isNewUser() = localDatabase.isNewUser()

    fun getLastTemeCache() = localDatabase.lastCacheTimestampOfSalonServices

    fun getMenServicesFromCache() = localDatabase.getMenServices()

    fun getWomenServicesFromCache() = localDatabase.getWomenServices()
}