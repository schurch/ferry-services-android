package com.stefanchurch.ferryservicesandroid.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataStore: DataStore<Preferences>,
) {
    private object Keys {
        val subscribedServiceIds = stringPreferencesKey("subscribed_service_ids_v2")
        val registeredForNotifications = booleanPreferencesKey("registered_for_notifications")
        val installationId = stringPreferencesKey("installation_id")
    }

    private object LegacyKeys {
        const val sharedPreferencesName = "com.stefanchurch.ferryservices.preferences"
        const val subscribedServiceIds = "subscribedServices"
        const val registeredForNotifications = "createdInstallation"
        const val installationId = "installationID"
    }

    private val legacySharedPreferences by lazy {
        context.getSharedPreferences(LegacyKeys.sharedPreferencesName, Context.MODE_PRIVATE)
    }

    val subscribedServiceIds: Flow<Set<Int>> = dataStore.data.map { preferences ->
        preferences[Keys.subscribedServiceIds]
            ?.let(::parseDelimitedServiceIds)
            ?: legacySharedPreferences.getString(LegacyKeys.subscribedServiceIds, null)
                ?.let(::parseLegacySubscribedServiceIds)
            ?: emptySet()
    }

    val registeredForNotifications: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[Keys.registeredForNotifications]
            ?: legacySharedPreferences.getBoolean(LegacyKeys.registeredForNotifications, false)
    }

    suspend fun installationId(): UUID {
        val current = dataStore.data.first()[Keys.installationId]
        val legacy = legacySharedPreferences.getString(LegacyKeys.installationId, null)
        return current?.let(UUID::fromString)
            ?: legacy?.let(UUID::fromString)?.also { id ->
                dataStore.edit { preferences -> preferences[Keys.installationId] = id.toString() }
            }
            ?: UUID.randomUUID().also { id ->
            dataStore.edit { preferences -> preferences[Keys.installationId] = id.toString() }
        }
    }

    suspend fun setSubscribedServiceIds(ids: Set<Int>) {
        dataStore.edit { preferences ->
            preferences[Keys.subscribedServiceIds] = ids.sorted().joinToString(",")
        }
    }

    suspend fun setRegisteredForNotifications(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.registeredForNotifications] = value
        }
    }

    suspend fun subscribedServiceIdsSnapshot(): Set<Int> = subscribedServiceIds.first()

    private fun parseDelimitedServiceIds(value: String): Set<Int> {
        return value.split(",").mapNotNull(String::toIntOrNull).toSet()
    }

    private fun parseLegacySubscribedServiceIds(value: String): Set<Int> {
        return runCatching {
            Json.decodeFromString<List<Int>>(value).toSet()
        }.getOrDefault(emptySet())
    }
}
