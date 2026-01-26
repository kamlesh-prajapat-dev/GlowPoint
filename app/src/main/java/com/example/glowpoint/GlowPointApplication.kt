package com.example.glowpoint

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class GlowPointApplication : Application(), Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }

    private fun createNotificationChannel() {
        val name = "Booking Status"
        val descriptionText = "Notifications about your booking status"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel("BOOKING_STATUS_CHANNEL", name, importance).apply {
            description = descriptionText
        }
        // Register the channel with the system
        val notificationManager: NotificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}