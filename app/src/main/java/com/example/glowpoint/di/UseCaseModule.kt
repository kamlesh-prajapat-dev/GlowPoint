package com.example.glowpoint.di

import com.example.glowpoint.data.local.LocalDatabase
import com.example.glowpoint.domain.repository.AuthRepository
import com.example.glowpoint.domain.repository.BookingRepository
import com.example.glowpoint.domain.repository.LocationRepository
import com.example.glowpoint.domain.repository.NotificationRepository
import com.example.glowpoint.domain.repository.SalonRepository
import com.example.glowpoint.domain.repository.SalonServiceRepository
import com.example.glowpoint.domain.repository.TokenRepository
import com.example.glowpoint.domain.repository.UserRepository
import com.example.glowpoint.domain.usecase.AuthUseCase
import com.example.glowpoint.domain.usecase.BookingUseCase
import com.example.glowpoint.domain.usecase.SalonServiceUseCase
import com.example.glowpoint.domain.usecase.ShopUseCase
import com.example.glowpoint.domain.usecase.TokenUseCase
import com.example.glowpoint.domain.usecase.UserUseCase
import com.example.glowpoint.util.NetworkUtils
import com.example.glowpoint.workerscheduler.SenderNotificationWorkerScheduler
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
    fun provideAuthUseCase(
        authRepository: AuthRepository,
        localDatabase: LocalDatabase,
        networkUtils: NetworkUtils
    ): AuthUseCase {
        return AuthUseCase(
            authRepository = authRepository,
            localDatabase = localDatabase,
            networkUtils = networkUtils
        )
    }

    @Provides
    @Singleton
    fun provideBookingUseCase(
        bookingRepository: BookingRepository,
        localDatabase: LocalDatabase,
        shopRepository: SalonRepository,
        tokenRepository: TokenRepository,
        notificationRepository: NotificationRepository,
        senderNotificationWorkerScheduler: SenderNotificationWorkerScheduler
    ): BookingUseCase {
        return BookingUseCase(
            bookingRepository = bookingRepository,
            localDatabase = localDatabase,
            shopRepository = shopRepository,
            tokenRepository = tokenRepository,
            notificationRepository = notificationRepository,
            senderNotificationWorkerScheduler = senderNotificationWorkerScheduler
        )
    }

    @Provides
    @Singleton
    fun provideSalonServiceUseCase(
        salonServiceRepository: SalonServiceRepository,
        localDatabase: LocalDatabase
    ): SalonServiceUseCase {
        return SalonServiceUseCase(salonServiceRepository, localDatabase)
    }

    @Provides
    @Singleton
    fun provideShopUseCase(
        shopRepository: SalonRepository,
        localDatabase: LocalDatabase
    ): ShopUseCase {
        return ShopUseCase(shopRepository, localDatabase)
    }

    @Provides
    @Singleton
    fun provideUserUseCase(
        userRepository: UserRepository,
        localDatabase: LocalDatabase,
        tokenRepository: TokenRepository,
        locationRepository: LocationRepository
    ): UserUseCase {
        return UserUseCase(
            userRepository = userRepository,
            tokenRepository = tokenRepository,
            localDatabase = localDatabase,
            locationRepository = locationRepository
        )
    }

    @Provides
    @Singleton
    fun provideTokenUseCase(
        tokenRepository: TokenRepository,
        localDatabase: LocalDatabase
    ): TokenUseCase {
        return TokenUseCase(
            tokenRepository = tokenRepository,
            localDatabase = localDatabase
        )
    }
}