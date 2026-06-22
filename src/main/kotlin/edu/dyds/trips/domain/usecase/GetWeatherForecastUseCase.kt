package edu.dyds.trips.domain.usecase

import edu.dyds.trips.domain.entity.Result
import edu.dyds.trips.domain.entity.WeatherForecast
import edu.dyds.trips.domain.repository.WeatherRepository

interface GetWeatherForecastUseCase {
    suspend operator fun invoke(latitude: Double, longitude: Double): Result<List<WeatherForecast>>
}

class GetWeatherForecastUseCaseImpl(
    private val repository: WeatherRepository
) : GetWeatherForecastUseCase {
    override suspend fun invoke(latitude: Double, longitude: Double): Result<List<WeatherForecast>> =
        repository.getWeatherForecast(latitude, longitude)
}

