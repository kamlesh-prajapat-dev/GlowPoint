package com.example.glowpoint.data.remote.repository

import com.example.glowpoint.data.models.User
import com.example.glowpoint.domain.model.result.UserResult
import com.example.glowpoint.domain.repository.UserRepository
import com.example.glowpoint.util.UserRepositoryConstant
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserRepository {

    override suspend fun createUser(user: User): UserResult {

        if (user.uid.isBlank()) {
            return UserResult.Failure(
                IllegalArgumentException("User id cannot be blank")
            )
        }

        return try {
            firestore.collection(UserRepositoryConstant.COLLECTION_NAME).document(user.uid)
                .set(user)
                .await()

            UserResult.Success(user)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            UserResult.Failure(e)
        }
    }

    override suspend fun getUserByPhoneNumber(phoneNumber: String): UserResult {

        if (phoneNumber.isBlank()) {
            return UserResult.Failure(
                IllegalArgumentException("Phone number cannot be blank")
            )
        }

        return try {
            val snapshot = FirebaseFirestore.getInstance()
                .collection(UserRepositoryConstant.COLLECTION_NAME)
                .whereEqualTo(UserRepositoryConstant.PHONE_NUMBER, phoneNumber)
                .limit(1)
                .get()
                .await()

            val document = snapshot.documents.firstOrNull()

            val user = document?.toObject(User::class.java)

            UserResult.Success(user)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            UserResult.Failure(e)
        }
    }
}