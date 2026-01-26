package com.example.glowpoint.domain.model.failure.realtime

sealed class GetReqDomainFailure {
    data class PermissionDenied(val message: String) : GetReqDomainFailure()
    object Network : GetReqDomainFailure()
    data class NotFound(val message: String) : GetReqDomainFailure()
    data class InvalidData(val message: String) : GetReqDomainFailure()
    data class Unknown(val cause: Throwable) : GetReqDomainFailure()
}
