package com.example.glowpoint.domain.mapper

import com.example.glowpoint.domain.model.failure.firestore.GetReqDomainFailure
import com.example.glowpoint.util.Logger
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlin.coroutines.cancellation.CancellationException

object FirestoreFailureMapper {

    fun <T> map(throwable: Throwable, data: T): GetReqDomainFailure {

        return when (throwable) {

            is CancellationException -> {
                GetReqDomainFailure.Cancelled
            }

            is FirebaseNetworkException -> {
                GetReqDomainFailure.NoInternet
            }

            is FirebaseFirestoreException -> {
                mapFirestoreException(throwable, data)
            }

            is IllegalArgumentException -> {
                GetReqDomainFailure.InvalidRequest
            }

            else -> {
                Logger.e(
                    tag = "UnknownFailure",
                    message = "Unexpected error in cancelBooking, data is $data",
                    throwable = throwable
                )
                GetReqDomainFailure.Unknown(throwable)
            }
        }
    }

    private fun <T> mapFirestoreException(
        exception: FirebaseFirestoreException,
        data: T
    ): GetReqDomainFailure {

        return when (exception.code) {

            FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                Logger.e(
                    tag = "FirebasePermission",
                    message = "Permission denied for bookingId=$data",
                    throwable = exception
                )
                GetReqDomainFailure.PermissionDenied(exception.message ?: "Permission Denied.")
            }

            FirebaseFirestoreException.Code.NOT_FOUND -> {
                GetReqDomainFailure.DataNotFound
            }

            FirebaseFirestoreException.Code.UNAVAILABLE -> {
                GetReqDomainFailure.NoInternet
            }

            FirebaseFirestoreException.Code.FAILED_PRECONDITION -> {
                // Mostly index missing
                GetReqDomainFailure.InvalidRequest
            }

            else -> {
                Logger.e(
                    tag = "UnknownFailure",
                    message = "Unexpected error in cancelBooking, data is $data",
                    throwable = exception
                )
                GetReqDomainFailure.Unknown(exception)
            }
        }
    }
}