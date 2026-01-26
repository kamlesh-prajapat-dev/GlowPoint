package com.example.glowpoint.data.remote.repository

import com.example.glowpoint.data.models.FetchedServiceItem
import com.example.glowpoint.data.sample.NewServiceModel
import com.example.glowpoint.data.sample.ServiceModel
import com.example.glowpoint.domain.model.result.SalonServicesResult
import com.example.glowpoint.domain.repository.SalonServiceRepository
import com.example.glowpoint.ui.screens.sample.SampleDataUIState
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

@Singleton
class SalonServiceRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : SalonServiceRepository {

    override suspend fun getServices(collectionName: String): SalonServicesResult {

        if (collectionName.isBlank()) {
            return SalonServicesResult.Failure(
                IllegalArgumentException("Collection name cannot be blank")
            )
        }

        return try {
            val snapshot = firestore.collection(collectionName)
                .get()
                .await()

            val services = snapshot.documents.mapNotNull { doc ->
                doc.toObject(FetchedServiceItem::class.java)
                    ?.copy(id = doc.id)
            }

            SalonServicesResult.Success(services)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            SalonServicesResult.Failure(e)
        }
    }

    override suspend fun createServices(
        services: List<ServiceModel>,
        collectionName: String
    ): SampleDataUIState {
        return try {
            if (services.isEmpty()) {
                return SampleDataUIState.Failure(
                    Exception("Services list is empty")
                )
            }

            val collectionRef = firestore.collection(collectionName)

            // 🔹 1. Check if collection already has data
            val existing = collectionRef.limit(1).get().await()
            if (!existing.isEmpty) {
                return SampleDataUIState.AlreadySaved
            }

            // 🔹 2. Save sample data (one-time)
            val batch = firestore.batch()
            services.forEach { service ->
                val docRef = collectionRef.document(service.id)
                batch.set(docRef, NewServiceModel(
                    name = service.name,
                    description = service.description,
                    price = service.price
                ))
            }

            batch.commit().await()

            SampleDataUIState.Success
        } catch (e: Exception) {
            SampleDataUIState.Failure(e)
        }
    }
}