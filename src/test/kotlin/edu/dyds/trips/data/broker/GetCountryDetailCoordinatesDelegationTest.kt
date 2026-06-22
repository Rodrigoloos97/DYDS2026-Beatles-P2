package edu.dyds.trips.data.broker

import edu.dyds.trips.domain.entity.Result
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class GetCountryDetailCoordinatesDelegationTest {

    @Test
    fun `getCountryDetail passes country coordinates to weather repository`() = runTest {
        val country = sampleCountry() // latitude = -34.6, longitude = -58.4
        val fakeWeather = FakeWeatherRepository()
        val broker = CountryWeatherBroker(
            countriesRepository = FakeCountriesRepository(
                countryResult = Result.Success(country)
            ),
            weatherRepository = fakeWeather
        )

        broker.getCountryDetail("AR")

        // Verifica que las coordenadas del país fueron propagadas correctamente
        assertNotNull(fakeWeather.capturedLatitude)
        assertNotNull(fakeWeather.capturedLongitude)
        assertEquals(country.latitude, fakeWeather.capturedLatitude)
        assertEquals(country.longitude, fakeWeather.capturedLongitude)
    }
}

