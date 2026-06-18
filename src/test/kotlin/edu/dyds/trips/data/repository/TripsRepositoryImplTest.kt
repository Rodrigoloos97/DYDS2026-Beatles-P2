package edu.dyds.trips.data.repository

import edu.dyds.trips.data.local.LocalTripDTO
import edu.dyds.trips.data.local.TripsLocalDataSource
import edu.dyds.trips.domain.entity.Trip
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Fake implementation of TripsLocalDataSource for testing the repository.
 * This fake operates on an in-memory list instead of a file.
 */
class FakeTripsLocalDataSource : TripsLocalDataSource {
    private val trips = mutableListOf<LocalTripDTO>()

    override suspend fun getTrips(): Result<List<LocalTripDTO>> {
        return Result.success(trips.toList())
    }

    override suspend fun saveTrip(trip: LocalTripDTO): Result<Unit> {
        trips.add(trip)
        return Result.success(Unit)
    }

    override suspend fun updateTrip(trip: LocalTripDTO): Result<Unit> {
        val index = trips.indexOfFirst { it.id == trip.id }
        if (index != -1) {
            trips[index] = trip
        }
        return Result.success(Unit)
    }

    override suspend fun deleteTrip(id: String): Result<Unit> {
        trips.removeAll { it.id == id }
        return Result.success(Unit)
    }
}

class TripsRepositoryImplTest {

    private lateinit var repository: TripsRepositoryImpl
    private lateinit var fakeDataSource: FakeTripsLocalDataSource

    @Before
    fun setUp() {
        fakeDataSource = FakeTripsLocalDataSource()
        repository = TripsRepositoryImpl(fakeDataSource)
    }

    @Test
    fun `getTrips should return a list of domain trips`() = runTest {
        // Given
        val tripDto = LocalTripDTO("trip-1", "AR", "Argentina", "2026-01-01", "2026-01-10", "Notes", 1L)
        fakeDataSource.saveTrip(tripDto)

        // When
        val result = repository.getTrips()

        // Then
        assertTrue(result.isSuccess)
        val trips = result.getOrThrow()
        assertEquals(1, trips.size)
        assertTrue(trips.first() is Trip)
        assertEquals("trip-1", trips.first().id)
    }

    @Test
    fun `saveTrip should correctly map and save a trip`() = runTest {
        // Given
        val domainTrip = Trip(id = "trip-2", countryCode = "BR", countryName = "Brazil", startDate = "2026-08-01", endDate = "2026-08-10")

        // When
        val saveResult = repository.saveTrip(domainTrip)
        val getResult = repository.getTrips()

        // Then
        assertTrue(saveResult.isSuccess)
        val trips = getResult.getOrThrow()
        assertEquals(1, trips.size)
        assertEquals("trip-2", trips.first().id)
    }

    @Test
    fun `deleteTrip should correctly call the data source`() = runTest {
        // Given
        val tripDto = LocalTripDTO("trip-1", "AR", "Argentina", "2026-01-01", "2026-01-10", "Notes", 1L)
        fakeDataSource.saveTrip(tripDto)

        // When
        val deleteResult = repository.deleteTrip("trip-1")
        val getResult = repository.getTrips()

        // Then
        assertTrue(deleteResult.isSuccess)
        assertTrue(getResult.getOrThrow().isEmpty())
    }
}
