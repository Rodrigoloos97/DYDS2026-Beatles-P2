package edu.dyds.trips.data.remote.countries

import edu.dyds.trips.config.AppConfig
import edu.dyds.trips.config.AppConfigImpl
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import java.io.File

class RestCountriesClient(
    private val httpClient: HttpClient,
    private val appConfig: AppConfig = AppConfigImpl.fromEnvironment(),
    // API key hardcodeada: solo se necesita la primera vez para descargar caché
    // En ejecuciones posteriores, la app funciona offline desde el archivo local
    private val apiKey: String? = "rc_live_e70d4f2c10a849cd9f6f6569d43a10c3",
    // Deprecated: usar appConfig.countriesCacheFilePath. Se mantiene para backwards compatibility.
    private val countriesCacheFilePath: String? = null,
    private val minValidCacheCountries: Int = 50,
    private val json: Json = Json { ignoreUnknownKeys = true }
) {
    // Resuelve la ruta del caché: prioridad a parámetro legacy (si se pasa), luego a appConfig
    private val resolvedCacheFilePath: String = countriesCacheFilePath ?: appConfig.countriesCacheFilePath
    private val apiBaseUrl: String = appConfig.restCountriesBaseUrl.trimEnd('/')
    @Volatile
    private var memoryCountriesCache: List<RemoteCountryDTO>? = null

    suspend fun getCountries(): List<RemoteCountryDTO> {
        memoryCountriesCache?.let {
            if (isCacheUsable(it)) return it
        }

        val fileCached = readCountriesFromDiskCache()
        if (isCacheUsable(fileCached)) {
            memoryCountriesCache = fileCached
            return fileCached
        }

        validateApiKey()
        val remoteCountries = fetchCountriesFromApi(query = null)
        val uniqueCountries = remoteCountries.distinctBy { it.cca2 }

        memoryCountriesCache = uniqueCountries
        writeCountriesToDiskCache(uniqueCountries)

        return uniqueCountries
    }

    private fun isCacheUsable(countries: List<RemoteCountryDTO>): Boolean {
        return countries.size >= minValidCacheCountries
    }

    suspend fun searchCountries(name: String): List<RemoteCountryDTO> {
        val normalized = name.trim()
        if (normalized.isEmpty()) {
            return getCountries()
        }

        val countries = getCountries()
        return countries.filter {
            it.name.common.contains(normalized, ignoreCase = true) ||
                it.cca2.contains(normalized, ignoreCase = true)
        }
    }

    private suspend fun fetchCountriesFromApi(query: String?): List<RemoteCountryDTO> {
        val pageSize = 100
        val collected = mutableListOf<RemoteCountryDTO>()
        var offset = 0

        while (true) {
            val page = try {
                parseCountriesPayload(
                    httpClient.get(apiBaseUrl) {
                        applyCommonRequestConfig()
                        if (!query.isNullOrBlank()) {
                            parameter("q", query)
                        }
                        parameter("limit", pageSize)
                        parameter("offset", offset)
                    }.bodyAsText()
                )
            } catch (e: IllegalStateException) {
                throw e
            } catch (e: Exception) {
                throw IllegalStateException("No se pudo conectar con Rest Countries", e)
            }

            if (page.isEmpty()) break
            collected += page

            if (page.size < pageSize) break
            offset += pageSize

            // Limite de seguridad para evitar loops infinitos por respuestas inesperadas
            if (offset > 10_000) break
        }

        return collected
    }

    private fun readCountriesFromDiskCache(): List<RemoteCountryDTO> {
        return try {
            val file = File(resolvedCacheFilePath)
            if (!file.exists()) return emptyList()
            if (file.length() == 0L) return emptyList()

            val content = file.readText()
            if (content.isBlank()) return emptyList()

            json.decodeFromString<List<RemoteCountryDTO>>(content)
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun writeCountriesToDiskCache(countries: List<RemoteCountryDTO>) {
        if (countries.isEmpty()) return

        try {
            val file = File(resolvedCacheFilePath)
            file.parentFile?.mkdirs()
            file.writeText(json.encodeToString(countries))
        } catch (_: Exception) {
            // Cache best-effort: no interrumpir flujo principal por fallas de IO.
        }
    }

    private fun validateApiKey() {
        if (apiKey.isNullOrBlank()) {
            throw IllegalStateException(
                "REST_COUNTRIES_API_KEY no configurada. Configura la variable de entorno para usar la API remota."
            )
        }
    }

    private fun io.ktor.client.request.HttpRequestBuilder.applyCommonRequestConfig() {
        header(HttpHeaders.Authorization, "Bearer $apiKey")
    }

    private fun parseCountriesPayload(payload: String): List<RemoteCountryDTO> {
        val root = json.parseToJsonElement(payload)

        if (root is JsonArray) {
            // Compatibilidad con formato legacy (array de paises directo)
            return json.decodeFromJsonElement(root)
        }

        if (root is JsonObject) {
            // Formato nuevo: { data: { objects: [...] } }
            val dataObject = root["data"] as? JsonObject
            val objectsArray = dataObject?.get("objects") as? JsonArray
            if (objectsArray != null) {
                return objectsArray.mapNotNull { mapNewCountry(it as? JsonObject) }
            }

            // Compatibilidad con variante: { data: [...] }
            val dataArray = root["data"] as? JsonArray
            if (dataArray != null) {
                return json.decodeFromJsonElement(dataArray)
            }

            val messageFromSimpleKeys = root.readString("message")
                ?: root.readString("error")
                ?: root.readString("details")

            if (!messageFromSimpleKeys.isNullOrEmpty()) {
                throw IllegalStateException(messageFromSimpleKeys)
            }

            val errorsElement = root["errors"] as? JsonArray
            if (!errorsElement.isNullOrEmpty()) {
                val firstError = errorsElement.firstOrNull() as? JsonObject
                val errMsg = firstError?.readString("message")
                if (!errMsg.isNullOrEmpty()) {
                    throw IllegalStateException(errMsg)
                }
            }

            throw IllegalStateException("Respuesta inesperada de RestCountries")
        }

        throw IllegalStateException("Formato de respuesta no soportado")
    }

    private fun mapNewCountry(obj: JsonObject?): RemoteCountryDTO? {
        if (obj == null) return null

        val codes = obj["codes"] as? JsonObject
        val code = codes?.readString("alpha_2")
            ?: obj.readString("cca2")
            ?: return null

        val names = (obj["names"] as? JsonObject) ?: (obj["name"] as? JsonObject)
        val commonName = names?.readString("common") ?: code
        val officialName = names?.readString("official") ?: commonName

        val capitalsElement = obj["capitals"] ?: obj["capital"]
        val capitals = parseCapitals(capitalsElement)

        val currencies = parseCurrencies(obj["currencies"])
        val languages = parseLanguages(obj["languages"])
        val timezones = parseTimezones(obj["timezones"])
        val latlng = parseLatLng(obj)
        val flags = parseFlags(obj)

        return RemoteCountryDTO(
            cca2 = code,
            name = RemoteCountryNameDTO(common = commonName, official = officialName),
            region = obj.readString("region") ?: "",
            subregion = obj.readString("subregion") ?: "",
            capital = capitals,
            currencies = currencies,
            languages = languages,
            timezones = timezones,
            latlng = latlng,
            flags = flags,
            population = obj.readInt("population") ?: 0
        )
    }

    private fun parseCapitals(element: JsonElement?): List<String> {
        return when (element) {
            is JsonArray -> element.mapNotNull { e ->
                when (e) {
                    is JsonPrimitive -> e.content.takeIf { it.isNotBlank() }
                    is JsonObject -> e.readString("name")?.takeIf { it.isNotBlank() }
                    else -> null
                }
            }

            is JsonObject -> listOfNotNull(element.readString("name")?.takeIf { it.isNotBlank() })
            is JsonPrimitive -> listOfNotNull(element.content.takeIf { it.isNotBlank() })
            else -> emptyList()
        }
    }

    private fun parseCurrencies(element: JsonElement?): Map<String, RemoteCurrencyDTO> {
        if (element is JsonObject) {
            val directCode = element.readString("code")
            if (!directCode.isNullOrBlank()) {
                return mapOf(
                    directCode to RemoteCurrencyDTO(
                        name = element.readString("name") ?: directCode,
                        symbol = element.readString("symbol") ?: directCode
                    )
                )
            }

            // Compatibilidad con formato legacy: { "USD": { "name": "...", "symbol": "..." } }
            return element.mapNotNull { (code, value) ->
                val valueObj = value as? JsonObject ?: return@mapNotNull null
                code to RemoteCurrencyDTO(
                    name = valueObj.readString("name") ?: code,
                    symbol = valueObj.readString("symbol") ?: code
                )
            }.toMap()
        }

        if (element is JsonArray) {
            return element.mapNotNull { currencyElement ->
                val obj = currencyElement as? JsonObject ?: return@mapNotNull null
                val code = obj.readString("code") ?: return@mapNotNull null
                code to RemoteCurrencyDTO(
                    name = obj.readString("name") ?: code,
                    symbol = obj.readString("symbol") ?: code
                )
            }.toMap()
        }

        return emptyMap()
    }

    private fun parseLanguages(element: JsonElement?): Map<String, String> {
        if (element is JsonObject) {
            // Compatibilidad formato legacy: { "spa": "Spanish" }
            return element.mapNotNull { (code, value) ->
                val primitive = value as? JsonPrimitive ?: return@mapNotNull null
                val name = primitive.content
                code to name
            }.toMap()
        }

        if (element is JsonArray) {
            return element.mapNotNull { languageElement ->
                val obj = languageElement as? JsonObject ?: return@mapNotNull null
                val code = obj.readString("iso639_3")
                    ?: obj.readString("iso639_1")
                    ?: obj.readString("bcp47")
                    ?: return@mapNotNull null
                val name = obj.readString("name") ?: return@mapNotNull null
                code to name
            }.toMap()
        }

        return emptyMap()
    }

    private fun parseTimezones(element: JsonElement?): List<String> {
        return when (element) {
            is JsonArray -> element.mapNotNull { (it as? JsonPrimitive)?.content }
            is JsonPrimitive -> listOfNotNull(element.content)
            else -> emptyList()
        }
    }

    private fun parseLatLng(obj: JsonObject): List<Double> {
        val coordinates = obj["coordinates"] as? JsonObject
        val lat = coordinates?.readDouble("lat")
        val lng = coordinates?.readDouble("lng")
        if (lat != null && lng != null) {
            return listOf(lat, lng)
        }

        val legacy = obj["latlng"] as? JsonArray
        if (legacy != null) {
            return legacy.mapNotNull { (it as? JsonPrimitive)?.content?.toDoubleOrNull() }
        }

        return emptyList()
    }

    private fun parseFlags(obj: JsonObject): RemoteFlagsDTO {
        val flag = (obj["flag"] as? JsonObject) ?: (obj["flags"] as? JsonObject)
        val png = flag?.readString("url_png") ?: flag?.readString("png") ?: ""
        val svg = flag?.readString("url_svg") ?: flag?.readString("svg") ?: ""
        return RemoteFlagsDTO(png = png, svg = svg)
    }

    private fun JsonObject.readString(key: String): String? {
        return (this[key] as? JsonPrimitive)?.content
    }

    private fun JsonObject.readInt(key: String): Int? {
        return (this[key] as? JsonPrimitive)?.content?.toIntOrNull()
    }

    private fun JsonObject.readDouble(key: String): Double? {
        return (this[key] as? JsonPrimitive)?.content?.toDoubleOrNull()
    }
}

