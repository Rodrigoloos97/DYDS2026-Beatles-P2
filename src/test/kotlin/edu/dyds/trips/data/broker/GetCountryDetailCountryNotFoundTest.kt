package edu.dyds.trips.data.broker

import edu.dyds.trips.domain.entity.Result
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertSame

class GetCountryDetailCountryNotFoundTest {

    @Test
    fun `getCountryDetail returns Failure when country is not found`() = runTest {
        val expectedException = NoSuchElementException("Country not found")
        val broker = CountryWeatherBroker(
            countriesRepository = FakeCountriesRepository(
                countryResult = Result.Failure(expectedException)
            ),
            weatherRepository = FakeWeatherRepository()
        )

        val result = broker.getCountryDetail("XX")

        assertIs<Result.Failure>(result)
        assertSame(expectedException, (result as Result.Failure).exception)
    }
}

