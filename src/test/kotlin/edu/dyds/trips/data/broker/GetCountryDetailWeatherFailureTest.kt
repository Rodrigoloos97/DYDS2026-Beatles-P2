package edu.dyds.trips.data.broker

import edu.dyds.trips.domain.entity.Result
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class GetCountryDetailWeatherFailureTest {

    @Test
    fun `getCountryDetail returns CountryDetail with empty forecast when weather repository fails`() = runTest {
        val broker = CountryWeatherBroker(
            countriesRepository = FakeCountriesRepository(
                countryResult = Result.Success(sampleCountry())
            ),
            weatherRepository = FakeWeatherRepository(
                weatherResult = Result.Failure(RuntimeException("Weather service unavailable"))
            )
        )

        val result = broker.getCountryDetail("AR")

        // El fallo del clima NO propaga el error: degrada graciosamente
        assertIs<Result.Success<*>>(result)
        val detail = (result as Result.Success).value
        assertEquals("Argentina", detail.country.name)
        assertTrue(detail.weatherForecast.isEmpty())
    }
}

