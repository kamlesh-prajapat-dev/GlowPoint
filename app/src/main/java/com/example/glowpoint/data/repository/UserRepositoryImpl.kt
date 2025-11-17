package com.example.glowpoint.data.repository

import com.example.glowpoint.data.models.User
import com.example.glowpoint.domain.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
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
}