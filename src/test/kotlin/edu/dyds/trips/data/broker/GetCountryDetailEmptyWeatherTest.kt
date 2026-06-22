package edu.dyds.trips.data.broker

import edu.dyds.trips.domain.entity.Result
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class GetCountryDetailEmptyWeatherTest {

    @Test
    fun `getCountryDetail returns CountryDetail with empty forecast when weather repository returns empty list`() = runTest {
        val broker = CountryWeatherBroker(
            countriesRepository = FakeCountriesRepository(
                countryResult = Result.Success(sampleCountry())
            ),
            weatherRepository = FakeWeatherRepository(
                weatherResult = Result.Success(emptyList())
            )
        )

        val result = broker.getCountryDetail("AR")

        assertIs<Result.Success<*>>(result)
        val detail = (result as Result.Success).value
        assertEquals("Argentina", detail.country.name)
        assertTrue(detail.weatherForecast.isEmpty())
    }
}

