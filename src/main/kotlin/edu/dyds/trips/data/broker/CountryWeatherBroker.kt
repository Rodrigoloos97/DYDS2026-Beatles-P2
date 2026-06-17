package edu.dyds.trips.data.broker

import edu.dyds.trips.domain.entity.CountryDetail
import edu.dyds.trips.domain.entity.Result
import edu.dyds.trips.domain.entity.getOrNull
import edu.dyds.trips.domain.entity.getOrThrow
import edu.dyds.trips.domain.repository.CountriesRepository
import edu.dyds.trips.domain.repository.WeatherRepository

class CountryWeatherBroker(
    private val countriesRepository: CountriesRepository,
    private val weatherRepository: WeatherRepository
) {
    suspend fun getCountryDetail(countryCode: String): Result<CountryDetail> =
        try {
            val country = countriesRepository.getCountryByCode(countryCode).getOrThrow()
            val weather = weatherRepository.getWeatherForecast(country.latitude, country.longitude).getOrNull()
            Result.Success(CountryDetail(country, weather ?: emptyList()))
        } catch (e: Exception) {
            Result.Failure(e)
        }
}

