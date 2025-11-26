package com.example.glowpoint.di

import android.content.Context
import android.content.SharedPreferences
import com.example.glowpoint.data.database.local.LocalDatabase
import com.example.glowpoint.util.NetworkUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideLocalHelper(sharedPreferences: SharedPreferences): LocalDatabase {
        return LocalDatabase(sharedPreferences)
    }

    @Provides
    @Singleton
    fun provideNetworkUtils(@ApplicationContext context: Context): NetworkUtils {
        return NetworkUtils(context)
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("GlowPointPrefs", Context.MODE_PRIVATE)
    }
}