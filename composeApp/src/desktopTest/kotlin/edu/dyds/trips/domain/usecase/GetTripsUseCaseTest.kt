package edu.dyds.trips.domain.usecase

import edu.dyds.trips.domain.entity.Result
import edu.dyds.trips.domain.entity.Trip
import edu.dyds.trips.domain.repository.TripsRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetTripsUseCaseTest {
    private lateinit var fakeRepository: FakeTripsRepository
    private lateinit var useCase: GetTripsUseCase

    @Before
    fun setup() {
        fakeRepository = FakeTripsRepository()
        useCase = GetTripsUseCaseImpl(fakeRepository)
    }

    @Test
    fun `invoke should return Success with trips list`() = runTest {
        // Arrange
        val trips = listOf(
            createMockTrip("AR", "Argentina"),
            createMockTrip("BR", "Brazil")
        )
        fakeRepository.setResult(Result.Success(trips))

        // Act
        val result = useCase()

        // Assert
        assertTrue(result is Result.Success)
        assertEquals((result as Result.Success).value.size, 2)
        assertEquals((result as Result.Success).value[0].countryName, "Argentina")
    }

    @Test
    fun `invoke should return Success with empty list when no trips`() = runTest {
        // Arrange
        fakeRepository.setResult(Result.Success(emptyList()))

        // Act
        val result = useCase()

        // Assert
        assertTrue(result is Result.Success)
        assertEquals((result as Result.Success).value.size, 0)
    }

    @Test
    fun `invoke should return Failure on error`() = runTest {
        // Arrange
        val exception = Exception("Database error")
        fakeRepository.setResult(Result.Failure(exception))

        // Act
        val result = useCase()

        // Assert
        assertTrue(result is Result.Failure)
    }
}

private class FakeTripsRepository : TripsRepository {
    private var result: Result<List<Trip>> = Result.Success(emptyList())

    fun setResult(newResult: Result<List<Trip>>) {
        result = newResult
    }

    override suspend fun getTrips(): Result<List<Trip>> = result

    override suspend fun getTripById(id: String): Result<Trip?> =
        Result.Success(null)

    override suspend fun saveTrip(trip: Trip): Result<Unit> =
        Result.Success(Unit)

    override suspend fun updateTrip(trip: Trip): Result<Unit> =
        Result.Success(Unit)

    override suspend fun deleteTrip(id: String): Result<Unit> =
        Result.Success(Unit)
}

private fun createMockTrip(code: String, name: String): Trip = Trip(
    countryCode = code,
    countryName = name,
    startDate = "2026-07-01",
    endDate = "2026-07-15"
)

