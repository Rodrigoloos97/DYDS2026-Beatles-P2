package edu.dyds.trips.domain.entity

data class WeatherForecast(
    val date: String,
    val tempMinCelsius: Double,
    val tempMaxCelsius: Double,
    val precipitationMm: Double,
    val windSpeedKmh: Double,
    val weatherCode: Int,
    val description: String
)

