package edu.dyds.trips.data.repository

import edu.dyds.trips.data.mapper.toDomain
import edu.dyds.trips.data.remote.weather.WeatherRemoteDataSource
import edu.dyds.trips.domain.entity.Result
import edu.dyds.trips.domain.entity.WeatherForecast
import edu.dyds.trips.domain.entity.toDomainResult
import edu.dyds.trips.domain.repository.WeatherRepository

class WeatherRepositoryImpl(
    private val remoteDataSource: WeatherRemoteDataSource
) : WeatherRepository {
    override suspend fun getWeatherForecast(
        latitude: Double,
        longitude: Double
    ): Result<List<WeatherForecast>> =
        remoteDataSource.fetchForecast(latitude, longitude, "UTC")
            .map { weather -> weather.toDomain() }
            .toDomainResult()
}

