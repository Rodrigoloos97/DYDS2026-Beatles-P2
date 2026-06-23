package edu.dyds.trips.data.repository

import edu.dyds.trips.data.local.LocalTripDTO
import edu.dyds.trips.data.local.TripsLocalDataSource
import edu.dyds.trips.domain.entity.Result
import edu.dyds.trips.domain.entity.Trip
import edu.dyds.trips.domain.entity.getOrThrow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeTripsLocalDataSource : TripsLocalDataSource {
    private val trips = mutableListOf<LocalTripDTO>()

    override suspend fun getTrips(): kotlin.Result<List<LocalTripDTO>> {
        return kotlin.Result.success(trips.toList())
    }

    override suspend fun saveTrip(trip: LocalTripDTO): kotlin.Result<Unit> {
        trips.add(trip)
        return kotlin.Result.success(Unit)
    }

    override suspend fun updateTrip(trip: LocalTripDTO): kotlin.Result<Unit> {
        val index = trips.indexOfFirst { it.id == trip.id }
        if (index != -1) {
            trips[index] = trip
        }
        return kotlin.Result.success(Unit)
    }

    override suspend fun deleteTrip(id: String): kotlin.Result<Unit> {
        trips.removeAll { it.id == id }
        return kotlin.Result.success(Unit)
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
        val tripDto = LocalTripDTO("trip-1", "AR", "Argentina", "2026-01-01", "2026-01-10", "Notes", 1L)
        fakeDataSource.saveTrip(tripDto)

        val result = repository.getTrips()

        assertTrue(result is Result.Success)
        val trips = result.getOrThrow()
        assertEquals(1, trips.size)
        assertEquals("trip-1", trips.first().id)
    }

    @Test
    fun `saveTrip should correctly map and save a trip`() = runTest {
        val domainTrip = Trip(id = "trip-2", countryCode = "BR", countryName = "Brazil", startDate = "2026-08-01", endDate = "2026-08-10")

        val saveResult = repository.saveTrip(domainTrip)
        val getResult = repository.getTrips()

        assertTrue(saveResult is Result.Success)
        val trips = getResult.getOrThrow()
        assertEquals(1, trips.size)
        assertEquals("trip-2", trips.first().id)
    }

    @Test
    fun `deleteTrip should correctly call the data source`() = runTest {
        val tripDto = LocalTripDTO("trip-1", "AR", "Argentina", "2026-01-01", "2026-01-10", "Notes", 1L)
        fakeDataSource.saveTrip(tripDto)

        val deleteResult = repository.deleteTrip("trip-1")
        val getResult = repository.getTrips()

        assertTrue(deleteResult is Result.Success)
        assertTrue(getResult.getOrThrow().isEmpty())
    }
}
