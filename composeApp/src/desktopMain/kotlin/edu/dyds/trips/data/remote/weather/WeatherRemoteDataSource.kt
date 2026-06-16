package edu.dyds.trips.data.remote.weather

interface WeatherRemoteDataSource {
    suspend fun fetchForecast(
        latitude: Double,
        longitude: Double,
        timezone: String
    ): kotlin.Result<RemoteWeatherDTO>
}

class WeatherRemoteDataSourceImpl(
    private val client: OpenMeteoClient
) : WeatherRemoteDataSource {
    override suspend fun fetchForecast(
        latitude: Double,
        longitude: Double,
        timezone: String
    ): kotlin.Result<RemoteWeatherDTO> =
        kotlin.runCatching {
            client.getForecast(latitude, longitude, timezone)
        }
}

