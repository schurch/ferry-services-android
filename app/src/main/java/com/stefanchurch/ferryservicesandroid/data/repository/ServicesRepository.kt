package com.stefanchurch.ferryservicesandroid.data.repository

import com.stefanchurch.ferryservicesandroid.data.local.ServiceCache
import com.stefanchurch.ferryservicesandroid.data.model.PushStatus
import com.stefanchurch.ferryservicesandroid.data.model.Service
import com.stefanchurch.ferryservicesandroid.data.network.CreateInstallationBody
import com.stefanchurch.ferryservicesandroid.data.network.CreateInstallationServiceBody
import com.stefanchurch.ferryservicesandroid.data.network.FerryApi
import com.stefanchurch.ferryservicesandroid.data.preferences.AppPreferences
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

@Singleton
class ServicesRepository @Inject constructor(
    private val api: FerryApi,
    private val cache: ServiceCache,
    private val preferences: AppPreferences,
) {
    val subscribedServiceIds: Flow<Set<Int>> = preferences.subscribedServiceIds
    val isRegisteredForNotifications: Flow<Boolean> = preferences.registeredForNotifications

    suspend fun fetchServices(): List<Service> {
        return api.fetchServices().also { services ->
            cache.writeServices(services)
        }
    }

    suspend fun fetchDefaultServices(): List<Service> {
        return cache.readCachedServices() ?: cache.readBundledServices()
    }

    suspend fun fetchService(serviceId: Int, date: LocalDate): Service {
        return api.fetchService(serviceId, date.toString())
    }

    suspend fun fetchService(serviceId: Int): Service {
        return api.fetchService(serviceId)
    }

    suspend fun toggleSubscription(serviceId: Int, subscribed: Boolean): Set<Int> {
        val installationId = preferences.installationId()
        val updatedIds = preferences.subscribedServiceIdsSnapshot().toMutableSet()
        if (subscribed) {
            api.addService(installationId, CreateInstallationServiceBody(serviceId))
            updatedIds += serviceId
        } else {
            api.removeService(installationId, serviceId)
            updatedIds -= serviceId
        }
        return updatedIds.toSet().also { ids ->
            preferences.setSubscribedServiceIds(ids)
        }
    }

    suspend fun registerInstallation(deviceToken: String) {
        api.createInstallation(preferences.installationId(), CreateInstallationBody(deviceToken))
        preferences.setRegisteredForNotifications(true)
    }

    suspend fun getPushStatus(): Boolean {
        return api.getPushStatus(preferences.installationId()).enabled
    }

    suspend fun updatePushStatus(enabled: Boolean) {
        api.updatePushStatus(preferences.installationId(), PushStatus(enabled))
    }

    suspend fun subscribedServiceIdsSnapshot(): Set<Int> = preferences.subscribedServiceIdsSnapshot()
}
