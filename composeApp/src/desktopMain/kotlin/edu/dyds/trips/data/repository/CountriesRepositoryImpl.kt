package edu.dyds.trips.data.repository

import edu.dyds.trips.data.mapper.toDomain
import edu.dyds.trips.data.remote.countries.CountriesRemoteDataSource
import edu.dyds.trips.domain.entity.Country
import edu.dyds.trips.domain.entity.Result
import edu.dyds.trips.domain.entity.mapCatching
import edu.dyds.trips.domain.entity.toDomainResult
import edu.dyds.trips.domain.repository.CountriesRepository

class CountriesRepositoryImpl(
    private val remoteDataSource: CountriesRemoteDataSource
) : CountriesRepository {
    override suspend fun getCountries(): Result<List<Country>> =
        remoteDataSource.fetchCountries()
            .map { countries -> countries.map { it.toDomain() } }
            .toDomainResult()

    override suspend fun searchCountries(query: String): Result<List<Country>> =
        remoteDataSource.searchCountries(query)
            .map { countries -> countries.map { it.toDomain() } }
            .toDomainResult()

    override suspend fun getCountryByCode(code: String): Result<Country> =
        getCountries().mapCatching { countries ->
            countries.firstOrNull { it.code.equals(code, ignoreCase = true) }
                ?: throw NoSuchElementException("Country not found for code: $code")
        }
}
