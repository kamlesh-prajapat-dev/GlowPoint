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

object BookingRepositoryConstant {
    const val COLLECTION_NAME = "bookings"
    const val USER_ID = "userId"
    const val BOOKING_STATUS = "bookingStatus"
    const val PREVIOUS_STATUS = "previousStatus"
}

object SalonServicesRepositoryConstant {
    const val MEN_SERVICES_COLLECTION = "men_services"
    const val WOMEN_SERVICES_COLLECTION = "women_services"
}

object SalonRepositoryConstant {
    const val COLLECTION = "salons"
    const val GEO_HASH = "geoHash"
    const val TIME_SLOTS_COLLECTION = "timeSlots"
}

object DateTimeFormateConstant {
    const val DATE_FORMATE = "yyyy-MM-dd"
    const val TIME_FORMATE = "HH:mm"
}

object TokenRepositoryConstant {
    const val COLLECTION_NAME = "tokens"
    const val FCM_TOKEN = "fcmToken"
    const val UPDATE_AT = "updatedAt"
}

object UserRepositoryConstant {
    const val COLLECTION_NAME = "users"
    const val PHONE_NUMBER = "phoneNumber"
}