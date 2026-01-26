package com.example.glowpoint.data.remote.repository

import com.example.glowpoint.data.remote.exception.EmptyDataException
import com.example.glowpoint.domain.model.result.TokenResult
import com.example.glowpoint.domain.repository.TokenRepository
import com.example.glowpoint.util.TokenRepositoryConstant
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class TokenRepositoryImpl @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore
): TokenRepository {
    override suspend fun saveFcmToken(
        token: String,
        userId: String
    ): TokenResult {

        if (userId.isBlank() || token.isBlank()) {
            return TokenResult.Failure(
                IllegalArgumentException("UserId=$userId and token=$token cannot be blank")
            )
        }

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

        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            TokenResult.Failure(e)
        }
    }

    override suspend fun getShopFcmToken(shopId: String): TokenResult {

        if (shopId.isBlank()) {
            return TokenResult.Failure(
                IllegalArgumentException("ShopId cannot be blank")
            )
        }

        return try {
            val snapshot = firebaseFirestore
                .collection(TokenRepositoryConstant.COLLECTION_NAME)
                .document(shopId)
                .get()
                .await()

            if (!snapshot.exists()) {
                return TokenResult.Failure(EmptyDataException("Token not found."))
            }

            val token = snapshot.getString(TokenRepositoryConstant.FCM_TOKEN)

            if (token.isNullOrBlank()) {
                return TokenResult.Failure(EmptyDataException("Token is already empty."))
            }

            TokenResult.Success(token)

        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            TokenResult.Failure(e)
        }
    }
}