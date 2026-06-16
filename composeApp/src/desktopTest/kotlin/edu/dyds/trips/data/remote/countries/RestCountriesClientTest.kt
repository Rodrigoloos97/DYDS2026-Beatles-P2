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

        val client = RestCountriesClient(mockHttp(payload))

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

        val client = RestCountriesClient(mockHttp(errorPayload, HttpStatusCode.BadRequest))

        val ex = assertFailsWith<IllegalStateException> {
            client.getCountries()
        }

        assertEquals("Bad request", ex.message)
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

