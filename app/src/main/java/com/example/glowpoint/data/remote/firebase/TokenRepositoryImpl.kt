package com.example.glowpoint.data.remote.firebase

import com.example.glowpoint.domain.model.TokenResult
import com.example.glowpoint.domain.repository.TokenRepository
import com.example.glowpoint.util.TokenRepositoryConstant
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class TokenRepositoryImpl @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore
): TokenRepository {
    override suspend fun saveFcmToken(
        token: String,
        userId: String
    ): TokenResult {
        return try {
            val data = mapOf(
                TokenRepositoryConstant.FCM_TOKEN to token,
                TokenRepositoryConstant.UPDATE_AT to FieldValue.serverTimestamp()
            )

            firebaseFirestore
                .collection(TokenRepositoryConstant.COLLECTION_NAME)
                .document(userId)
                .set(data, SetOptions.merge())
                .await()

            TokenResult.Success(token)

        } catch (e: Exception) {
            TokenResult.Failure(e)
        }
    }


    override suspend fun getShopFcmToken(shopId: String): TokenResult {
        return try {
            val snapshot = firebaseFirestore
                .collection(TokenRepositoryConstant.COLLECTION_NAME)
                .document(shopId)
                .get()
                .await()

            if (!snapshot.exists()) {
                return TokenResult.Failure(Exception("Token Not Found."))
            }

            val token = snapshot.getString(TokenRepositoryConstant.FCM_TOKEN)

            if (token.isNullOrBlank()) {
                return TokenResult.Failure(Exception("Token is already empty."))
            }

            TokenResult.Success(token)

        } catch (e: Exception) {
            TokenResult.Failure(e)
        }
    }

}