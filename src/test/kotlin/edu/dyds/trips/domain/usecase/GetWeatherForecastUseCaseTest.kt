package edu.dyds.trips.domain.usecase

import edu.dyds.trips.domain.entity.Result
import edu.dyds.trips.domain.entity.WeatherForecast
import edu.dyds.trips.domain.repository.WeatherRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetWeatherForecastUseCaseTest {
    @Test
    fun `returns forecast from weather repository`() = runTest {
        val useCase = GetWeatherForecastUseCaseImpl(FakeWeatherRepository())

        val result = useCase(-34.6, -58.4)

        assertTrue(result is Result.Success)
        assertEquals(1, result.value.size)
    }
}


