package com.stefanchurch.ferryservicesandroid.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

@Singleton
class AppPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private object Keys {
        val subscribedServiceIds = stringPreferencesKey("subscribed_service_ids_v2")
        val registeredForNotifications = booleanPreferencesKey("registered_for_notifications")
        val installationId = stringPreferencesKey("installation_id")
    }

    val subscribedServiceIds: Flow<Set<Int>> = dataStore.data.map { preferences ->
        preferences[Keys.subscribedServiceIds]
            ?.split(",")
            ?.mapNotNull(String::toIntOrNull)
            ?.toSet()
            ?: emptySet()
    }

    val registeredForNotifications: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[Keys.registeredForNotifications] ?: false
    }

    suspend fun installationId(): UUID {
        val current = dataStore.data.first()[Keys.installationId]
        return current?.let(UUID::fromString) ?: UUID.randomUUID().also { id ->
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
}
