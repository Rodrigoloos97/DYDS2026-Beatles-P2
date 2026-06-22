package edu.dyds.trips.data.local

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
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

    @Test
    fun `deleteTrip for unknown id should be idempotent and keep stored data`() = runTest {
        // Given
        val trip = LocalTripDTO("trip-1", "AR", "Argentina", "2026-01-01", "2026-01-10", "Test trip", 1L)
        dataSource.saveTrip(trip)

        // When
        val firstDelete = dataSource.deleteTrip("missing-trip")
        val secondDelete = dataSource.deleteTrip("missing-trip")
        val getResult = dataSource.getTrips()

        // Then
        assertTrue("Eliminar id inexistente deberia ser Success", firstDelete.isSuccess)
        assertTrue("Eliminar id inexistente repetido deberia ser Success", secondDelete.isSuccess)
        assertEquals(1, getResult.getOrThrow().size)
        assertEquals("trip-1", getResult.getOrThrow().first().id)
    }

    @Test
    fun `getTrips should fail when persistence file is malformed`() = runTest {
        // Given
        tempFile.writeText("{ json roto")

        // When
        val result = dataSource.getTrips()

        // Then
        assertTrue("La lectura con JSON corrupto deberia fallar", result.isFailure)
    }

    @Test
    fun `saveTrip should recreate persistence file after external deletion`() = runTest {
        // Given
        val trip = LocalTripDTO("trip-1", "AR", "Argentina", "2026-01-01", "2026-01-10", "Test trip", 1L)
        dataSource.saveTrip(trip)
        tempFile.delete()

        // When
        val recreateResult = dataSource.saveTrip(
            LocalTripDTO("trip-2", "BR", "Brazil", "2026-02-01", "2026-02-10", "Trip recreated", 2L)
        )
        val getResult = dataSource.getTrips()

        // Then
        assertTrue("Guardar luego de borrar archivo deberia ser Success", recreateResult.isSuccess)
        assertTrue("El archivo deberia recrearse", tempFile.exists())
        assertEquals(1, getResult.getOrThrow().size)
        assertEquals("trip-2", getResult.getOrThrow().first().id)
    }

    @Test
    fun `saveTrip should fail when file becomes read only`() = runTest {
        // Given
        val trip = LocalTripDTO("trip-1", "AR", "Argentina", "2026-01-01", "2026-01-10", "Original", 1L)
        dataSource.saveTrip(trip)
        val readOnlyApplied = tempFile.setReadOnly()
        if (!readOnlyApplied) return@runTest

        // When
        val saveResult = dataSource.saveTrip(trip.copy(notes = "No deberia persistir"))
        val getResult = dataSource.getTrips()

        // Then
        assertTrue("El guardado en archivo read-only deberia fallar", saveResult.isFailure)
        assertTrue("La lectura deberia seguir funcionando", getResult.isSuccess)
        assertEquals("Original", getResult.getOrThrow().first().notes)

        tempFile.setWritable(true)
    }

    @Test
    fun `getTrips should fail when file was partially written`() = runTest {
        // Given
        tempFile.writeText("""{"trips":[{"id":"trip-1"""")

        // When
        val result = dataSource.getTrips()

        // Then
        assertTrue("Leer un archivo truncado deberia fallar", result.isFailure)
    }

    @Test
    fun `saveTrip should fail when existing file is truncated`() = runTest {
        // Given
        tempFile.writeText("""{"trips":[{"id":"trip-1"""")
        val trip = LocalTripDTO("trip-2", "BR", "Brazil", "2026-02-01", "2026-02-10", "Trip", 2L)

        // When
        val saveResult = dataSource.saveTrip(trip)

        // Then
        assertTrue("Guardar sobre JSON truncado deberia fallar", saveResult.isFailure)
    }

    @Test
    fun `concurrent saveTrip calls on same id should keep one valid trip`() = runTest {
        // Given
        val trip = LocalTripDTO("trip-1", "AR", "Argentina", "2026-01-01", "2026-01-10", "Base", 1L)

        // When
        coroutineScope {
            launch { dataSource.saveTrip(trip.copy(notes = "A")) }
            launch { dataSource.saveTrip(trip.copy(notes = "B")) }
        }
        val getResult = dataSource.getTrips()

        // Then
        assertTrue("La lectura final deberia ser Success", getResult.isSuccess)
        val trips = getResult.getOrThrow()
        assertEquals(1, trips.size)
        assertTrue(trips.first().notes == "A" || trips.first().notes == "B")
    }

    @Test
    fun `concurrent update and delete should keep datasource readable`() = runTest {
        // Given
        val trip = LocalTripDTO("trip-1", "AR", "Argentina", "2026-01-01", "2026-01-10", "Original", 1L)
        dataSource.saveTrip(trip)

        // When
        coroutineScope {
            launch { dataSource.updateTrip(trip.copy(notes = "Actualizado")) }
            launch { dataSource.deleteTrip("trip-1") }
        }
        val getResult = dataSource.getTrips()

        // Then
        assertTrue("La lectura despues de concurrencia deberia ser Success", getResult.isSuccess)
        val trips = getResult.getOrThrow()
        assertTrue(trips.isEmpty() || (trips.size == 1 && trips.first().id == "trip-1"))
    }
}
