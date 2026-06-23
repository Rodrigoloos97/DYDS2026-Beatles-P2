package edu.dyds.trips.config

import kotlin.test.Test
import kotlin.test.assertEquals

class AppConfigFromEnvironmentTest {

    @Test
    fun `fromEnvironment returns default values when environment variables are not set`() {
        val config = AppConfigImpl.fromEnvironment()

        val expectedRestUrl = System.getenv("REST_COUNTRIES_BASE_URL") ?: "https://api.restcountries.com/countries/v5"
        val expectedMeteoUrl = System.getenv("OPEN_METEO_BASE_URL") ?: "https://api.open-meteo.com/v1/forecast"
        val expectedCountriesPath = System.getenv("COUNTRIES_CACHE_PATH") ?: "app_data/countries_cache.json"
        val expectedTripsPath = System.getenv("TRIPS_CACHE_PATH") ?: "app_data/trips_data.json"

        assertEquals(expectedRestUrl, config.restCountriesBaseUrl)
        assertEquals(expectedMeteoUrl, config.openMeteoBaseUrl)
        assertEquals(expectedCountriesPath, config.countriesCacheFilePath)
        assertEquals(expectedTripsPath, config.tripsCacheFilePath)
    }
}


