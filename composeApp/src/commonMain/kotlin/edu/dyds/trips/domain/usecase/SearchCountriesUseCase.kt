package edu.dyds.trips.domain.usecase

import edu.dyds.trips.domain.entity.Country
import edu.dyds.trips.domain.entity.Result
import edu.dyds.trips.domain.repository.CountriesRepository

interface SearchCountriesUseCase {
    suspend operator fun invoke(query: String): Result<List<Country>>
}

class SearchCountriesUseCaseImpl(
    private val repository: CountriesRepository
) : SearchCountriesUseCase {
    override suspend fun invoke(query: String): Result<List<Country>> =
        repository.searchCountries(query)
}

