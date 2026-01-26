package com.example.glowpoint.domain.model.failure.firestore

sealed class GetReqDomainFailure {
    object NoInternet : GetReqDomainFailure()

    data class PermissionDenied(val message: String) : GetReqDomainFailure()

    object DataNotFound : GetReqDomainFailure()

    object InvalidRequest : GetReqDomainFailure()

    object Cancelled : GetReqDomainFailure()

    data class Unknown(
        val cause: Throwable
    ) : GetReqDomainFailure()
}
