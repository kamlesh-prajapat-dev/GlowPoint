package com.example.glowpoint.data.local

import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.glowpoint.data.models.ShopDetails
import com.example.glowpoint.data.models.FetchedServiceItem
import com.example.glowpoint.data.models.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalDatabase @Inject constructor(
    private val sharedPreferences: SharedPreferences
) {

    private val gson = Gson()

    companion object {
        private const val SELECTED_LANGUAGE = "Locale.Helper.Selected.Language"
        private const val LATITUDE = "latitude"
        private const val LONGITUDE = "longitude"
        private const val GEOCODER = "geocoder" // City name
        private const val AREA = "area" // Address of user
        private const val USER = "user_data"
        private const val SALON_MODEL = "salons"
        private const val LAST_CACHE_TIMESTAMP_OF_SALON_MEN_SERVICES = "last_cache_timestamp_of_men_services"
        private const val LAST_CACHE_TIMESTAMP_OF_SALON_WOMEN_SERVICES = "last_cache_timestamp_of_women_services"
        private const val LAST_CACHE_TIMESTAMP_OF_SALONS = "last_cache_timestamp_of_salons"
        private const val IS_NEW_USER = "is_new_user"
        private const val MEN_SERVICES = "men_services"
        private const val WOMEN_SERVICES = "women_services"
    }

    fun getLanguage(): String? {
        return sharedPreferences.getString(SELECTED_LANGUAGE, null)
    }

    fun setLanguage(language: String?) {
        sharedPreferences.edit {
            putString(SELECTED_LANGUAGE, language)
        }
    }

    fun setMenServices(services: List<FetchedServiceItem>) {
        val json = gson.toJson(services)
        sharedPreferences.edit { putString(MEN_SERVICES, json) }
    }

    fun setWomenServices(services: List<FetchedServiceItem>) {
        val json = gson.toJson(services)
        sharedPreferences.edit { putString(WOMEN_SERVICES, json) }
    }

    fun getMenServices(): List<FetchedServiceItem> {
        val json = sharedPreferences.getString(MEN_SERVICES, null) ?: return emptyList()
        val type = object : TypeToken<List<FetchedServiceItem>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    fun getWomenServices(): List<FetchedServiceItem> {
        val json = sharedPreferences.getString(WOMEN_SERVICES, null) ?: return emptyList()
        val type = object : TypeToken<List<FetchedServiceItem>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    fun setUser(user: User) {
        val json = gson.toJson(user)
        sharedPreferences.edit { putString(USER, json) }
    }

    fun getUser(): User? {
        val json = sharedPreferences.getString(USER, null) ?: return null
        return gson.fromJson(json, User::class.java)
    }

    fun setSalonModel(shopDetails: List<ShopDetails>) {
        val json = gson.toJson(shopDetails)
        sharedPreferences.edit { putString(SALON_MODEL, json) }
    }

    fun getSalonModel(): List<ShopDetails> {
        val json = sharedPreferences.getString(SALON_MODEL, null) ?: return emptyList()
        val type = object : TypeToken<List<ShopDetails>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    var latitude: Float
        get() = sharedPreferences.getFloat(LATITUDE, 0f)
        set(value) {
            sharedPreferences.edit { putFloat(LATITUDE, value) }
        }

    var longitude: Float
        get() = sharedPreferences.getFloat(LONGITUDE, 0f)
        set(value) {
            sharedPreferences.edit { putFloat(LONGITUDE, value) }
        }

    var lastCacheTimestampOfSalonMenServices: Long
        get() = sharedPreferences.getLong(LAST_CACHE_TIMESTAMP_OF_SALON_MEN_SERVICES, 0L)
        set(value) {
            sharedPreferences.edit { putLong(LAST_CACHE_TIMESTAMP_OF_SALON_MEN_SERVICES, value) }
        }

    var lastCacheTimestampOfSalonWomenServices: Long
        get() = sharedPreferences.getLong(LAST_CACHE_TIMESTAMP_OF_SALON_WOMEN_SERVICES, 0L)
        set(value) {
            sharedPreferences.edit { putLong(LAST_CACHE_TIMESTAMP_OF_SALON_WOMEN_SERVICES, value) }
        }

    var lastCacheTimestampOfSalons: Long
        get() = sharedPreferences.getLong(LAST_CACHE_TIMESTAMP_OF_SALONS, 0L)
        set(value) {
            sharedPreferences.edit { putLong(LAST_CACHE_TIMESTAMP_OF_SALONS, value) }
        }

    fun isLocationSet(): Boolean {
        return latitude != 0f && longitude != 0f
    }

    fun setAreaOfUser(area: String) {
        sharedPreferences.edit { putString(AREA, area) }
    }

    fun getAreaOfUser(): String {
        return sharedPreferences.getString(AREA, "") ?: ""
    }

    fun getLocationName(): String {
        return sharedPreferences.getString(GEOCODER, "") ?: ""
    }

    fun setLocationName(location: String) {
        sharedPreferences.edit { putString(GEOCODER, location) }
    }

    fun isNewUser(): Boolean {
        val isNew = sharedPreferences.getBoolean(IS_NEW_USER, true)
        if (isNew) {
            sharedPreferences.edit { putBoolean(IS_NEW_USER, false) }
        }
        return isNew
    }
}