package edu.dyds.trips.data.broker

import edu.dyds.trips.domain.entity.Result
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class GetCountryDetailSuccessTest {

    @Test
    fun `getCountryDetail returns CountryDetail with forecast when both repositories succeed`() = runTest {
        val broker = CountryWeatherBroker(
            countriesRepository = FakeCountriesRepository(
                countryResult = Result.Success(sampleCountry())
            ),
            weatherRepository = FakeWeatherRepository(
                weatherResult = Result.Success(listOf(sampleWeatherForecast()))
            )
        )

        val result = broker.getCountryDetail("AR")

        assertIs<Result.Success<*>>(result)
        val detail = (result as Result.Success).value
        assertEquals("Argentina", detail.country.name)
        assertTrue(detail.weatherForecast.isNotEmpty())
        assertEquals("2026-06-20", detail.weatherForecast.first().date)
    }
}

