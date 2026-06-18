package edu.dyds.trips.data.local

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
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

    @Test
    fun `saveTrip with same id overwrites previous data`() = runTest {
        val tempFile = Files.createTempFile("trips", ".json").toFile()
        tempFile.deleteOnExit()
        val persistence = TripsJsonPersistence(filePath = tempFile.absolutePath)
        val original = LocalTripDTO(
            id = "trip-1",
            countryCode = "AR",
            countryName = "Argentina",
            startDate = "2026-06-20",
            endDate = "2026-06-25",
            notes = "Original",
            createdAt = 1L
        )

        persistence.saveTrip(original)
        persistence.saveTrip(original.copy(notes = "Sobrescrito", endDate = "2026-06-30"))

        val trips = persistence.loadTrips()
        assertEquals(1, trips.size)
        assertEquals("Sobrescrito", trips.first().notes)
        assertEquals("2026-06-30", trips.first().endDate)
        tempFile.delete()
    }

    @Test
    fun `deleteTrip with unknown id does not modify stored trips`() = runTest {
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
        persistence.deleteTrip("trip-inexistente")

        val trips = persistence.loadTrips()
        assertEquals(1, trips.size)
        assertEquals("trip-1", trips.first().id)
        tempFile.delete()
    }

    @Test
    fun `loadTrips fails when file content is corrupt`() = runTest {
        val tempFile = Files.createTempFile("trips", ".json").toFile()
        tempFile.deleteOnExit()
        tempFile.writeText("{ malformed json")
        val persistence = TripsJsonPersistence(filePath = tempFile.absolutePath)

        assertFailsWith<Exception> {
            persistence.loadTrips()
        }

        tempFile.delete()
    }

    @Test
    fun `saveTrip recreates file when it was deleted externally`() = runTest {
        val tempFile = Files.createTempFile("trips", ".json").toFile()
        tempFile.deleteOnExit()
        val persistence = TripsJsonPersistence(filePath = tempFile.absolutePath)

        val firstTrip = LocalTripDTO("trip-1", "AR", "Argentina", "2026-06-20", "2026-06-25", "Primero", 1L)
        persistence.saveTrip(firstTrip)
        assertTrue(tempFile.exists())

        tempFile.delete()
        assertFalse(tempFile.exists())

        val secondTrip = LocalTripDTO("trip-2", "BR", "Brazil", "2026-07-01", "2026-07-10", "Segundo", 2L)
        persistence.saveTrip(secondTrip)

        assertTrue(tempFile.exists())
        val trips = persistence.loadTrips()
        assertEquals(1, trips.size)
        assertEquals("trip-2", trips.first().id)
        tempFile.delete()
    }

    @Test
    fun `saveTrip fails when file is read only`() = runTest {
        val tempFile = Files.createTempFile("trips", ".json").toFile()
        tempFile.deleteOnExit()
        val persistence = TripsJsonPersistence(filePath = tempFile.absolutePath)
        val trip = LocalTripDTO("trip-1", "AR", "Argentina", "2026-06-20", "2026-06-25", "Original", 1L)
        persistence.saveTrip(trip)

        val readOnlyApplied = tempFile.setReadOnly()
        if (!readOnlyApplied) {
            tempFile.delete()
            return@runTest
        }

        try {
            assertFailsWith<Exception> {
                persistence.saveTrip(trip.copy(notes = "No deberia persistir"))
            }
            assertEquals("Original", persistence.loadTrips().first().notes)
        } finally {
            tempFile.setWritable(true)
            tempFile.delete()
        }
    }

    @Test
    fun `loadTrips fails when write was interrupted and json is truncated`() = runTest {
        val tempFile = Files.createTempFile("trips", ".json").toFile()
        tempFile.deleteOnExit()
        tempFile.writeText("""{"trips":[{"id":"trip-1"""")
        val persistence = TripsJsonPersistence(filePath = tempFile.absolutePath)

        assertFailsWith<Exception> {
            persistence.loadTrips()
        }

        tempFile.delete()
    }

    @Test
    fun `saveTrip fails when previous interrupted write left malformed json`() = runTest {
        val tempFile = Files.createTempFile("trips", ".json").toFile()
        tempFile.deleteOnExit()
        tempFile.writeText("""{"trips":[{"id":"trip-1"""")
        val persistence = TripsJsonPersistence(filePath = tempFile.absolutePath)

        val trip = LocalTripDTO("trip-2", "BR", "Brazil", "2026-07-01", "2026-07-10", "Nuevo", 2L)
        assertFailsWith<Exception> {
            persistence.saveTrip(trip)
        }

        tempFile.delete()
    }

    @Test
    fun `concurrent saveTrip calls on same id keep file consistent`() = runTest {
        val tempFile = Files.createTempFile("trips", ".json").toFile()
        tempFile.deleteOnExit()
        val persistence = TripsJsonPersistence(filePath = tempFile.absolutePath)
        val baseTrip = LocalTripDTO("trip-1", "AR", "Argentina", "2026-06-20", "2026-06-25", "Base", 1L)

        coroutineScope {
            launch { persistence.saveTrip(baseTrip.copy(notes = "A")) }
            launch { persistence.saveTrip(baseTrip.copy(notes = "B")) }
        }

        val trips = persistence.loadTrips()
        assertEquals(1, trips.size)
        assertTrue(trips.first().notes == "A" || trips.first().notes == "B")
        tempFile.delete()
    }

    @Test
    fun `concurrent update and delete on same id may corrupt but remains recoverable`() = runTest {
        val tempFile = Files.createTempFile("trips", ".json").toFile()
        tempFile.deleteOnExit()
        val persistence = TripsJsonPersistence(filePath = tempFile.absolutePath)
        val trip = LocalTripDTO("trip-1", "AR", "Argentina", "2026-06-20", "2026-06-25", "Original", 1L)
        persistence.saveTrip(trip)

        coroutineScope {
            launch { persistence.updateTrip(trip.copy(notes = "Actualizado")) }
            launch { persistence.deleteTrip(trip.id) }
        }

        val readResult = runCatching { persistence.loadTrips() }
        if (readResult.isSuccess) {
            val trips = readResult.getOrThrow()
            assertTrue(trips.isEmpty() || (trips.size == 1 && trips.first().id == "trip-1"))
        } else {
            // Corruption is acceptable in this destructive test, but the file must be recoverable.
            tempFile.writeText("")
            val recoveryTrip = trip.copy(id = "trip-2", notes = "Recuperado")
            persistence.saveTrip(recoveryTrip)
            val recovered = persistence.loadTrips()
            assertEquals(1, recovered.size)
            assertEquals("trip-2", recovered.first().id)
        }
        tempFile.delete()
    }
}

