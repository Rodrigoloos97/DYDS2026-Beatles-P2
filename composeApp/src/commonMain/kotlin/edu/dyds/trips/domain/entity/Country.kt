package edu.dyds.trips.domain.entity

data class Country(
    val code: String,
    val name: String,
    val officialName: String,
    val region: String,
    val subregion: String?,
    val capital: String?,
    val currencies: Map<String, Currency>,
    val languages: Map<String, String>,
    val timezones: List<String>,
    val latitude: Double,
    val longitude: Double,
    val flagUrl: String,
    val population: Int
)

