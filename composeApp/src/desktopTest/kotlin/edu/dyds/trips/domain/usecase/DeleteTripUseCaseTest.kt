package edu.dyds.trips.domain.usecase

import edu.dyds.trips.domain.entity.Result
import edu.dyds.trips.domain.entity.Trip
import edu.dyds.trips.domain.repository.TripsRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeleteTripUseCaseTest {
    private lateinit var fakeRepository: FakeTripsRepository
    private lateinit var useCase: DeleteTripUseCase

    @Before
    fun setup() {
        fakeRepository = FakeTripsRepository()
        useCase = DeleteTripUseCaseImpl(fakeRepository)
    }

    @Test
    fun `invoke should return Success when trip deleted`() = runTest {
        // Arrange
        val tripId = "trip-123"
        fakeRepository.setResult(Result.Success(Unit))

        // Act
        val result = useCase(tripId)

        // Assert
        assertTrue(result is Result.Success)
    }

    @Test
    fun `invoke should return Failure when trip not found`() = runTest {
        // Arrange
        val tripId = "nonexistent-id"
        val exception = Exception("Trip not found")
        fakeRepository.setResult(Result.Failure(exception))

        // Act
        val result = useCase(tripId)

        // Assert
        assertTrue(result is Result.Failure)
    }

    @Test
    fun `invoke should return Failure on repository error`() = runTest {
        // Arrange
        val tripId = "trip-123"
        val exception = Exception("Database error")
        fakeRepository.setResult(Result.Failure(exception))

        // Act
        val result = useCase(tripId)

        // Assert
        assertTrue(result is Result.Failure)
    }

    @Test
    fun `invoke should call repository with correct trip id`() = runTest {
        // Arrange
        val tripId = "trip-456"
        fakeRepository.setResult(Result.Success(Unit))

        // Act
        val result = useCase(tripId)

        // Assert
        assertTrue(result is Result.Success)
        assertEquals(fakeRepository.lastDeletedTripId, tripId)
    }
}

private class FakeTripsRepository : TripsRepository {
    private var result: Result<Unit> = Result.Success(Unit)
    var lastDeletedTripId: String? = null

    fun setResult(newResult: Result<Unit>) {
        result = newResult
    }

    override suspend fun getTrips(): Result<List<Trip>> =
        Result.Success(emptyList())

    override suspend fun getTripById(id: String): Result<Trip?> =
        Result.Success(null)

    override suspend fun saveTrip(trip: Trip): Result<Unit> =
        Result.Success(Unit)

    override suspend fun updateTrip(trip: Trip): Result<Unit> =
        Result.Success(Unit)

    override suspend fun deleteTrip(id: String): Result<Unit> {
        lastDeletedTripId = id
        return result
    }
}

