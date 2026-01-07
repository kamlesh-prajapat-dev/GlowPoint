package com.example.glowpoint.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.glowpoint.data.local.LocalDatabase
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

object LocaleHelper {

    fun onAttach(context: Context): Context {
        // Get the persisted language, or fall back to the device's default language.
        val lang = LocalDatabase(sharedPreferences = context.getSharedPreferences("GlowPointPrefs", Context.MODE_PRIVATE)).getLanguage() ?: Locale.getDefault().language
        return updateResources(context, lang)
    }

    fun setLocale(context: Context, language: String?): Context {
        LocalDatabase(sharedPreferences = context.getSharedPreferences("GlowPointPrefs", Context.MODE_PRIVATE)).setLanguage(language)
        return updateResources(context, language)
    }

    private fun updateResources(context: Context, language: String?): Context {
        val locale = if (language != null) Locale.forLanguageTag(language) else Locale.getDefault()
        Locale.setDefault(locale)
        val configuration = context.resources.configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        return context.createConfigurationContext(configuration)
    }
}