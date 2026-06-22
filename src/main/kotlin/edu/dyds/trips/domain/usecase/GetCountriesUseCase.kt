package edu.dyds.trips.domain.usecase

import edu.dyds.trips.domain.entity.Country
import edu.dyds.trips.domain.entity.Result
import edu.dyds.trips.domain.repository.CountriesRepository

interface GetCountriesUseCase {
    suspend operator fun invoke(): Result<List<Country>>
}

class GetCountriesUseCaseImpl(
    private val repository: CountriesRepository
) : GetCountriesUseCase {
    override suspend fun invoke(): Result<List<Country>> = repository.getCountries()
}

