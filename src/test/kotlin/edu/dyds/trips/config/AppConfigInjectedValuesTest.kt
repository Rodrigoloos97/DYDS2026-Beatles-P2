package edu.dyds.trips.config

import kotlin.test.Test
import kotlin.test.assertEquals

class AppConfigInjectedValuesTest {

    @Test
    fun `AppConfigImpl with custom values returns exactly those values`() {
        val config = AppConfigImpl(
            restCountriesBaseUrl = "https://mock.restcountries.test/api",
            openMeteoBaseUrl = "https://mock.open-meteo.test/v1/forecast",
            countriesCacheFilePath = "tmp/test-countries.json",
            tripsCacheFilePath = "tmp/test-trips.json"
        )

        // Verifica que los valores inyectados son devueltos sin transformación (DIP)
        assertEquals("https://mock.restcountries.test/api", config.restCountriesBaseUrl)
        assertEquals("https://mock.open-meteo.test/v1/forecast", config.openMeteoBaseUrl)
        assertEquals("tmp/test-countries.json", config.countriesCacheFilePath)
        assertEquals("tmp/test-trips.json", config.tripsCacheFilePath)
    }
}

