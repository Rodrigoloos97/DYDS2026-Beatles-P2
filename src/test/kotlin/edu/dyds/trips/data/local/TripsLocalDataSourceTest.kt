package edu.dyds.trips.data.local

import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class TripsLocalDataSourceTest {

    private lateinit var tempFile: File
    private lateinit var persistence: TripsJsonPersistence
    private lateinit var dataSource: TripsLocalDataSource

    @Before
    fun setUp() {
        // Crea un archivo temporal para cada test para asegurar el aislamiento
        tempFile = File.createTempFile("test_trips", ".json")
        persistence = TripsJsonPersistence(filePath = tempFile.absolutePath)
        dataSource = TripsLocalDataSourceImpl(persistence)
    }

    @After
    fun tearDown() {
        // Limpia el archivo temporal después de cada test
        tempFile.delete()
    }

    @Test
    fun `getTrips on a new file should return an empty list`() = runTest {
        // When
        val result = dataSource.getTrips()

        // Then
        assertTrue("El resultado deberia ser Success", result.isSuccess)
        assertTrue("La lista de viajes deberia estar vacia", result.getOrThrow().isEmpty())
    }

    @Test
    fun `saveTrip and getTrips should save and retrieve a trip`() = runTest {
        // Given
        val newTrip = LocalTripDTO("trip-1", "AR", "Argentina", "2026-01-01", "2026-01-10", "Test trip", 1L)

        // When
        val saveResult = dataSource.saveTrip(newTrip)
        val getResult = dataSource.getTrips()

        // Then
        assertTrue("El guardado deberia ser Success", saveResult.isSuccess)
        assertTrue("La obtencion deberia ser Success", getResult.isSuccess)
        val trips = getResult.getOrThrow()
        assertEquals(1, trips.size)
        assertEquals("trip-1", trips.first().id)
    }

    @Test
    fun `updateTrip should modify an existing trip`() = runTest {
        // Given
        val originalTrip = LocalTripDTO("trip-1", "AR", "Argentina", "2026-01-01", "2026-01-10", "Original notes", 1L)
        dataSource.saveTrip(originalTrip)

        // When
        val updatedTrip = originalTrip.copy(notes = "Updated notes")
        val updateResult = dataSource.updateTrip(updatedTrip)
        val getResult = dataSource.getTrips()

        // Then
        assertTrue("La actualizacion deberia ser Success", updateResult.isSuccess)
        val trips = getResult.getOrThrow()
        assertEquals(1, trips.size)
        assertEquals("Updated notes", trips.first().notes)
    }

    @Test
    fun `deleteTrip should remove an existing trip`() = runTest {
        // Given
        val trip = LocalTripDTO("trip-1", "AR", "Argentina", "2026-01-01", "2026-01-10", "Test trip", 1L)
        dataSource.saveTrip(trip)

        // When
        val deleteResult = dataSource.deleteTrip("trip-1")
        val getResult = dataSource.getTrips()

        // Then
        assertTrue("La eliminacion deberia ser Success", deleteResult.isSuccess)
        assertTrue("La lista de viajes deberia estar vacia", getResult.getOrThrow().isEmpty())
    }
}
