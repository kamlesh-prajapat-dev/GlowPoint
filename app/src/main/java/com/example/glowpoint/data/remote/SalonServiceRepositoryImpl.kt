package com.example.glowpoint.data.remote

import com.example.glowpoint.data.models.FetchedServiceItem
import com.example.glowpoint.domain.model.SalonServicesResult
import com.example.glowpoint.domain.repository.SalonServiceRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SalonServiceRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : SalonServiceRepository {

    override suspend fun getServices(collectionName: String): SalonServicesResult {
        return try {
            val snapshot = firestore.collection(collectionName)
                .get()
                .await()

            val documents = snapshot.documents
            if (documents.isNotEmpty()) {
                val services = documents.mapNotNull { it.toObject(FetchedServiceItem::class.java)
                    ?.copy(id = it.id) }
                SalonServicesResult.Success(services)
            } else {
                SalonServicesResult.Success(emptyList())
            }
        } catch (e: Exception) {
            SalonServicesResult.Failure(e)
        }
    }
}
