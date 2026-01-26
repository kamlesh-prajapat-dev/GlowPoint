package com.example.glowpoint.domain.model.failure.realtime

sealed class WriteReqDomainFailure {

    object NoInternet : WriteReqDomainFailure()

    data class PermissionDenied(val message: String) : WriteReqDomainFailure()

    data class NotFound(val message: String) : WriteReqDomainFailure()

    data class Cancelled(val message: String) : WriteReqDomainFailure()

    data class ValidationError(
        val message: String
    ) : WriteReqDomainFailure()

    data class Unknown(
        val cause: Throwable
    ) : WriteReqDomainFailure()
}