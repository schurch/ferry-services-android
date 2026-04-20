package com.stefanchurch.ferryservicesandroid

import com.stefanchurch.ferryservicesandroid.data.model.Service
import com.stefanchurch.ferryservicesandroid.data.model.ServiceStatus
import com.stefanchurch.ferryservicesandroid.ui.model.globallySharedDepartureNote
import com.stefanchurch.ferryservicesandroid.ui.model.groupedDepartureSections
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class UiModelsTest {
    @Test
    fun `global shared note only returns when every departure matches`() {
        val service = testService(
            notes = listOf("Weather dependent", "Weather dependent"),
        )

        assertEquals("Weather dependent", service.globallySharedDepartureNote())
    }

    @Test
    fun `departure sections split by destination`() {
        val service = testService(
            notes = listOf(null, "Different"),
            destinationNames = listOf("Brodick", "Campbeltown"),
        )

        val sections = service.groupedDepartureSections(Instant.parse("2024-01-01T00:00:00Z"))

        assertEquals(listOf("Brodick", "Campbeltown"), sections.map { it.destinationName })
        assertNull(service.globallySharedDepartureNote())
    }

    private fun testService(
        notes: List<String?>,
        destinationNames: List<String> = listOf("Brodick", "Brodick"),
    ): Service {
        val departures = notes.mapIndexed { index, note ->
            Service.Location.ScheduledDeparture(
                departure = "2024-01-01T0${index + 8}:00:00Z",
                arrival = "2024-01-01T0${index + 9}:00:00Z",
                destination = Service.Location.ScheduledDeparture.DepartureLocation(
                    id = index + 10,
                    name = destinationNames[index],
                    latitude = 0.0,
                    longitude = 0.0,
                ),
                note = note,
            )
        }

        return Service(
            serviceId = 1,
            status = ServiceStatus.DISRUPTED,
            area = "Arran",
            route = "Ardrossan - Brodick",
            locations = listOf(
                Service.Location(
                    id = 1,
                    name = "Ardrossan",
                    latitude = 0.0,
                    longitude = 0.0,
                    scheduledDepartures = departures,
                ),
            ),
        )
    }
}
