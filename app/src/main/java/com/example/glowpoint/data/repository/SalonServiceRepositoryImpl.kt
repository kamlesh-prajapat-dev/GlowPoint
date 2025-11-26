package com.example.glowpoint.data.repository

import com.example.glowpoint.data.models.ServiceItem
import com.example.glowpoint.domain.model.FetchSalonServicesResult
import com.example.glowpoint.domain.repository.SalonServiceRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.getField
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SalonServiceRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : SalonServiceRepository {

    override suspend fun getMenSalonServices(): FetchSalonServicesResult {
        return getServicesFromCollection("men_services")
    }

    override suspend fun getWomenSalonServices(): FetchSalonServicesResult {
        return getServicesFromCollection("women_services")
    }

    private suspend fun getServicesFromCollection(collectionName: String): FetchSalonServicesResult {
        return try {
            FetchSalonServicesResult.Success(
                firestore
                    .collection(collectionName)
                    .get()
                    .await()
                    .documents
                    .mapNotNull { ServiceItem(
                        id = it.id,
                        name = it.getString("name") ?: "",
                        description = it.getString("description") ?: "",
                        price = it.getField("price") ?: 0.0f,
                    ) }
            )
        } catch (e: Exception) {
            FetchSalonServicesResult.Failure(e)
        }
    }
}
