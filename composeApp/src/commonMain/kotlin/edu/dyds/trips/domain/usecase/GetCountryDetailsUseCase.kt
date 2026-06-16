package edu.dyds.trips.domain.usecase

import edu.dyds.trips.domain.entity.CountryDetail
import edu.dyds.trips.domain.entity.Result
import edu.dyds.trips.domain.entity.mapCatching
import edu.dyds.trips.domain.repository.CountriesRepository

interface GetCountryDetailsUseCase {
    suspend operator fun invoke(countryCode: String): Result<CountryDetail>
}

class GetCountryDetailsUseCaseImpl(
    private val repository: CountriesRepository
) : GetCountryDetailsUseCase {
    override suspend fun invoke(countryCode: String): Result<CountryDetail> =
        repository.getCountryByCode(countryCode).mapCatching { country ->
            CountryDetail(country = country, weatherForecast = emptyList())
        }
}

