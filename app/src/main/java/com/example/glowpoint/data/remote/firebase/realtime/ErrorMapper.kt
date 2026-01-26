package com.example.glowpoint.data.remote.firebase.realtime

import com.example.glowpoint.data.remote.exception.UnknownException
import com.google.firebase.database.DatabaseError
import java.io.IOException

object ErrorMapper {
    fun map(error: DatabaseError): Exception {
        return when (error.code) {

            DatabaseError.PERMISSION_DENIED ->
                SecurityException("You don’t have permission to perform this action.")

            DatabaseError.NETWORK_ERROR ->
                IOException("Please check your internet connection and try again.")

            DatabaseError.DISCONNECTED ->
                IOException("Firebase disconnected")

            DatabaseError.OPERATION_FAILED ->
                IllegalStateException("Operation failed")

            DatabaseError.UNKNOWN_ERROR ->
                UnknownException("Something went wrong. Please try again.r")

            else ->
                error.toException()
        }
    }
}