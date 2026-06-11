package edu.dyds.trips.domain.usecase

import edu.dyds.trips.domain.entity.Result
import edu.dyds.trips.domain.entity.Trip
import edu.dyds.trips.domain.repository.TripsRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UpdateTripUseCaseTest {
    private lateinit var fakeRepository: FakeTripsRepository
    private lateinit var useCase: UpdateTripUseCase

    @Before
    fun setup() {
        fakeRepository = FakeTripsRepository()
        useCase = UpdateTripUseCaseImpl(fakeRepository)
    }

    @Test
    fun `invoke should return Success when trip updated`() = runTest {
        // Arrange
        val trip = createMockTrip("AR", "Argentina", "2026-07-01", "2026-07-15")
        fakeRepository.setResult(Result.Success(Unit))

        // Act
        val result = useCase(trip)

        // Assert
        assertTrue(result is Result.Success)
    }

    @Test
    fun `invoke should return Failure when start date after end date`() = runTest {
        // Arrange
        val trip = createMockTrip("AR", "Argentina", "2026-07-15", "2026-07-01")

        // Act
        val result = useCase(trip)

        // Assert
        assertTrue(result is Result.Failure)
        assertTrue((result as Result.Failure).exception.message?.contains("Start date") == true)
    }

    @Test
    fun `invoke should accept trip with same start and end date`() = runTest {
        // Arrange
        val trip = createMockTrip("AR", "Argentina", "2026-07-01", "2026-07-01")
        fakeRepository.setResult(Result.Success(Unit))

        // Act
        val result = useCase(trip)

        // Assert
        assertTrue(result is Result.Success)
    }

    @Test
    fun `invoke should return Failure on repository error`() = runTest {
        // Arrange
        val trip = createMockTrip("AR", "Argentina", "2026-07-01", "2026-07-15")
        val exception = Exception("Update failed")
        fakeRepository.setResult(Result.Failure(exception))

        // Act
        val result = useCase(trip)

        // Assert
        assertTrue(result is Result.Failure)
    }

    @Test
    fun `invoke should call repository with correct updated trip`() = runTest {
        // Arrange
        val trip = createMockTrip("BR", "Brazil", "2026-08-01", "2026-08-30")
        fakeRepository.setResult(Result.Success(Unit))

        // Act
        val result = useCase(trip)

        // Assert
        assertTrue(result is Result.Success)
        assertEquals(fakeRepository.lastUpdatedTrip?.countryCode, "BR")
    }
}

private class FakeTripsRepository : TripsRepository {
    private var result: Result<Unit> = Result.Success(Unit)
    var lastUpdatedTrip: Trip? = null

    fun setResult(newResult: Result<Unit>) {
        result = newResult
    }

    override suspend fun getTrips(): Result<List<Trip>> =
        Result.Success(emptyList())

    override suspend fun getTripById(id: String): Result<Trip?> =
        Result.Success(null)

    override suspend fun saveTrip(trip: Trip): Result<Unit> =
        Result.Success(Unit)

    override suspend fun updateTrip(trip: Trip): Result<Unit> {
        lastUpdatedTrip = trip
        return result
    }

    override suspend fun deleteTrip(id: String): Result<Unit> =
        Result.Success(Unit)
}

private fun createMockTrip(
    code: String,
    name: String,
    startDate: String,
    endDate: String
): Trip = Trip(
    countryCode = code,
    countryName = name,
    startDate = startDate,
    endDate = endDate
)

