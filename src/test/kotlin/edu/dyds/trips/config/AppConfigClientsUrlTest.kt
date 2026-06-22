package edu.dyds.trips.config

import edu.dyds.trips.data.remote.countries.RestCountriesClient
import edu.dyds.trips.data.remote.weather.OpenMeteoClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

class AppConfigClientsUrlTest {

    @Test
    fun `RestCountriesClient uses URL from AppConfig`() = runTest {
        val customUrl = "https://mock.restcountries.test/api"
        val config = AppConfigImpl(
            restCountriesBaseUrl = customUrl,
            countriesCacheFilePath = tempCacheFilePath()
        )
        var capturedUrl = ""
        val mockEngine = MockEngine { request ->
            capturedUrl = request.url.toString().substringBefore("?")
            respond(
                content = "[]",
                status = HttpStatusCode.OK,
                headers = headersOf("Content-Type", ContentType.Application.Json.toString())
            )
        }
        val client = RestCountriesClient(
            httpClient = HttpClient(mockEngine),
            appConfig = config,
            apiKey = "test-key"
        )

        client.getCountries()

        // Verifica que la URL capturada corresponde a la configurada en AppConfig
        assertEquals(customUrl, capturedUrl)
        File(config.countriesCacheFilePath).delete()
    }

    @Test
    fun `OpenMeteoClient uses URL from AppConfig`() = runTest {
        val customUrl = "https://mock.open-meteo.test/v1/forecast"
        val config = AppConfigImpl(openMeteoBaseUrl = customUrl)
        var capturedUrl = ""
        val mockEngine = MockEngine { request ->
            capturedUrl = request.url.toString().substringBefore("?")
            respond(
                content = """{"daily":{"time":[],"temperature_2m_max":[],"temperature_2m_min":[],"precipitation_sum":[],"windspeed_10m_max":[],"weather_code":[]}}""",
                status = HttpStatusCode.OK,
                headers = headersOf("Content-Type", ContentType.Application.Json.toString())
            )
        }
        val client = OpenMeteoClient(
            httpClient = HttpClient(mockEngine) {
                install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            },
            appConfig = config
        )

        client.getForecast(-34.6, -58.4, "UTC")

        // Verifica que la URL capturada corresponde a la configurada en AppConfig
        assertEquals(customUrl, capturedUrl)
    }

    private fun tempCacheFilePath(): String =
        "app_data/test-appconfig-cache-${UUID.randomUUID()}.json"
}

