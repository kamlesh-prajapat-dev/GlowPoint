package com.example.glowpoint.data.repository

import android.util.Log
import com.example.glowpoint.data.models.SalonModel
import com.example.glowpoint.domain.model.FetchSalonsResult
import com.example.glowpoint.domain.repository.SalonRepository
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@Singleton
class SalonRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : SalonRepository {

    private val earthRadius = 6371000.0 // Earth radius in meters
    private val radiusInMeters: Double = 5000.0
    private val maxResults: Int = 20

    override suspend fun getNearBySalon(
        centerLat: Double,
        centerLng: Double
    ): FetchSalonsResult = coroutineScope {

        try {
            val center = GeoLocation(centerLat, centerLng)
            val bounds = GeoFireUtils.getGeoHashQueryBounds(center, radiusInMeters)

            val tasks = bounds.map { b ->
                async {
                    firestore.collection("salons")
                        .orderBy("geoHash")
                        .startAt(b.startHash)
                        .endAt(b.endHash)
                        .limit(maxResults.toLong())
                        .get()
                        .await()
                }
            }

            val snapshots = tasks.map { it.await() }
            val matchingDocs = mutableListOf<Pair<DocumentSnapshot, Double>>()
            val seenIds = HashSet<String>()

            for (snapshot in snapshots) {
                for (doc in snapshot.documents) {
                    val id = doc.id
                    if (seenIds.contains(id)) continue

                    val location = doc.getGeoPoint("location") ?: continue
                    val lat = location.latitude
                    val lng = location.longitude

                    val distance = haversineDistance(centerLat, centerLng, lat, lng)
                    if (distance <= radiusInMeters) {
                        seenIds.add(id)
                        matchingDocs.add(Pair(doc, distance))
                    }
                }
            }

            if (matchingDocs.isEmpty()) {
                return@coroutineScope FetchSalonsResult.NotServiceable
            }

            matchingDocs.sortBy { it.second }
            val salons = matchingDocs.take(maxResults).mapNotNull { (doc, distance) ->
                doc.toObject(SalonModel::class.java)?.copy(id = doc.id, distance = distance)
            }

            FetchSalonsResult.Success(salons)

        } catch (e: Exception) {
            Log.e("SalonRepository", "Failed to fetch salons", e)
            FetchSalonsResult.Failure(e)
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
}