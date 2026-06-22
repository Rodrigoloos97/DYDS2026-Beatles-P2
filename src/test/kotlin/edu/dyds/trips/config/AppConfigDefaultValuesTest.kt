package edu.dyds.trips.config

import kotlin.test.Test
import kotlin.test.assertEquals

class AppConfigDefaultValuesTest {

    @Test
    fun `AppConfigImpl with no arguments uses default URLs and paths`() {
        val config = AppConfigImpl()

        assertEquals("https://api.restcountries.com/countries/v5", config.restCountriesBaseUrl)
        assertEquals("https://api.open-meteo.com/v1/forecast", config.openMeteoBaseUrl)
        assertEquals("app_data/countries_cache.json", config.countriesCacheFilePath)
        assertEquals("app_data/trips_data.json", config.tripsCacheFilePath)
    }
}

