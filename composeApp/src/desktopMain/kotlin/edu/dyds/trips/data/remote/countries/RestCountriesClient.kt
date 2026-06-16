package edu.dyds.trips.data.remote.countries

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

class RestCountriesClient(
    private val httpClient: HttpClient,
    private val json: Json = Json { ignoreUnknownKeys = true }
) {
    suspend fun getCountries(): List<RemoteCountryDTO> =
        parseCountriesPayload(
            httpClient.get("https://restcountries.com/v3.1/all") {
                parameter(
                    "fields",
                    "cca2,name,region,subregion,capital,currencies,languages,timezones,latlng,flags,population"
                )
            }.bodyAsText()
        )

    suspend fun searchCountries(name: String): List<RemoteCountryDTO> =
        parseCountriesPayload(
            httpClient.get("https://restcountries.com/v3.1/name/$name") {
                parameter(
                    "fields",
                    "cca2,name,region,subregion,capital,currencies,languages,timezones,latlng,flags,population"
                )
            }.bodyAsText()
        )

    private fun parseCountriesPayload(payload: String): List<RemoteCountryDTO> {
        val root = json.parseToJsonElement(payload)

        if (root is JsonArray) {
            return json.decodeFromJsonElement(root)
        }

        if (root is JsonObject) {
            val message = root["message"]?.toString()?.trim('"')
                ?: root["error"]?.toString()?.trim('"')
                ?: root["details"]?.toString()?.trim('"')
                ?: "Respuesta inesperada de RestCountries"
            throw IllegalStateException(message)
        }

        throw IllegalStateException("Formato de respuesta no soportado")
    }
}
