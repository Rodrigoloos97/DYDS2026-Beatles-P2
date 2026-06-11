package edu.dyds.trips.domain.usecase

import edu.dyds.trips.domain.entity.Result
import edu.dyds.trips.domain.entity.WeatherForecast
import edu.dyds.trips.domain.repository.WeatherRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetWeatherForecastUseCaseTest {
    private lateinit var fakeRepository: FakeWeatherRepository
    private lateinit var useCase: GetWeatherForecastUseCase

    @Before
    fun setup() {
        fakeRepository = FakeWeatherRepository()
        useCase = GetWeatherForecastUseCaseImpl(fakeRepository)
    }

    @Test
    fun `invoke should return Success with weather forecast list`() = runTest {
        // Arrange
        val forecast = listOf(
            createMockForecast("2026-06-11"),
            createMockForecast("2026-06-12")
        )
        fakeRepository.setResult(Result.Success(forecast))

        // Act
        val result = useCase(latitude = -34.6037, longitude = -58.3816)

        // Assert
        assertTrue(result is Result.Success)
        assertEquals((result as Result.Success).value.size, 2)
        assertEquals((result as Result.Success).value[0].date, "2026-06-11")
    }

    @Test
    fun `invoke should return Success with empty list when no forecast available`() = runTest {
        // Arrange
        fakeRepository.setResult(Result.Success(emptyList()))

        // Act
        val result = useCase(latitude = 0.0, longitude = 0.0)

        // Assert
        assertTrue(result is Result.Success)
        assertEquals((result as Result.Success).value.size, 0)
    }

    @Test
    fun `invoke should return Failure on network error`() = runTest {
        // Arrange
        val exception = Exception("Network error")
        fakeRepository.setResult(Result.Failure(exception))

        // Act
        val result = useCase(latitude = -34.6037, longitude = -58.3816)

        // Assert
        assertTrue(result is Result.Failure)
    }

    @Test
    fun `invoke should pass correct coordinates to repository`() = runTest {
        // Arrange
        val latitude = 40.7128
        val longitude = -74.0060
        fakeRepository.setResult(Result.Success(emptyList()))

        // Act
        val result = useCase(latitude = latitude, longitude = longitude)

        // Assert
        assertEquals(fakeRepository.lastLatitude, latitude)
        assertEquals(fakeRepository.lastLongitude, longitude)
    }
}

private class FakeWeatherRepository : WeatherRepository {
    private var result: Result<List<WeatherForecast>> = Result.Success(emptyList())
    var lastLatitude: Double = 0.0
    var lastLongitude: Double = 0.0

    fun setResult(newResult: Result<List<WeatherForecast>>) {
        result = newResult
    }

    override suspend fun getWeatherForecast(
        latitude: Double,
        longitude: Double
    ): Result<List<WeatherForecast>> {
        lastLatitude = latitude
        lastLongitude = longitude
        return result
    }
}

private fun createMockForecast(date: String): WeatherForecast = WeatherForecast(
    date = date,
    tempMinCelsius = 15.0,
    tempMaxCelsius = 25.0,
    precipitationMm = 0.0,
    windSpeedKmh = 10.0,
    weatherCode = 0,
    description = "Sunny"
)

