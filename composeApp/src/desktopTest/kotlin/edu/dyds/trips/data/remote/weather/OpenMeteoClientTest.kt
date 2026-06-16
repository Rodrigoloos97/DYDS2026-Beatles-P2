package edu.dyds.trips.data.remote.weather

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
import kotlin.test.Test
import kotlin.test.assertEquals

class OpenMeteoClientTest {
    @Test
    fun `getForecast parses weather payload`() = runTest {
        val payload = """
            {
              "daily": {
                "time": ["2026-06-16"],
                "temperature_2m_max": [20.0],
                "temperature_2m_min": [11.0],
                "precipitation_sum": [0.5],
                "windspeed_10m_max": [15.0],
                "weather_code": [1]
              }
            }
        """.trimIndent()

        val client = OpenMeteoClient(mockHttp(payload))

        val forecast = client.getForecast(-34.6, -58.4, "UTC")

        assertEquals(1, forecast.daily.time.size)
        assertEquals(20.0, forecast.daily.temperature2mMax.first())
        assertEquals(1, forecast.daily.weatherCode.first())
    }

    private fun mockHttp(payload: String): HttpClient {
        val engine = MockEngine { _ ->
            respond(
                content = payload,
                status = HttpStatusCode.OK,
                headers = headersOf("Content-Type", ContentType.Application.Json.toString())
            )
        }
        return HttpClient(engine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }
}

