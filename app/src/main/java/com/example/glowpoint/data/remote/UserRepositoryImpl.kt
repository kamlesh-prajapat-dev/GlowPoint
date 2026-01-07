package com.example.glowpoint.data.remote

import com.example.glowpoint.data.models.User
import com.example.glowpoint.domain.model.UserResult
import com.example.glowpoint.domain.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserRepository {

    override suspend fun createUser(user: User): UserResult {
        return try {
            firestore.collection("users").document(user.uid)
                .set(user)
                .await()

            UserResult.Success(user)
        } catch (e: Exception) {
            UserResult.Failure(e)
        }
    }

    override suspend fun getUserByPhoneNumber(phoneNumber: String): UserResult {
        return try {
            val snapshot = FirebaseFirestore.getInstance()
                .collection("users")
                .whereEqualTo("phoneNumber", phoneNumber)
                .limit(1)
                .get()
                .await()

            val document = snapshot.documents
            if (document.isNotEmpty()) {
                val user = document[0].toObject(User::class.java) ?: User()
                UserResult.Success(user)
            }
            else
                UserResult.Success(user = null)
        } catch (e: Exception) {
            UserResult.Failure(e)
        }
    }
}