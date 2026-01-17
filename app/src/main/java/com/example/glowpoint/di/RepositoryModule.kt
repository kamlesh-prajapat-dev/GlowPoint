package com.example.glowpoint.di

import com.example.glowpoint.data.remote.firebase.AuthRepositoryImpl
import com.example.glowpoint.data.remote.firebase.BookingRepositoryImpl
import com.example.glowpoint.data.remote.firebase.ShopRepositoryImpl
import com.example.glowpoint.data.remote.firebase.SalonServiceRepositoryImpl
import com.example.glowpoint.data.remote.firebase.TokenRepositoryImpl
import com.example.glowpoint.data.remote.firebase.UserRepositoryImpl
import com.example.glowpoint.domain.repository.AuthRepository
import com.example.glowpoint.domain.repository.BookingRepository
import com.example.glowpoint.domain.repository.SalonRepository
import com.example.glowpoint.domain.repository.SalonServiceRepository
import com.example.glowpoint.domain.repository.TokenRepository
import com.example.glowpoint.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    abstract fun bindSalonRepository(impl: ShopRepositoryImpl): SalonRepository

    @Binds
    abstract fun bindSalonServiceRepository(impl: SalonServiceRepositoryImpl): SalonServiceRepository

    @Binds
    abstract fun bindBookingRepository(impl: BookingRepositoryImpl): BookingRepository

    @Binds
    abstract fun bindTokenRepository(impl: TokenRepositoryImpl): TokenRepository
}