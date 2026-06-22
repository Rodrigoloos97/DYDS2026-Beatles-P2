package edu.dyds.trips.data.remote.countries

import kotlinx.serialization.Serializable

@Serializable
data class RemoteCountryDTO(
    val cca2: String,
    val name: RemoteCountryNameDTO,
    val region: String,
    val subregion: String? = null,
    val capital: List<String>? = null,
    val currencies: Map<String, RemoteCurrencyDTO>? = null,
    val languages: Map<String, String>? = null,
    val timezones: List<String> = emptyList(),
    val latlng: List<Double> = emptyList(),
    val flags: RemoteFlagsDTO,
    val population: Int = 0
)

@Serializable
data class RemoteCountryNameDTO(
    val common: String,
    val official: String
)

@Serializable
data class RemoteCurrencyDTO(
    val name: String,
    val symbol: String = ""
)

@Serializable
data class RemoteFlagsDTO(
    val png: String = "",
    val svg: String = ""
)

