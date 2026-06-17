package edu.dyds.trips.domain.usecase

import edu.dyds.trips.domain.entity.CountryDetail
import edu.dyds.trips.domain.entity.Result
import edu.dyds.trips.domain.repository.CountriesRepository
import edu.dyds.trips.domain.repository.WeatherRepository

interface GetCountryDetailsUseCase {
    suspend operator fun invoke(countryCode: String): Result<CountryDetail>
}

class GetCountryDetailsUseCaseImpl(
    private val countriesRepository: CountriesRepository,
    private val weatherRepository: WeatherRepository
) : GetCountryDetailsUseCase {
    override suspend fun invoke(countryCode: String): Result<CountryDetail> {
        return when (val countryResult = countriesRepository.getCountryByCode(countryCode)) {
            is Result.Failure -> countryResult
            is Result.Success -> {
                val weatherForecast = when (
                    val weatherResult = weatherRepository.getWeatherForecast(
                        countryResult.value.latitude,
                        countryResult.value.longitude
                    )
                ) {
                    is Result.Success -> weatherResult.value
                    is Result.Failure -> emptyList()
                }

                Result.Success(
                    CountryDetail(
                        country = countryResult.value,
                        weatherForecast = weatherForecast
                    )
                )
            }
        }
    }
}

