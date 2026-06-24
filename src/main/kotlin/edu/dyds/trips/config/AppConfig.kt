package edu.dyds.trips.config

interface AppConfig {
    val restCountriesBaseUrl: String

    val openMeteoBaseUrl: String

    val countriesCacheFilePath: String

    val tripsCacheFilePath: String
}

data class AppConfigImpl(
    override val restCountriesBaseUrl: String = DEFAULT_REST_COUNTRIES_BASE_URL,
    override val openMeteoBaseUrl: String = DEFAULT_OPEN_METEO_BASE_URL,
    override val countriesCacheFilePath: String = DEFAULT_COUNTRIES_CACHE_PATH,
    override val tripsCacheFilePath: String = DEFAULT_TRIPS_CACHE_PATH
) : AppConfig {
    companion object {
        private const val DEFAULT_REST_COUNTRIES_BASE_URL = "https://api.restcountries.com/countries/v5"
        private const val DEFAULT_OPEN_METEO_BASE_URL = "https://api.open-meteo.com/v1/forecast"
        private const val DEFAULT_COUNTRIES_CACHE_PATH = "app_data/countries_cache.json"
        private const val DEFAULT_TRIPS_CACHE_PATH = "app_data/trips_data.json"

        fun fromEnvironment(): AppConfig = AppConfigImpl(
            restCountriesBaseUrl = System.getenv("REST_COUNTRIES_BASE_URL") ?: DEFAULT_REST_COUNTRIES_BASE_URL,
            openMeteoBaseUrl = System.getenv("OPEN_METEO_BASE_URL") ?: DEFAULT_OPEN_METEO_BASE_URL,
            countriesCacheFilePath = System.getenv("COUNTRIES_CACHE_PATH") ?: DEFAULT_COUNTRIES_CACHE_PATH,
            tripsCacheFilePath = System.getenv("TRIPS_CACHE_PATH") ?: DEFAULT_TRIPS_CACHE_PATH
        )
    }
}


