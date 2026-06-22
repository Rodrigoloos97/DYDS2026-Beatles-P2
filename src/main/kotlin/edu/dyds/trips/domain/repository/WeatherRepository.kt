package edu.dyds.trips.domain.repository

import edu.dyds.trips.domain.entity.Result
import edu.dyds.trips.domain.entity.WeatherForecast

interface WeatherRepository {
    suspend fun getWeatherForecast(
        latitude: Double,
        longitude: Double
    ): Result<List<WeatherForecast>>
}

