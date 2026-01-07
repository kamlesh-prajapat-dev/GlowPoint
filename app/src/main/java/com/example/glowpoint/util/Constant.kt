package com.example.glowpoint.util

object PaymentMethod {
    const val PAYMENT_AFTER_SERVICE = "After Service"
    const val PAYMENT_BEFORE_SERVICE = "Before Service"
}

object PaymentStatus {
    const val PENDING = "Pending"
    const val IN_PROGRESS = "In Progress"
    const val COMPLETE = "Complete"
}

object TimeSlotStatus {
    const val AVAILABLE = "Available"
    const val BOOKED = "Booked"
}

object BookingStatus {
    const val PENDING = "Pending"
    const val CONFIRMED = "Confirmed"
    const val COMPLETE = "Complete"
    const val IN_PROGRESS = "In Progress"
    const val CANCELLED = "Cancelled"
}