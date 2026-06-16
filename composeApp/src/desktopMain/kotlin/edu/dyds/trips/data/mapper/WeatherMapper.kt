package edu.dyds.trips.data.mapper

import edu.dyds.trips.data.remote.weather.RemoteWeatherDTO
import edu.dyds.trips.domain.entity.WeatherForecast

fun RemoteWeatherDTO.toDomain(): List<WeatherForecast> =
    daily.time.mapIndexed { index, date ->
        WeatherForecast(
            date = date,
            tempMinCelsius = daily.temperature2mMin.getOrElse(index) { 0.0 },
            tempMaxCelsius = daily.temperature2mMax.getOrElse(index) { 0.0 },
            precipitationMm = daily.precipitationSum.getOrElse(index) { 0.0 },
            windSpeedKmh = daily.windspeed10mMax.getOrElse(index) { 0.0 },
            weatherCode = daily.weatherCode.getOrElse(index) { -1 },
            description = weatherCodeToDescription(daily.weatherCode.getOrElse(index) { -1 })
        )
    }

fun weatherCodeToDescription(code: Int): String = when (code) {
    0 -> "Sunny"
    1, 2 -> "Partly cloudy"
    3 -> "Overcast"
    45, 48 -> "Foggy"
    in 51..67 -> "Rainy"
    in 71..77 -> "Snowy"
    in 80..99 -> "Stormy"
    else -> "Unknown"
}

