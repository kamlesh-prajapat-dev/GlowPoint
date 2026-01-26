package com.example.glowpoint.domain.usecase

import com.example.glowpoint.data.local.LocalDatabase
import com.example.glowpoint.domain.mapper.FirestoreFailureMapper
import com.example.glowpoint.domain.model.result.SalonServicesResult
import com.example.glowpoint.domain.repository.SalonServiceRepository
import com.example.glowpoint.ui.screens.components.services.ServiceContainerUIState
import com.example.glowpoint.util.SalonServicesRepositoryConstant
import javax.inject.Inject

class SalonServiceUseCase @Inject constructor(
    private val salonServiceRepository: SalonServiceRepository,
    private val localDatabase: LocalDatabase
) {
    suspend fun getMenSalonServices(): ServiceContainerUIState {
        return when (val result =
            salonServiceRepository.getServices(SalonServicesRepositoryConstant.MEN_SERVICES_COLLECTION)) {
            is SalonServicesResult.Success -> {
                val services = result.services
                if (services.isNotEmpty()) {
                    localDatabase.setMenServices(services)
                    localDatabase.lastCacheTimestampOfSalonMenServices = System.currentTimeMillis()
                }
                ServiceContainerUIState.MenSuccess(
                    services = services
                )
            }

            is SalonServicesResult.Failure -> {
                ServiceContainerUIState.Failure(FirestoreFailureMapper.map(result.exception,
                    SalonServicesRepositoryConstant.MEN_SERVICES_COLLECTION))
            }
        }
    }

    suspend fun getWomenSalonServices(): ServiceContainerUIState {
        return when (val result =
            salonServiceRepository.getServices(SalonServicesRepositoryConstant.WOMEN_SERVICES_COLLECTION)) {
            is SalonServicesResult.Success -> {
                val services = result.services
                if (services.isNotEmpty()) {
                    localDatabase.setWomenServices(services)
                    localDatabase.lastCacheTimestampOfSalonWomenServices =
                        System.currentTimeMillis()
                }
                ServiceContainerUIState.WomenSuccess(
                    services = services
                )
            }

            is SalonServicesResult.Failure -> {
                ServiceContainerUIState.Failure(FirestoreFailureMapper.map(result.exception,
                    SalonServicesRepositoryConstant.WOMEN_SERVICES_COLLECTION))
            }
        }
    }

    fun getCurrentUser() = localDatabase.getUser()

    fun isNewUser() = localDatabase.isNewUser()

    fun getLastTemeCacheOfWomenServices() = localDatabase.lastCacheTimestampOfSalonWomenServices
    fun getLastTemeCacheOfMenServices() = localDatabase.lastCacheTimestampOfSalonMenServices


    fun getMenServicesFromCache() = localDatabase.getMenServices()

    fun getWomenServicesFromCache() = localDatabase.getWomenServices()
}