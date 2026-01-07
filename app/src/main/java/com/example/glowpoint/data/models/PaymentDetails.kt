package com.example.glowpoint.data.models

import com.example.glowpoint.util.PaymentMethod
import com.example.glowpoint.util.PaymentStatus

data class PaymentDetails(
    val paymentMethod: String = PaymentMethod.PAYMENT_AFTER_SERVICE,
    val paymentStatus: String = PaymentStatus.PENDING,
    val paymentAmount: Double = 0.0,
    val paymentTimestamp: Long = 0L
)