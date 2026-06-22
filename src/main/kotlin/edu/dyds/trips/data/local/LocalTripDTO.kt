package edu.dyds.trips.data.local

import edu.dyds.trips.data.remote.countries.RemoteCountryDTO
import kotlinx.serialization.Serializable

@Serializable
data class LocalTripDTO(
    val id: String,
    val countryCode: String,
    val countryName: String,
    val startDate: String,
    val endDate: String,
    val notes: String,
    val createdAt: Long
)

@Serializable
data class AppDataFile(
    val trips: List<LocalTripDTO> = emptyList(),
    val cachedCountries: List<RemoteCountryDTO> = emptyList(),
    val lastCountriesFetchTime: Long = 0L
)

