package edu.dyds.trips.domain.repository

import edu.dyds.trips.domain.entity.Country
import edu.dyds.trips.domain.entity.Result

interface CountriesRepository {
    suspend fun getCountries(): Result<List<Country>>
    suspend fun searchCountries(query: String): Result<List<Country>>
    suspend fun getCountryByCode(code: String): Result<Country>
}

