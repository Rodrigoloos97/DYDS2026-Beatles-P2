package edu.dyds.trips.config

/**
 * Contrato centralizado para toda la configuración de la aplicación.
 *
 * Define las propiedades de configuración requeridas por la app sin
 * especificar cómo se obtienen. Respeta ISP: solo expone lo necesario.
 *
 * Principios aplicados:
 * - SRP: esta interfaz solo define el contrato de configuración
 * - ISP: contiene solo las propiedades que los clientes necesitan
 * - DIP: otros componentes dependen de esta interfaz, no de implementaciones concretas
 */
interface AppConfig {
    /**
     * URL base de la API de restcountries.
     * Se puede configurar vía variable de entorno `REST_COUNTRIES_BASE_URL`.
     * Default: https://api.restcountries.com/countries/v5
     */
    val restCountriesBaseUrl: String

    /**
     * URL base de la API de OpenMeteo (pronóstico del clima).
     * Se puede configurar vía variable de entorno `OPEN_METEO_BASE_URL`.
     * Default: https://api.open-meteo.com/v1/forecast
     */
    val openMeteoBaseUrl: String

    /**
     * Ruta del archivo de caché local para datos de países.
     * Se puede configurar vía variable de entorno `COUNTRIES_CACHE_PATH`.
     * Default: app_data/countries_cache.json
     */
    val countriesCacheFilePath: String

    /**
     * Ruta del archivo de caché local para datos de viajes del usuario.
     * Se puede configurar vía variable de entorno `TRIPS_CACHE_PATH`.
     * Default: app_data/trips_data.json
     */
    val tripsCacheFilePath: String
}

/**
 * Implementación de AppConfig que lee desde variables de entorno con defaults.
 *
 * Permite inyección de valores para testing sin modificar variables de entorno.
 *
 * Principios aplicados:
 * - OCP: interfaz cerrada a cambios; implementación abierta a extensión
 * - DIP: se inyecta en componentes que dependen de AppConfig
 */
data class AppConfigImpl(
    override val restCountriesBaseUrl: String = DEFAULT_REST_COUNTRIES_BASE_URL,
    override val openMeteoBaseUrl: String = DEFAULT_OPEN_METEO_BASE_URL,
    override val countriesCacheFilePath: String = DEFAULT_COUNTRIES_CACHE_PATH,
    override val tripsCacheFilePath: String = DEFAULT_TRIPS_CACHE_PATH
) : AppConfig {
    companion object {
        // Constantes de default (valores públicos conocidos de las APIs)
        private const val DEFAULT_REST_COUNTRIES_BASE_URL = "https://api.restcountries.com/countries/v5"
        private const val DEFAULT_OPEN_METEO_BASE_URL = "https://api.open-meteo.com/v1/forecast"
        private const val DEFAULT_COUNTRIES_CACHE_PATH = "app_data/countries_cache.json"
        private const val DEFAULT_TRIPS_CACHE_PATH = "app_data/trips_data.json"

        /**
         * Factory que crea AppConfigImpl leyendo desde variables de entorno con defaults.
         * Se utiliza para inicialización en producción.
         *
         * Variables de entorno soportadas:
         * - REST_COUNTRIES_BASE_URL
         * - OPEN_METEO_BASE_URL
         * - COUNTRIES_CACHE_PATH
         * - TRIPS_CACHE_PATH
         */
        fun fromEnvironment(): AppConfig = AppConfigImpl(
            restCountriesBaseUrl = System.getenv("REST_COUNTRIES_BASE_URL") ?: DEFAULT_REST_COUNTRIES_BASE_URL,
            openMeteoBaseUrl = System.getenv("OPEN_METEO_BASE_URL") ?: DEFAULT_OPEN_METEO_BASE_URL,
            countriesCacheFilePath = System.getenv("COUNTRIES_CACHE_PATH") ?: DEFAULT_COUNTRIES_CACHE_PATH,
            tripsCacheFilePath = System.getenv("TRIPS_CACHE_PATH") ?: DEFAULT_TRIPS_CACHE_PATH
        )
    }
}


