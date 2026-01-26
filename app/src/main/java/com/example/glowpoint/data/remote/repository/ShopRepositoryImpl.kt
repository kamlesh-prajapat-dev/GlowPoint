package com.example.glowpoint.data.remote.repository

import com.example.glowpoint.data.models.ShopDetails
import com.example.glowpoint.data.models.TimeSlot
import com.example.glowpoint.data.remote.firebase.realtime.ErrorMapper
import com.example.glowpoint.data.sample.NewShopDetails
import com.example.glowpoint.domain.model.result.FetchSalonsResult
import com.example.glowpoint.domain.repository.SalonRepository
import com.example.glowpoint.ui.screens.sample.SampleDataUIState
import com.example.glowpoint.util.DateTimeFormateConstant
import com.example.glowpoint.util.Logger
import com.example.glowpoint.util.SalonRepositoryConstant
import com.example.glowpoint.util.TimeSlotStatus
import com.firebase.geofire.GeoQueryBounds
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseException
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
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
import kotlin.coroutines.cancellation.CancellationException

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

        val maxResults = 50L

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
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            FetchSalonsResult.Failure(e)
        }
    }

    override fun observeTimeSlot(
        salonId: String,
        date: Long,
        openTime: String,
        closeTime: String
    ): Flow<FetchSalonsResult> = callbackFlow {

        if (salonId.isBlank()) {
            trySend(
                FetchSalonsResult.Failure(
                    IllegalArgumentException("SalonId cannot be blank")
                )
            )
            close()
            return@callbackFlow
        }

        val currentDate = formatDate(date)

        val ref = realtimeDatabase
            .getReference(SalonRepositoryConstant.TIME_SLOTS_COLLECTION)
            .child(salonId)
            .child(currentDate)

        val realtimeListener = object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val timeSlots = snapshot.children.mapNotNull { child ->
                        val time = child.key

                        val status = child.getValue(String::class.java)

                        if (time.isNullOrBlank() || status == null) {
                            Logger.e(
                                tag = "TimeSlotMapping",
                                message = "Invalid slot data for salonId=$salonId date=$currentDate"
                            )
                            null
                        } else {
                            TimeSlot(
                                time = time,
                                isAvailable = status == TimeSlotStatus.AVAILABLE,
                                isSelected = false
                            )
                        }
                    }

                    trySend(
                        FetchSalonsResult.GetTimeSlotSuccess(timeSlots)
                    )

                } catch (e: Exception) {
                    trySend(FetchSalonsResult.Failure(e))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                val exception = ErrorMapper.map(error)

                trySend(FetchSalonsResult.Failure(exception))
                close(exception)
            }
        }

        // 🔹 Ensure node exists ONCE, safely
        ref.runTransaction(object : Transaction.Handler {

            override fun doTransaction(currentData: MutableData): Transaction.Result {
                if (!currentData.hasChildren()) {
                    val slots = generateTimeSlots(openTime, closeTime)
                    currentData.value = slots
                }
                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                snapshot: DataSnapshot?
            ) {
                if (error != null) {
                    val exception = ErrorMapper.map(error)
                    trySend(FetchSalonsResult.Failure(exception))
                    close(exception)
                    return
                }

                ref.addValueEventListener(realtimeListener)
            }
        })

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

        if (salonId.isBlank()) {
            return FetchSalonsResult.Failure(
                IllegalArgumentException("SalonId cannot be blank")
            )
        }

        if (timeSlot.isEmpty()) {
            return FetchSalonsResult.Failure(
                IllegalArgumentException("TimeSlot list cannot be empty")
            )
        }

        val currentDate = formatDate(date)

        return try {
            val ref = realtimeDatabase
                .getReference(SalonRepositoryConstant.TIME_SLOTS_COLLECTION)
                .child(salonId)
                .child(currentDate)

            // Only valid slots should be updated
            val updatedSlots = timeSlot
                .filter { it.time.isNotBlank() }
                .associate { slot ->
                    slot.time to if (slot.isSelected)
                        TimeSlotStatus.BOOKED
                    else
                        TimeSlotStatus.AVAILABLE
                }

            if (updatedSlots.isEmpty()) {
                return FetchSalonsResult.Failure(
                    IllegalStateException("No valid time slots to update")
                )
            }

            ref.updateChildren(updatedSlots).await()

            FetchSalonsResult.UpdateTimeSlotSuccess(true)

        } catch (e: CancellationException) {
            // NEVER swallow coroutine cancellation
            throw e

        } catch (e: FirebaseNetworkException) {
            FetchSalonsResult.Failure(e)

        } catch (e: DatabaseException) {
            FetchSalonsResult.Failure(e)

        } catch (e: Exception) {
            FetchSalonsResult.Failure(e)
        }
    }

    override suspend fun saveShopData(salonShops: List<NewShopDetails>): SampleDataUIState {
        return try {
            if (salonShops.isEmpty()) {
                return SampleDataUIState.Failure(
                    Exception("Services list is empty")
                )
            }

            val collectionRef = firestore.collection(SalonRepositoryConstant.COLLECTION)

            // 🔹 1. Check if collection already has data
            val existing = collectionRef.limit(1).get().await()
            if (!existing.isEmpty) {
                return SampleDataUIState.AlreadySaved
            }

            // 🔹 2. Save sample data (one-time)
            val batch = firestore.batch()
            salonShops.forEach { service ->
                val docRef = collectionRef.document()
                batch.set(docRef, service)
            }

            batch.commit().await()

            SampleDataUIState.Success
        } catch (e: Exception) {
            SampleDataUIState.Failure(e)
        }
    }
}