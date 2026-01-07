package com.example.glowpoint.domain.usecase

import com.example.glowpoint.data.local.LocalDatabase
import com.example.glowpoint.data.models.BookingDetails
import com.example.glowpoint.data.models.FetchedBooking
import com.example.glowpoint.data.models.TimeSlot
import com.example.glowpoint.domain.model.BookingResult
import com.example.glowpoint.domain.model.FetchSalonsResult
import com.example.glowpoint.domain.repository.BookingRepository
import com.example.glowpoint.domain.repository.SalonRepository
import com.example.glowpoint.ui.screens.bookingStatus.BookingStatusUIState
import com.example.glowpoint.ui.screens.bookings.BookingsUIState
import com.example.glowpoint.ui.screens.components.bookingsummary.BookingSummaryUIState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.collections.map

class BookingUseCase @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val localDatabase: LocalDatabase,
    private val shopRepository: SalonRepository
) {

    suspend fun bookService(bookingDetails: BookingDetails): BookingSummaryUIState {
        return when (val result = bookingRepository.bookServices(bookingDetails)) {
            is BookingResult.Success -> {
                val timeSlots = bookingDetails.selectedTimeSlot.map {
                    TimeSlot(
                        time = it,
                        isAvailable = true,
                        isSelected = true
                    )
                }
                when (val result = updateTimeSlot(
                    salonId = bookingDetails.shopId,
                    date = System.currentTimeMillis(),
                    selectedTimeSlot = timeSlots
                )) {
                    is FetchSalonsResult.UpdateTimeSlotSuccess -> {

                    }

                    is FetchSalonsResult.Failure -> {
                        BookingSummaryUIState.Failure(result.exception)
                    }

                    else -> Unit
                }
                BookingSummaryUIState.Success(converter(bookingDetails, result.bookingId))
            }

            is BookingResult.Failure -> {
                BookingSummaryUIState.Failure(result.exception)
            }

            else -> BookingSummaryUIState.Idle
        }
    }

    fun observeBookings(): Flow<BookingsUIState> {
        val user = localDatabase.getUser()
        val userId = user?.uid
            ?: return flowOf(BookingsUIState.Failure(IllegalArgumentException("UserId cannot be blank")))
        return bookingRepository.observeBookings(userId)
            .map {
                when (it) {
                    is BookingResult.GetSuccess -> {
                        BookingsUIState.GetSuccess(it.bookings)
                    }

                    is BookingResult.Failure -> {
                        BookingsUIState.Failure(it.exception)
                    }

                    else -> BookingsUIState.Idle
                }
            }.catch {
                emit(BookingsUIState.Failure(it as Exception))
            }
    }

    fun observeBooking(bookingId: String): Flow<BookingStatusUIState> {
        return bookingRepository.observeBooking(bookingId)
            .map {
                when (it) {
                    is BookingResult.GetFetchedBookingSuccess -> {
                        BookingStatusUIState.Success(it.booking)
                    }

                    is BookingResult.Failure -> {
                        BookingStatusUIState.Failure(it.exception)
                    }

                    else -> BookingStatusUIState.Idle
                }
            }.catch {
                emit(BookingStatusUIState.Failure(it as Exception))
            }
    }

    suspend fun cancelBooking(
        previousStatus: String,
        bookingId: String,
        salonId: String,
        selectedTimeSlot: List<String>
    ): BookingStatusUIState {
        return when (val result = bookingRepository.cancelBooking(previousStatus, bookingId)) {
            is BookingResult.CancelSuccess -> {
                val isSuccess = result.isSuccess
                if (isSuccess) {
                    val timeSlots = selectedTimeSlot.map {
                        TimeSlot(
                            time = it,
                            isAvailable = true,
                            isSelected = false
                        )
                    }
                    when (val result = updateTimeSlot(
                        salonId = salonId,
                        date = System.currentTimeMillis(),
                        selectedTimeSlot = timeSlots
                    )) {
                        is FetchSalonsResult.UpdateTimeSlotSuccess -> {
                            // Do nothing
                            BookingStatusUIState.CancelSuccess(result.isSuccess)
                        }

                        is FetchSalonsResult.Failure -> {
                            BookingStatusUIState.Failure(result.exception)
                        }

                        else -> Unit
                    }
                }
                BookingStatusUIState.CancelSuccess(false)
            }

            is BookingResult.Failure -> {
                BookingStatusUIState.Failure(result.exception)
            }

            else -> BookingStatusUIState.Idle
        }
    }

    private suspend fun updateTimeSlot(
        salonId: String,
        date: Long,
        selectedTimeSlot: List<TimeSlot>
    ): FetchSalonsResult {
        return shopRepository.updateTimeSlot(
            salonId = salonId,
            date = date,
            timeSlot = selectedTimeSlot
        )
    }

    private fun converter(bookingDetails: BookingDetails, bookingId: String): FetchedBooking {
        return FetchedBooking(
            bookingId = bookingId,
            userId = bookingDetails.userId,
            shopId = bookingDetails.shopId,
            shopName = bookingDetails.shopName,
            shopAddress = bookingDetails.shopAddress,
            distance = bookingDetails.distance,
            selectedServices = bookingDetails.selectedServices,
            selectedTimeSlot = bookingDetails.selectedTimeSlot,
            createdAt = bookingDetails.createdAt,
            bookingStatus = bookingDetails.bookingStatus,
            paymentDetails = bookingDetails.paymentDetails
        )
    }
}