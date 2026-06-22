package edu.dyds.trips.domain.usecase

import edu.dyds.trips.domain.entity.Result
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetCountryDetailsUseCaseTest {
    @Test
    fun `returns country detail with forecast`() = runTest {
        val useCase = GetCountryDetailsUseCaseImpl(
            countriesRepository = FakeCountriesRepository(),
            weatherRepository = FakeWeatherRepository()
        )

        val result = useCase("AR")

        assertTrue(result is Result.Success)
        assertEquals("Argentina", result.value.country.name)
        assertTrue(result.value.weatherForecast.isNotEmpty())
    }
}


