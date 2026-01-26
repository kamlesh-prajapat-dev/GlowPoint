package com.example.glowpoint.domain.mapper

import com.example.glowpoint.data.remote.exception.DataParsingException
import com.example.glowpoint.data.remote.exception.EmptyDataException
import com.example.glowpoint.domain.model.failure.realtime.GetReqDomainFailure
import com.example.glowpoint.domain.model.failure.realtime.WriteReqDomainFailure
import com.example.glowpoint.util.Logger
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.database.DatabaseException
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

fun <T>Throwable.toGetReqDomainFailure(data: T): GetReqDomainFailure {
    return when (this) {
        is SecurityException -> {
            Logger.e(
                tag = "FirebasePermission",
                message = "Permission denied for bookingId=$data",
                throwable = this
            )
            GetReqDomainFailure.PermissionDenied(this.message ?: "")
        }
        is IOException -> GetReqDomainFailure.Network
        is EmptyDataException -> GetReqDomainFailure.NotFound(this.message ?: "The requested data was not found.")
        is DataParsingException -> {
            Logger.e(
                tag = "ObserveBookings",
                message = "Unexpected error while parsing data $data",
                throwable = this
            )
            GetReqDomainFailure.InvalidData(this.message ?: "")
        }
        else -> {
            Logger.e(
                tag = "UnknownFailure",
                message = "Unexpected error in cancelBooking, data is $data",
                throwable = this
            )
            GetReqDomainFailure.Unknown(this)
        }
    }
}

fun <T> Throwable.toWriteReqDomainFailure(data: T): WriteReqDomainFailure {
    return when (this) {
        is FirebaseNetworkException -> {
            WriteReqDomainFailure.NoInternet
        }

        is DatabaseException -> {
            if (message?.contains("Permission denied", true) == true) {
                Logger.e(
                    tag = "FirebasePermission",
                    message = "Permission denied for bookingId=$data",
                    throwable = this
                )
                WriteReqDomainFailure.PermissionDenied(message ?: "Permission denied")
            } else {
                WriteReqDomainFailure.Unknown(this)
            }
        }

        is EmptyDataException -> {
            WriteReqDomainFailure.NotFound(message ?: "The requested data was not found.")
        }

        is IllegalArgumentException -> {
            WriteReqDomainFailure.ValidationError(message ?: "Invalid input")
        }

        is CancellationException -> {
            WriteReqDomainFailure.Cancelled(message ?: "Cancelled")
        }

        is IllegalStateException -> {
            Logger.e(
                tag = "FirebaseIllegalStateException",
                message = "${this.message} for user: $data",
                throwable = this
            )
            WriteReqDomainFailure.Unknown(this)
        }

        else -> {
            Logger.e(
                tag = "UnknownFailure",
                message = "Unexpected error in cancelBooking",
                throwable = this
            )
            WriteReqDomainFailure.Unknown(this)
        }
    }
}
