package edu.dyds.trips.data.remote.weather

import edu.dyds.trips.domain.util.Constants
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class OpenMeteoClient(
    private val httpClient: HttpClient
) {
    suspend fun getForecast(
        latitude: Double,
        longitude: Double,
        timezone: String
    ): RemoteWeatherDTO =
        httpClient.get("https://api.open-meteo.com/v1/forecast") {
            parameter("latitude", latitude)
            parameter("longitude", longitude)
            parameter(
                "daily",
                "temperature_2m_max,temperature_2m_min,precipitation_sum,windspeed_10m_max,weather_code"
            )
            parameter("forecast_days", Constants.WEATHER_FORECAST_DAYS)
            parameter("timezone", timezone)
        }.body()
}

