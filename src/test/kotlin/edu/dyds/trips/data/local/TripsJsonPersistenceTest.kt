package edu.dyds.trips.data.local

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import java.io.File
import java.nio.file.Files

class TripsJsonPersistenceTest {
    @Test
    fun `save load update and delete trips`() = runTest {
        val tempFile = Files.createTempFile("trips", ".json").toFile()
        tempFile.deleteOnExit()
        val persistence = TripsJsonPersistence(filePath = tempFile.absolutePath)
        val trip = LocalTripDTO(
            id = "trip-1",
            countryCode = "AR",
            countryName = "Argentina",
            startDate = "2026-06-20",
            endDate = "2026-06-25",
            notes = "Vacaciones",
            createdAt = 1L
        )

        persistence.saveTrip(trip)
        assertEquals(1, persistence.loadTrips().size)
        assertEquals("AR", persistence.loadTrips().first().countryCode)

        persistence.updateTrip(trip.copy(notes = "Actualizado"))
        assertEquals("Actualizado", persistence.loadTrips().first().notes)

        persistence.deleteTrip(trip.id)
        assertTrue(persistence.loadTrips().isEmpty())
        File(tempFile.absolutePath).delete()
    }
}

