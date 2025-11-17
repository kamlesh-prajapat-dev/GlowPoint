package com.example.glowpoint.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import java.util.Locale

object LocaleHelper {

    private const val SELECTED_LANGUAGE = "Locale.Helper.Selected.Language"

    // This is called on every MainActivity start.
    fun onAttach(context: Context): Context {
        // Get the persisted language, or fall back to the device's default language.
        val lang = getLanguage(context) ?: Locale.getDefault().language
        return updateResources(context, lang)
    }

    // This returns the persisted language, or null if no language has been chosen by the user.
    fun getLanguage(context: Context): String? {
        val preferences = getPreferences(context)
        return preferences.getString(SELECTED_LANGUAGE, null)
    }

    // This should be called when the user explicitly chooses a language.
    // It persists the choice and updates the resources.
    fun setLocale(context: Context, language: String?): Context {
        persist(context, language)
        return updateResources(context, language)
    }

    private fun persist(context: Context, language: String?) {
        val preferences = getPreferences(context)
        preferences.edit {
            putString(SELECTED_LANGUAGE, language)
        }
    }

    private fun updateResources(context: Context, language: String?): Context {
        val locale = if (language != null) Locale.forLanguageTag(language) else Locale.getDefault()
        Locale.setDefault(locale)
        val configuration = context.resources.configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        return context.createConfigurationContext(configuration)
    }

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences("GlowPointPrefs", Context.MODE_PRIVATE)
    }
}