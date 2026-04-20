package com.stefanchurch.ferryservicesandroid

import com.stefanchurch.ferryservicesandroid.data.model.ServiceStatus
import com.stefanchurch.ferryservicesandroid.data.model.ServiceStatusSerializer
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class ServiceStatusSerializerTest {
    @Test
    fun `maps integer values to service status`() {
        val json = Json

        assertEquals(ServiceStatus.NORMAL, json.decodeFromString(ServiceStatusSerializer, "0"))
        assertEquals(ServiceStatus.DISRUPTED, json.decodeFromString(ServiceStatusSerializer, "1"))
        assertEquals(ServiceStatus.CANCELLED, json.decodeFromString(ServiceStatusSerializer, "2"))
        assertEquals(ServiceStatus.UNKNOWN, json.decodeFromString(ServiceStatusSerializer, "-99"))
    }
}
