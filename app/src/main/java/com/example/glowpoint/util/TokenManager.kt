package com.example.glowpoint.util

import android.content.ContentValues.TAG
import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

object TokenManager {

    suspend fun getFCMToken(): String? {
        return try {
            val token =
                FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.w("Token", "Fetching FCM registration token failed", task.exception)
                        return@OnCompleteListener
                    }

                    // Get new FCM registration token
                    task.result
                }).await()
            token
        } catch (e: Exception) {
            null
        }
    }
}