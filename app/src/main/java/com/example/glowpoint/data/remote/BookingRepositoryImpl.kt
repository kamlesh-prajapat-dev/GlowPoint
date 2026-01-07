package com.example.glowpoint.data.remote

import com.example.glowpoint.data.models.BookingDetails
import com.example.glowpoint.data.models.FetchedBooking
import com.example.glowpoint.domain.model.BookingResult
import com.example.glowpoint.domain.repository.BookingRepository
import com.example.glowpoint.util.BookingStatus
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookingRepositoryImpl @Inject constructor(
    private val realtimeDatabase: FirebaseDatabase
) : BookingRepository {
    override suspend fun bookServices(booking: BookingDetails): BookingResult {
        return try {
            val ref = realtimeDatabase.getReference("bookings")

            // Firebase generated bookingId
            val bookingId = ref.push().key
                ?: return BookingResult.Failure(Exception("Failed to generate booking ID"))

            // Save booking details to Firebase Realtime Database
            ref.child(bookingId)
                .setValue(booking)
                .await()

            BookingResult.Success(bookingId)
        } catch (e: Exception) {
            BookingResult.Failure(e)
        }
    }

    override fun observeBookings(userId: String): Flow<BookingResult> = callbackFlow {
        if (userId.isBlank()) {
            trySend(BookingResult.Failure(IllegalArgumentException("UserId cannot be blank")))
            close()
            return@callbackFlow
        }

        val ref = realtimeDatabase
            .getReference("bookings")
            .orderByChild("userId")
            .equalTo(userId)

        val listener = object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                val bookings = mutableListOf<FetchedBooking>()

                for (child in snapshot.children) {
                    val booking = child.getValue(FetchedBooking::class.java)
                    if (booking != null) {
                        bookings.add(
                            booking.copy(
                                bookingId = child.key.orEmpty()
                            )
                        )
                    }
                }

                trySend(BookingResult.GetSuccess(bookings))
                    .onFailure {
                        // Flow collector is cancelled or buffer is full
                        // Try sending again
                        trySend(BookingResult.GetSuccess(bookings))
                    }
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(BookingResult.Failure(error.toException()))
                close(error.toException())
            }
        }

        ref.addValueEventListener(listener)

        awaitClose {
            ref.removeEventListener(listener)
        }
    }

    override fun observeBooking(bookingId: String): Flow<BookingResult> = callbackFlow {
        if (bookingId.isBlank()) {
            trySend(BookingResult.Failure(IllegalArgumentException("BookingId cannot be blank")))
            close()
            return@callbackFlow
        }

        val ref = realtimeDatabase
            .getReference("bookings")
            .child(bookingId)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val booking = snapshot.getValue(FetchedBooking::class.java)?.copy(bookingId = bookingId)
                if (booking != null) {
                    trySend(BookingResult.GetFetchedBookingSuccess(booking))
                        .onFailure {
                            // Flow collector is cancelled or buffer is full
                            // Try sending again
                            trySend(BookingResult.GetFetchedBookingSuccess(booking))
                        }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                trySend(BookingResult.Failure(error.toException()))
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)

        awaitClose {
            ref.removeEventListener(listener)
        }
    }

    override suspend fun cancelBooking(
        previousStatus: String,
        bookingId: String
    ): BookingResult {
        return try {
            val bookingRef = realtimeDatabase
                .getReference("bookings")
                .child(bookingId)

            val snapshot = bookingRef.get().await()

            if (!snapshot.exists()) {
                BookingResult.Failure(Exception("Booking not found"))
            } else {
                val updates = mapOf(
                    "bookingStatus" to BookingStatus.CANCELLED,
                    "previousStatus" to previousStatus
                )
                bookingRef.updateChildren(updates).await()
                BookingResult.CancelSuccess(isSuccess = true)
            }
        } catch (e: Exception) {
            BookingResult.Failure(e)
        }
    }
}
