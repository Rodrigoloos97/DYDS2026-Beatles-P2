package edu.dyds.trips.data.remote.countries

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import java.io.File

class RestCountriesClient(
    private val httpClient: HttpClient,
    private val json: Json = Json { ignoreUnknownKeys = true }
) {
    suspend fun getCountries(): List<RemoteCountryDTO> = try {
        parseCountriesPayload(
            httpClient.get("https://restcountries.com/v3.1/all") {
                parameter(
                    "fields",
                    "cca2,name,region,subregion,capital,currencies,languages,timezones,latlng,flags,population"
                )
            }.bodyAsText()
        )
    } catch (e: IllegalStateException) {
        // Error de parseo/validación de API (malo request del servidor) → relanzar
        throw e
    } catch (e: Exception) {
        // Error de conexión/red → cargar desde datos locales
        getLocalCountries()
    }

    suspend fun searchCountries(name: String): List<RemoteCountryDTO> = try {
        parseCountriesPayload(
            httpClient.get("https://restcountries.com/v3.1/name/$name") {
                parameter(
                    "fields",
                    "cca2,name,region,subregion,capital,currencies,languages,timezones,latlng,flags,population"
                )
            }.bodyAsText()
        )
    } catch (e: IllegalStateException) {
        // Error de parseo/validación de API → relanzar
        throw e
    } catch (e: Exception) {
        // Error de conexión → buscar en datos locales
        getLocalCountries().filter {
            it.name.common.contains(name, ignoreCase = true) ||
            it.cca2.contains(name, ignoreCase = true)
        }
    }

    private fun getLocalCountries(): List<RemoteCountryDTO> {
        // Intentar leer del archivo primero
        val fromFile = loadLocalCountriesFromFile()
        if (fromFile.isNotEmpty()) {
            return fromFile
        }
        // Fallback: datos embebidos directamente en código
        return getEmbeddedCountries()
    }

    private fun loadLocalCountriesFromFile(): List<RemoteCountryDTO> {
        return try {
            // Intentar cargar desde múltiples rutas posibles
            val possiblePaths = listOf(
                "app_data/countries_local.json",
                "composeApp/app_data/countries_local.json",
                "${System.getProperty("user.dir")}/app_data/countries_local.json",
                "${System.getProperty("user.dir")}/composeApp/app_data/countries_local.json"
            )

            val file = possiblePaths.map { File(it) }.firstOrNull { it.exists() }

            if (file != null) {
                val jsonContent = file.readText()
                json.decodeFromJsonElement(json.parseToJsonElement(jsonContent))
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun getEmbeddedCountries(): List<RemoteCountryDTO> {
        return listOf(
            RemoteCountryDTO(
                cca2 = "AR",
                name = RemoteCountryNameDTO("Argentina", "Argentine Republic"),
                region = "Americas",
                subregion = "South America",
                capital = listOf("Buenos Aires"),
                currencies = mapOf("ARS" to RemoteCurrencyDTO("Argentine peso", "$")),
                languages = mapOf("spa" to "Spanish"),
                timezones = listOf("UTC-03:00"),
                latlng = listOf(-34.0, -64.0),
                flags = RemoteFlagsDTO("https://flagcdn.com/w320/ar.png", "https://flagcdn.com/ar.svg"),
                population = 46233344
            ),
            RemoteCountryDTO(
                cca2 = "BR",
                name = RemoteCountryNameDTO("Brazil", "Federative Republic of Brazil"),
                region = "Americas",
                subregion = "South America",
                capital = listOf("Brasilia"),
                currencies = mapOf("BRL" to RemoteCurrencyDTO("Brazilian real", "R$")),
                languages = mapOf("por" to "Portuguese"),
                timezones = listOf("UTC-02:00", "UTC-03:00", "UTC-04:00", "UTC-05:00"),
                latlng = listOf(-10.0, -55.0),
                flags = RemoteFlagsDTO("https://flagcdn.com/w320/br.png", "https://flagcdn.com/br.svg"),
                population = 215313498
            ),
            RemoteCountryDTO(
                cca2 = "US",
                name = RemoteCountryNameDTO("United States", "United States of America"),
                region = "Americas",
                subregion = "North America",
                capital = listOf("Washington"),
                currencies = mapOf("USD" to RemoteCurrencyDTO("United States dollar", "$")),
                languages = mapOf("eng" to "English"),
                timezones = listOf("UTC-08:00", "UTC-05:00"),
                latlng = listOf(38.0, -97.0),
                flags = RemoteFlagsDTO("https://flagcdn.com/w320/us.png", "https://flagcdn.com/us.svg"),
                population = 328239523
            ),
            RemoteCountryDTO(
                cca2 = "ES",
                name = RemoteCountryNameDTO("Spain", "Kingdom of Spain"),
                region = "Europe",
                subregion = "Southern Europe",
                capital = listOf("Madrid"),
                currencies = mapOf("EUR" to RemoteCurrencyDTO("Euro", "EUR")),
                languages = mapOf("spa" to "Spanish"),
                timezones = listOf("UTC+00:00", "UTC+01:00"),
                latlng = listOf(40.0, -3.0),
                flags = RemoteFlagsDTO("https://flagcdn.com/w320/es.png", "https://flagcdn.com/es.svg"),
                population = 47615034
            ),
            RemoteCountryDTO(
                cca2 = "JP",
                name = RemoteCountryNameDTO("Japan", "Japan"),
                region = "Asia",
                subregion = "Eastern Asia",
                capital = listOf("Tokyo"),
                currencies = mapOf("JPY" to RemoteCurrencyDTO("Japanese yen", "JPY")),
                languages = mapOf("jpn" to "Japanese"),
                timezones = listOf("UTC+09:00"),
                latlng = listOf(36.0, 138.0),
                flags = RemoteFlagsDTO("https://flagcdn.com/w320/jp.png", "https://flagcdn.com/jp.svg"),
                population = 125124989
            )
        )
    }

    private fun parseCountriesPayload(payload: String): List<RemoteCountryDTO> {
        val root = json.parseToJsonElement(payload)

        if (root is JsonArray) {
            return json.decodeFromJsonElement(root)
        }

        if (root is JsonObject) {
            // Manejar respuestas de error con diferentes estructuras.
            // Ejemplos:
            // { "message": "..." }
            // { "error": "..." }
            // { "details": "..." }
            // { "success": false, "errors": [ { "message": "..." } ] }
            val messageFromSimpleKeys = root["message"]?.toString()?.trim('"')
                ?: root["error"]?.toString()?.trim('"')
                ?: root["details"]?.toString()?.trim('"')

            if (!messageFromSimpleKeys.isNullOrEmpty()) {
                throw IllegalStateException(messageFromSimpleKeys)
            }

            // Si la respuesta trae un array "errors" con objetos que contienen "message"
            val errorsElement = root["errors"]
            if (errorsElement is JsonArray && errorsElement.isNotEmpty()) {
                val firstError = errorsElement[0]
                if (firstError is JsonObject) {
                    val errMsg = firstError["message"]?.toString()?.trim('"')
                    if (!errMsg.isNullOrEmpty()) {
                        throw IllegalStateException(errMsg)
                    }
                }
            }

            throw IllegalStateException("Respuesta inesperada de RestCountries")
        }

        throw IllegalStateException("Formato de respuesta no soportado")
    }
}
