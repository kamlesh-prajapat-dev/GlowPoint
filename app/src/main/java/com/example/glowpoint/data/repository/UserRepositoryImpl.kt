package com.example.glowpoint.data.repository

import com.example.glowpoint.data.models.User
import com.example.glowpoint.domain.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserRepository {

    override suspend fun createUser(user: User, onResult: (Boolean) -> Unit) {
        firestore.collection("users").document(user.uid)
            .set(user)
            .addOnCompleteListener { task ->
                onResult(task.isSuccessful)
            }
    }

    override suspend fun getUserByPhoneNumber(phoneNumber: String?): User? {
        return try {
            val snapshot = FirebaseFirestore.getInstance()
                .collection("users")
                .whereEqualTo("phoneNumber", phoneNumber)
                .limit(1)
                .get()
                .await()

            if (!snapshot.isEmpty)
                snapshot.documents[0].toObject(User::class.java)
            else
                null

        } catch (e: Exception) {
            null
        }
    }
}