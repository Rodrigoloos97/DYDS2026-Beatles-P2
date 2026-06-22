package edu.dyds.trips.data.remote.weather

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteWeatherDTO(
    val daily: DailyDTO
)

@Serializable
data class DailyDTO(
    val time: List<String>,
    @SerialName("temperature_2m_max")
    val temperature2mMax: List<Double>,
    @SerialName("temperature_2m_min")
    val temperature2mMin: List<Double>,
    @SerialName("precipitation_sum")
    val precipitationSum: List<Double>,
    @SerialName("windspeed_10m_max")
    val windspeed10mMax: List<Double>,
    @SerialName("weather_code")
    val weatherCode: List<Int>
)

