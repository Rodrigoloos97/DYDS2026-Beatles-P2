package edu.dyds.trips.domain.entity

data class CountryDetail(
    val country: Country,
    val weatherForecast: List<WeatherForecast>
)

