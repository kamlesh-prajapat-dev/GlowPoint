package com.example.glowpoint.di

import com.example.glowpoint.data.repository.AuthRepositoryImpl
import com.example.glowpoint.data.repository.UserRepositoryImpl
import com.example.glowpoint.domain.repository.AuthRepository
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
}