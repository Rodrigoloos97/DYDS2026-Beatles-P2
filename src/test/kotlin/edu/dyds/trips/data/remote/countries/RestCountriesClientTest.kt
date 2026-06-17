package edu.dyds.trips.data.remote.countries

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import java.io.File
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

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

        val cachePath = tempCacheFilePath()
        val client = RestCountriesClient(mockHttp(payload), apiKey = "test-key", countriesCacheFilePath = cachePath)

        val countries = client.getCountries()

        assertEquals(1, countries.size)
        assertEquals("AR", countries.first().cca2)
        assertEquals("Argentina", countries.first().name.common)
        File(cachePath).delete()
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

        val cachePath = tempCacheFilePath()
        val client = RestCountriesClient(mockHttp(payload), apiKey = "test-key", countriesCacheFilePath = cachePath)

        val countries = client.getCountries()

        assertEquals(1, countries.size)
        assertEquals("AR", countries.first().cca2)
        File(cachePath).delete()
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

        val cachePath = tempCacheFilePath()
        val client = RestCountriesClient(mockHttp(payload), apiKey = "test-key", countriesCacheFilePath = cachePath)

        val countries = client.getCountries()

        assertEquals(1, countries.size)
        assertEquals("AR", countries.first().cca2)
        assertEquals("Argentina", countries.first().name.common)
        File(cachePath).delete()
    }

    @Test
    fun `getCountries caches first remote response and avoids second API call`() = runTest {
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

        val requestCount = AtomicInteger(0)
        val cachePath = tempCacheFilePath()
        val client = RestCountriesClient(
            httpClient = mockHttpCounting(payload, requestCount),
            apiKey = "test-key",
            countriesCacheFilePath = cachePath,
            minValidCacheCountries = 1
        )

        val first = client.getCountries()
        val second = client.getCountries()

        assertEquals(1, first.size)
        assertEquals(1, second.size)
        assertEquals(1, requestCount.get())
        File(cachePath).delete()
    }

    @Test
    fun `searchCountries uses cached countries without extra API requests`() = runTest {
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

        val requestCount = AtomicInteger(0)
        val cachePath = tempCacheFilePath()
        val client = RestCountriesClient(
            httpClient = mockHttpCounting(payload, requestCount),
            apiKey = "test-key",
            countriesCacheFilePath = cachePath,
            minValidCacheCountries = 1
        )

        client.getCountries()
        val result = client.searchCountries("argen")

        assertEquals(1, result.size)
        assertTrue(result.first().name.common.contains("Argentina"))
        assertEquals(1, requestCount.get())
        File(cachePath).delete()
    }

    @Test
    fun `getCountries throws readable error for object payload`() = runTest {
        val errorPayload = """
            { "success": false, "message": "Bad request" }
        """.trimIndent()

        val cachePath = tempCacheFilePath()
        val client = RestCountriesClient(
            mockHttp(errorPayload, HttpStatusCode.BadRequest),
            apiKey = "test-key",
            countriesCacheFilePath = cachePath
        )

        val ex = assertFailsWith<IllegalStateException> {
            client.getCountries()
        }

        assertEquals("Bad request", ex.message)
        File(cachePath).delete()
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

        val cachePath = tempCacheFilePath()
        val client = RestCountriesClient(mockHttp(payload), apiKey = null, countriesCacheFilePath = cachePath)

        val ex = assertFailsWith<IllegalStateException> {
            client.getCountries()
        }

        assertEquals(
            "REST_COUNTRIES_API_KEY no configurada. Configura la variable de entorno para usar la API remota.",
            ex.message
        )
        File(cachePath).delete()
    }

    private fun tempCacheFilePath(): String {
        return "app_data/test-countries-cache-${UUID.randomUUID()}.json"
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

    private fun mockHttpCounting(
        payload: String,
        counter: AtomicInteger,
        status: HttpStatusCode = HttpStatusCode.OK
    ): HttpClient {
        val engine = MockEngine { _ ->
            counter.incrementAndGet()
            respond(
                content = payload,
                status = status,
                headers = headersOf("Content-Type", ContentType.Application.Json.toString())
            )
        }
        return HttpClient(engine)
    }
}

