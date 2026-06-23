package edu.dyds.trips.data.broker

import edu.dyds.trips.domain.entity.Result
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetCountryDetailCodeDelegationTest {

    @Test
    fun `getCountryDetail passes received country code to countries repository`() = runTest {
        val fakeCountries = FakeCountriesRepository(
            countryResult = Result.Success(sampleCountry())
        )
        val broker = CountryWeatherBroker(
            countriesRepository = fakeCountries,
            weatherRepository = FakeWeatherRepository()
        )

        broker.getCountryDetail("AR")

        assertEquals("AR", fakeCountries.capturedCode)
    }
}


