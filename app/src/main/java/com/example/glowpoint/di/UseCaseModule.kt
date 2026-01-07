package com.example.glowpoint.di

import com.example.glowpoint.data.local.LocalDatabase
import com.example.glowpoint.domain.repository.AuthRepository
import com.example.glowpoint.domain.repository.BookingRepository
import com.example.glowpoint.domain.repository.SalonRepository
import com.example.glowpoint.domain.repository.SalonServiceRepository
import com.example.glowpoint.domain.repository.UserRepository
import com.example.glowpoint.domain.usecase.AuthUseCase
import com.example.glowpoint.domain.usecase.BookingUseCase
import com.example.glowpoint.domain.usecase.SalonServiceUseCase
import com.example.glowpoint.domain.usecase.ShopUseCase
import com.example.glowpoint.domain.usecase.UserUseCase
import com.example.glowpoint.util.NetworkUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class UseCaseModule {

    @Provides
    @Singleton
    fun provideAuthUseCase(authRepository: AuthRepository, localDatabase: LocalDatabase, networkUtils: NetworkUtils): AuthUseCase {
        return AuthUseCase(
            authRepository = authRepository,
            localDatabase = localDatabase,
            networkUtils = networkUtils
        )
    }

    @Provides
    @Singleton
    fun provideBookingUseCase(bookingRepository: BookingRepository, localDatabase: LocalDatabase, shopRepository: SalonRepository): BookingUseCase {
        return BookingUseCase(bookingRepository = bookingRepository, localDatabase = localDatabase, shopRepository = shopRepository)
    }

    @Provides
    @Singleton
    fun provideSalonServiceUseCase(salonServiceRepository: SalonServiceRepository, localDatabase: LocalDatabase): SalonServiceUseCase {
        return SalonServiceUseCase(salonServiceRepository, localDatabase)
    }

    @Provides
    @Singleton
    fun provideShopUseCase(shopRepository: SalonRepository, localDatabase: LocalDatabase): ShopUseCase {
        return ShopUseCase(shopRepository, localDatabase)
    }

    @Provides
    @Singleton
    fun provideUserUseCase(userRepository: UserRepository, localDatabase: LocalDatabase): UserUseCase {
        return UserUseCase(userRepository, localDatabase)
    }
}