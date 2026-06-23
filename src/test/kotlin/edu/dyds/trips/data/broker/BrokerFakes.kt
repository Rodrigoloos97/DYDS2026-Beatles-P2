package edu.dyds.trips.data.broker

import edu.dyds.trips.domain.entity.Country
import edu.dyds.trips.domain.entity.Currency
import edu.dyds.trips.domain.entity.Result
import edu.dyds.trips.domain.entity.WeatherForecast
import edu.dyds.trips.domain.repository.CountriesRepository
import edu.dyds.trips.domain.repository.WeatherRepository

class FakeCountriesRepository(
    private val countryResult: Result<Country> = Result.Success(sampleCountry()),
    private val throwOnCall: Exception? = null
) : CountriesRepository {

    var capturedCode: String? = null
        private set

    override suspend fun getCountryByCode(code: String): Result<Country> {
        capturedCode = code
        throwOnCall?.let { throw it }
        return countryResult
    }

    override suspend fun getCountries(): Result<List<Country>> = Result.Success(emptyList())
    override suspend fun searchCountries(query: String): Result<List<Country>> = Result.Success(emptyList())
}

class FakeWeatherRepository(
    private val weatherResult: Result<List<WeatherForecast>> = Result.Success(listOf(sampleWeatherForecast()))
) : WeatherRepository {

    var capturedLatitude: Double? = null
        private set
    var capturedLongitude: Double? = null
        private set

    override suspend fun getWeatherForecast(
        latitude: Double,
        longitude: Double
    ): Result<List<WeatherForecast>> {
        capturedLatitude = latitude
        capturedLongitude = longitude
        return weatherResult
    }
}

fun sampleCountry(): Country = Country(
    code = "AR",
    name = "Argentina",
    officialName = "Argentine Republic",
    region = "Americas",
    subregion = "South America",
    capital = "Buenos Aires",
    currencies = mapOf("ARS" to Currency("ARS", "Peso", "$")),
    languages = mapOf("es" to "Spanish"),
    timezones = listOf("UTC-03:00"),
    latitude = -34.6,
    longitude = -58.4,
    flagUrl = "https://flagcdn.com/ar.png",
    population = 46000000
)

fun sampleWeatherForecast(): WeatherForecast = WeatherForecast(
    date = "2026-06-20",
    tempMinCelsius = 11.0,
    tempMaxCelsius = 19.0,
    precipitationMm = 0.0,
    windSpeedKmh = 12.0,
    weatherCode = 0,
    description = "Sunny"
)


