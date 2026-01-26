package com.example.glowpoint.domain.repository

import com.example.glowpoint.domain.model.result.TokenResult

interface TokenRepository {
    suspend fun saveFcmToken(token: String, userId: String): TokenResult
    suspend fun getShopFcmToken(shopId: String): TokenResult
}