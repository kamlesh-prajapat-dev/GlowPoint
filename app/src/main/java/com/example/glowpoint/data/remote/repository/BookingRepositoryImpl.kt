package com.example.glowpoint.data.remote.repository

import com.example.glowpoint.data.models.BookingDetails
import com.example.glowpoint.data.models.FetchedBooking
import com.example.glowpoint.data.remote.exception.DataParsingException
import com.example.glowpoint.data.remote.exception.EmptyDataException
import com.example.glowpoint.data.remote.firebase.realtime.ErrorMapper
import com.example.glowpoint.domain.model.result.BookingResult
import com.example.glowpoint.domain.repository.BookingRepository
import com.example.glowpoint.util.BookingRepositoryConstant
import com.example.glowpoint.util.BookingStatus
import com.example.glowpoint.util.Logger
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseException
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

@Singleton
class BookingRepositoryImpl @Inject constructor(
    private val realtimeDatabase: FirebaseDatabase
) : BookingRepository {
    override suspend fun bookServices(
        booking: BookingDetails
    ): BookingResult {

        if (booking.userId.isBlank()) {
            return BookingResult.Failure(
                IllegalArgumentException("UserId cannot be blank")
            )
        }

        return try {
            val ref = realtimeDatabase
                .getReference(BookingRepositoryConstant.COLLECTION_NAME)

            val bookingId = ref.push().key
                ?: return BookingResult.Failure(
                    IllegalStateException("Firebase failed to generate bookingId")
                )

            ref.child(bookingId)
                .setValue(booking)
                .await()

            BookingResult.Success(bookingId)

        } catch (e: CancellationException) {
            // MUST rethrow
            throw e

        } catch (e: FirebaseNetworkException) {
            BookingResult.Failure(e)

        } catch (e: DatabaseException) {
            BookingResult.Failure(e)

        } catch (e: Exception) {
            BookingResult.Failure(e)
        }
    }


    override fun observeBookings(userId: String): Flow<BookingResult> = callbackFlow {

        if (userId.isBlank()) {
            trySend(
                BookingResult.Failure(
                    IllegalArgumentException("UserId cannot be blank")
                )
            )
            close()
            return@callbackFlow
        }

        val ref = realtimeDatabase
            .getReference(BookingRepositoryConstant.COLLECTION_NAME)
            .orderByChild(BookingRepositoryConstant.USER_ID)
            .equalTo(userId)

        val listener = object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val bookings = snapshot.children.mapNotNull { child ->
                        val booking = child.getValue(FetchedBooking::class.java)

                        if (booking == null) {
                            // CRITICAL: Data shape issue
                            Logger.e(
                                tag = "BookingMapping",
                                message = "Failed to map booking for key=${child.key}"
                            )
                            null
                        } else {
                            booking.copy(bookingId = child.key.orEmpty())
                        }
                    }

                    trySend(
                        if (bookings.isNotEmpty()) {
                            BookingResult.GetSuccess(bookings)
                        } else {
                            BookingResult.Failure(EmptyDataException("The requested data was not found."))
                        }
                    )
                } catch (e: Exception) {

                    trySend(BookingResult.Failure(DataParsingException(
                        e.message ?: "Failed to parse booking data"
                    )))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                val exception = ErrorMapper.map(error)

                trySend(BookingResult.Failure(exception))
                close(exception)
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
            .getReference(BookingRepositoryConstant.COLLECTION_NAME)
            .child(bookingId)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    trySend(
                        BookingResult.Failure(
                            EmptyDataException("The requested data was not found.")
                        )
                    )
                    return
                }

                try {
                    val booking =
                        snapshot.getValue(FetchedBooking::class.java)?.copy(bookingId = bookingId)
                    if (booking != null) {
                        trySend(BookingResult.GetFetchedBookingSuccess(booking))
                    } else {
                        trySend(BookingResult.Failure(EmptyDataException("The requested data was not found.")))
                    }
                } catch (e: Exception) {
                    trySend(
                        BookingResult.Failure(
                            DataParsingException(
                                e.message ?: "Failed to parse booking data"
                            )
                        )
                    )
                    close(e)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                val exception = ErrorMapper.map(error)
                trySend(BookingResult.Failure(exception))
                close(exception)
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

        if (bookingId.isBlank()) {
            return BookingResult.Failure(
                IllegalArgumentException("BookingId cannot be blank")
            )
        }

        return try {
            val bookingRef = realtimeDatabase
                .getReference(BookingRepositoryConstant.COLLECTION_NAME)
                .child(bookingId)

            val snapshot = bookingRef.get().await()

            if (!snapshot.exists()) {
                return BookingResult.Failure(
                    EmptyDataException("Booking not found")
                )
            }

            val updates = mapOf(
                BookingRepositoryConstant.BOOKING_STATUS to BookingStatus.CANCELLED,
                BookingRepositoryConstant.PREVIOUS_STATUS to previousStatus
            )

            bookingRef.updateChildren(updates).await()

            BookingResult.CancelSuccess(isSuccess = true)

        } catch (e: FirebaseNetworkException) {
            BookingResult.Failure(e)

        } catch (e: DatabaseException) {
            BookingResult.Failure(e)

        } catch (e: CancellationException) {
            throw e // VERY IMPORTANT – never swallow coroutine cancellation

        } catch (e: Exception) {
            BookingResult.Failure(e)
        }
    }

}
