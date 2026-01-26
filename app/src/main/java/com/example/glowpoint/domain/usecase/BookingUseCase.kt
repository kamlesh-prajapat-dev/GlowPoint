package com.example.glowpoint.domain.usecase

import com.example.glowpoint.data.local.LocalDatabase
import com.example.glowpoint.data.models.BookingDetails
import com.example.glowpoint.data.models.FetchedBooking
import com.example.glowpoint.data.models.api.NotificationRequest
import com.example.glowpoint.data.models.TimeSlot
import com.example.glowpoint.domain.mapper.toGetReqDomainFailure
import com.example.glowpoint.domain.mapper.toWriteReqDomainFailure
import com.example.glowpoint.domain.model.result.BookingResult
import com.example.glowpoint.domain.model.result.FetchSalonsResult
import com.example.glowpoint.domain.model.result.NotificationResult
import com.example.glowpoint.domain.model.result.TokenResult
import com.example.glowpoint.domain.repository.BookingRepository
import com.example.glowpoint.domain.repository.NotificationRepository
import com.example.glowpoint.domain.repository.SalonRepository
import com.example.glowpoint.domain.repository.TokenRepository
import com.example.glowpoint.ui.screens.bookingStatus.BookingStatusUIState
import com.example.glowpoint.ui.screens.bookings.BookingsUIState
import com.example.glowpoint.ui.screens.components.bookingsummary.BookingSummaryUIState
import com.example.glowpoint.util.Logger
import com.example.glowpoint.workerscheduler.SenderNotificationWorkerScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BookingUseCase @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val localDatabase: LocalDatabase,
    private val shopRepository: SalonRepository,
    private val tokenRepository: TokenRepository,
    private val notificationRepository: NotificationRepository,
    private val senderNotificationWorkerScheduler: SenderNotificationWorkerScheduler
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
                val shopId = bookingDetails.shopId
                when (val updateTimeSlotResult = updateTimeSlot(
                    salonId = shopId,
                    date = System.currentTimeMillis(),
                    selectedTimeSlot = timeSlots
                )) {
                    is FetchSalonsResult.UpdateTimeSlotSuccess -> {
                        when(val notifyResult = notifyOwner(bookingId = result.bookingId, shopId = shopId)) {
                            is NotificationResult.Success -> {
                                Logger.d("OrderUseCase", "Notification sent successfully")
                            }

                            is NotificationResult.Error -> {
                                Logger.e("OrderUseCase", "Error sending notification", notifyResult.e)
                            }
                        }
                    }

                    is FetchSalonsResult.Failure -> {
                        BookingSummaryUIState.Failure(updateTimeSlotResult.exception.toWriteReqDomainFailure(shopId))
                    }

                    else -> Unit
                }
                BookingSummaryUIState.Success(converter(bookingDetails, result.bookingId))
            }

            is BookingResult.Failure -> {
                BookingSummaryUIState.Failure(result.exception.toWriteReqDomainFailure(data = bookingDetails.userId))
            }

            else -> BookingSummaryUIState.Idle
        }
    }

    private suspend fun notifyOwner(bookingId: String, shopId: String): NotificationResult {
        return when (val result = tokenRepository.getShopFcmToken(shopId)) {
            is TokenResult.Success -> {
                val token = result.token
                notificationRepository.sendNotification(
                    NotificationRequest(
                        token = token,
                        title = "New Order Received",
                        body = "You have a new order! Order ID: $bookingId",
                        bookingId = bookingId
                    )
                )

                NotificationResult.Success(true)
            }

            is TokenResult.Failure -> {
                senderNotificationWorkerScheduler.retryNotification(bookingId = bookingId, shopId = shopId)
                return NotificationResult.Error(result.e)
            }
        }
    }

    fun observeBookings(): Flow<BookingsUIState> {
        val user = localDatabase.getUser()
        val userId = user?.uid
            ?: return flowOf(BookingsUIState.Failure(IllegalArgumentException("UserId cannot be blank").toGetReqDomainFailure(
                null
            )))
        return bookingRepository.observeBookings(userId)
            .map {
                when (it) {
                    is BookingResult.GetSuccess -> {
                        BookingsUIState.GetSuccess(it.bookings)
                    }

                    is BookingResult.Failure -> {
                        BookingsUIState.Failure(it.exception.toGetReqDomainFailure(userId))
                    }

                    else -> BookingsUIState.Idle
                }
            }.catch {
                emit(BookingsUIState.Failure(it.toGetReqDomainFailure(userId)))
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
                        BookingStatusUIState.GetFailure(it.exception.toGetReqDomainFailure(bookingId))
                    }

                    else -> BookingStatusUIState.Idle
                }
            }.catch {
                emit(BookingStatusUIState.GetFailure(it.toGetReqDomainFailure(bookingId)))
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
                            BookingStatusUIState.WriteFailure(result.exception.toWriteReqDomainFailure(salonId))
                        }

                        else -> Unit
                    }
                }
                BookingStatusUIState.CancelSuccess(false)
            }

            is BookingResult.Failure -> {
                BookingStatusUIState.WriteFailure(result.exception.toWriteReqDomainFailure(bookingId))
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