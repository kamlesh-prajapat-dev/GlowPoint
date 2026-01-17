package com.example.glowpoint.data.remote.firebase

import android.util.Log
import com.example.glowpoint.data.models.ShopDetails
import com.example.glowpoint.data.models.TimeSlot
import com.example.glowpoint.domain.model.FetchSalonsResult
import com.example.glowpoint.domain.repository.SalonRepository
import com.example.glowpoint.util.DateTimeFormateConstant
import com.example.glowpoint.util.SalonRepositoryConstant
import com.example.glowpoint.util.TimeSlotStatus
import com.firebase.geofire.GeoQueryBounds
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShopRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val realtimeDatabase: FirebaseDatabase
) : SalonRepository {

    override suspend fun getNearBySalon(
        centerLat: Double,
        centerLng: Double,
        bounds: List<GeoQueryBounds>
    ): FetchSalonsResult = coroutineScope {

        val maxResults = 20L

        try {
            val snapshots = bounds.map { b ->
                async {
                    firestore.collection(SalonRepositoryConstant.COLLECTION)
                        .orderBy(SalonRepositoryConstant.GEO_HASH)
                        .startAt(b.startHash)
                        .endAt(b.endHash)
                        .limit(maxResults)
                        .get()
                        .await()
                }
            }.awaitAll()

            val documents = snapshots
                .flatMap { it.documents }
                .distinctBy { it.id }

            if (documents.isEmpty()) {
                FetchSalonsResult.NotServiceable
            } else {
                val salons = documents
                    .mapNotNull { it.toObject(ShopDetails::class.java)?.copy(id = it.id) }
                    .take(maxResults.toInt())

                FetchSalonsResult.Success(salons)
            }
        } catch (e: Exception) {
            Log.e("SalonRepository", "Failed to fetch salons", e)
            FetchSalonsResult.Failure(e)
        }
    }

    override fun observeTimeSlot(
        salonId: String,
        date: Long,
        openTime: String,
        closeTime: String
    ): Flow<FetchSalonsResult> = callbackFlow {

        val currentDate = formatDate(date)
        val ref = realtimeDatabase
            .getReference(SalonRepositoryConstant.TIME_SLOTS_COLLECTION)
            .child(salonId)
            .child(currentDate)

        // 🔹 Listener for realtime updates
        val realtimeListener = object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                val timeSlots = snapshot.children.map { child ->

                    val time = child.key ?: ""
                    val status = child.getValue(String::class.java) ?: TimeSlotStatus.BOOKED

                    TimeSlot(
                        time = time,
                        isAvailable = status == TimeSlotStatus.AVAILABLE,
                        isSelected = false
                    )
                }

                trySend(
                    FetchSalonsResult.GetTimeSlotSuccess(timeSlots)
                )
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(FetchSalonsResult.Failure(error.toException()))
                close()
            }
        }

        // 🔹 One-time check for existence
        ref.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    // Create node if missing
                    val slots = generateTimeSlots(openTime = openTime, closeTime = closeTime)
                    ref.setValue(slots)
                }

                // Attach realtime listener AFTER ensuring existence
                ref.addValueEventListener(realtimeListener)
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(FetchSalonsResult.Failure(error.toException()))
                close()
            }
        })

        // ✅ awaitClose MUST be here
        awaitClose {
            ref.removeEventListener(realtimeListener)
        }
    }

    private fun formatDate(dateMillis: Long): String {
        val formatter = SimpleDateFormat(DateTimeFormateConstant.DATE_FORMATE, Locale.US)
        return formatter.format(Date(dateMillis))
    }

    private fun generateTimeSlots(
        openTime: String,
        closeTime: String
    ): Map<String, String> {

        val formatter = SimpleDateFormat(DateTimeFormateConstant.TIME_FORMATE, Locale.US)
        formatter.isLenient = false

        val start = Calendar.getInstance().apply {
            time = formatter.parse(openTime)!!
        }

        val end = Calendar.getInstance().apply {
            time = formatter.parse(closeTime)!!
        }

        val slots = mutableMapOf<String, String>()

        while (start.before(end)) {
            val slotTime = formatter.format(start.time)
            slots[slotTime] = TimeSlotStatus.AVAILABLE
            start.add(Calendar.MINUTE, 30)
        }

        return slots
    }


    override suspend fun updateTimeSlot(
        salonId: String,
        date: Long,
        timeSlot: List<TimeSlot>
    ): FetchSalonsResult {
        return try {
            val currentDate = formatDate(date)

            val ref = realtimeDatabase
                .getReference(SalonRepositoryConstant.TIME_SLOTS_COLLECTION)
                .child(salonId)
                .child(currentDate)

            // 🔥 Only update given slots
            val updatedSlots = timeSlot.associate { slot ->
                slot.time to if (slot.isSelected)
                    TimeSlotStatus.BOOKED
                else
                    TimeSlotStatus.AVAILABLE
            }

            ref.updateChildren(updatedSlots).await()

            FetchSalonsResult.UpdateTimeSlotSuccess(true)

        } catch (e: Exception) {
            FetchSalonsResult.Failure(e)
        }
    }
}