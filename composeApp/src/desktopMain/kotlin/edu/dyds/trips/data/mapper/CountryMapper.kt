package edu.dyds.trips.data.mapper

import edu.dyds.trips.data.local.LocalTripDTO
import edu.dyds.trips.data.remote.countries.RemoteCountryDTO
import edu.dyds.trips.domain.entity.Country
import edu.dyds.trips.domain.entity.Currency
import edu.dyds.trips.domain.entity.Trip

fun RemoteCountryDTO.toDomain(): Country = Country(
    code = cca2,
    name = name.common,
    officialName = name.official,
    region = region,
    subregion = subregion,
    capital = capital?.firstOrNull(),
    currencies = currencies?.map { (currencyCode, remoteCurrency) ->
        currencyCode to Currency(
            code = currencyCode,
            name = remoteCurrency.name,
            symbol = remoteCurrency.symbol
        )
    }?.toMap() ?: emptyMap(),
    languages = languages ?: emptyMap(),
    timezones = timezones,
    latitude = latlng.getOrElse(0) { 0.0 },
    longitude = latlng.getOrElse(1) { 0.0 },
    flagUrl = flags.png.ifBlank { flags.svg },
    population = population
)

fun Trip.toDTO(): LocalTripDTO = LocalTripDTO(
    id = id,
    countryCode = countryCode,
    countryName = countryName,
    startDate = startDate,
    endDate = endDate,
    notes = notes,
    createdAt = createdAt
)

fun LocalTripDTO.toDomain(): Trip = Trip(
    id = id,
    countryCode = countryCode,
    countryName = countryName,
    startDate = startDate,
    endDate = endDate,
    notes = notes,
    createdAt = createdAt
)

