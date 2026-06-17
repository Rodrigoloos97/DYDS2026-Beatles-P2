package edu.dyds.trips.data.remote.countries

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RestCountriesClientTest {
    @Test
    fun `getCountries parses list payload`() = runTest {
        val payload = """
            [
              {
                "cca2": "AR",
                "name": { "common": "Argentina", "official": "Argentine Republic" },
                "region": "Americas",
                "subregion": "South America",
                "capital": ["Buenos Aires"],
                "currencies": { "ARS": { "name": "Peso", "symbol": "$" } },
                "languages": { "es": "Spanish" },
                "timezones": ["UTC-03:00"],
                "latlng": [-34.0, -64.0],
                "flags": { "png": "https://flagcdn.com/ar.png", "svg": "https://flagcdn.com/ar.svg" },
                "population": 46000000
              }
            ]
        """.trimIndent()

        val client = RestCountriesClient(mockHttp(payload), apiKey = "test-key")

        val countries = client.getCountries()

        assertEquals(1, countries.size)
        assertEquals("AR", countries.first().cca2)
        assertEquals("Argentina", countries.first().name.common)
    }

    @Test
    fun `getCountries parses object payload with data array`() = runTest {
        val payload = """
            {
              "success": true,
              "data": [
                {
                  "cca2": "AR",
                  "name": { "common": "Argentina", "official": "Argentine Republic" },
                  "region": "Americas",
                  "subregion": "South America",
                  "capital": ["Buenos Aires"],
                  "currencies": { "ARS": { "name": "Peso", "symbol": "$" } },
                  "languages": { "es": "Spanish" },
                  "timezones": ["UTC-03:00"],
                  "latlng": [-34.0, -64.0],
                  "flags": { "png": "https://flagcdn.com/ar.png", "svg": "https://flagcdn.com/ar.svg" },
                  "population": 46000000
                }
              ]
            }
        """.trimIndent()

        val client = RestCountriesClient(mockHttp(payload), apiKey = "test-key")

        val countries = client.getCountries()

        assertEquals(1, countries.size)
        assertEquals("AR", countries.first().cca2)
    }

    @Test
    fun `getCountries parses v5 payload with data objects`() = runTest {
        val payload = """
            {
              "data": {
                "objects": [
                  {
                    "codes": { "alpha_2": "AR" },
                    "names": { "common": "Argentina", "official": "Argentine Republic" },
                    "region": "Americas",
                    "subregion": "South America",
                    "capitals": [ { "name": "Buenos Aires" } ],
                    "currencies": { "code": "ARS", "name": "Peso", "symbol": "$" },
                    "languages": [ { "iso639_3": "spa", "name": "Spanish" } ],
                    "timezones": ["UTC-03:00"],
                    "coordinates": { "lat": -34.0, "lng": -64.0 },
                    "flag": { "url_png": "https://flagcdn.com/ar.png", "url_svg": "https://flagcdn.com/ar.svg" },
                    "population": 46000000
                  }
                ]
              }
            }
        """.trimIndent()

        val client = RestCountriesClient(mockHttp(payload), apiKey = "test-key")

        val countries = client.getCountries()

        assertEquals(1, countries.size)
        assertEquals("AR", countries.first().cca2)
        assertEquals("Argentina", countries.first().name.common)
    }

    @Test
    fun `getCountries throws readable error for object payload`() = runTest {
        val errorPayload = """
            { "success": false, "message": "Bad request" }
        """.trimIndent()

        val client = RestCountriesClient(
            mockHttp(errorPayload, HttpStatusCode.BadRequest),
            apiKey = "test-key"
        )

        val ex = assertFailsWith<IllegalStateException> {
            client.getCountries()
        }

        assertEquals("Bad request", ex.message)
    }

    @Test
    fun `getCountries fails when api key is missing`() = runTest {
        val payload = """
            [
              {
                "cca2": "AR",
                "name": { "common": "Argentina", "official": "Argentine Republic" },
                "region": "Americas",
                "subregion": "South America",
                "capital": ["Buenos Aires"],
                "currencies": { "ARS": { "name": "Peso", "symbol": "$" } },
                "languages": { "es": "Spanish" },
                "timezones": ["UTC-03:00"],
                "latlng": [-34.0, -64.0],
                "flags": { "png": "https://flagcdn.com/ar.png", "svg": "https://flagcdn.com/ar.svg" },
                "population": 46000000
              }
            ]
        """.trimIndent()

        val client = RestCountriesClient(mockHttp(payload), apiKey = null)

        val ex = assertFailsWith<IllegalStateException> {
            client.getCountries()
        }

        assertEquals(
            "REST_COUNTRIES_API_KEY no configurada. Configura la variable de entorno para usar la API remota.",
            ex.message
        )
    }

    private fun mockHttp(payload: String, status: HttpStatusCode = HttpStatusCode.OK): HttpClient {
        val engine = MockEngine { _ ->
            respond(
                content = payload,
                status = status,
                headers = headersOf("Content-Type", ContentType.Application.Json.toString())
            )
        }
        return HttpClient(engine)
    }
}

