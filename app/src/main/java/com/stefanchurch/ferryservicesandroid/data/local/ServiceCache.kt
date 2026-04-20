package com.stefanchurch.ferryservicesandroid.data.local

import android.content.Context
import com.stefanchurch.ferryservicesandroid.data.model.Service
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

@Singleton
class ServiceCache @Inject constructor(
    @ApplicationContext private val context: Context,
    private val json: Json,
) {
    private val serializer = ListSerializer(Service.serializer())
    private val cacheFile: File = File(context.cacheDir, "services.json")

    suspend fun readBundledServices(): List<Service> {
        return context.assets.open("services.json").bufferedReader().use {
            json.decodeFromString(serializer, it.readText())
        }
    }

    suspend fun readCachedServices(): List<Service>? {
        if (!cacheFile.exists()) return null
        return json.decodeFromString(serializer, cacheFile.readText())
    }

    suspend fun writeServices(services: List<Service>) {
        cacheFile.writeText(json.encodeToString(serializer, services))
    }
}
