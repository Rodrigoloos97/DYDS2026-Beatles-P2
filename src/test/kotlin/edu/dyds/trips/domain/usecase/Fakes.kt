package edu.dyds.trips.domain.usecase

import edu.dyds.trips.domain.entity.Country
import edu.dyds.trips.domain.entity.Currency
import edu.dyds.trips.domain.entity.Result
import edu.dyds.trips.domain.entity.Trip
import edu.dyds.trips.domain.entity.WeatherForecast
import edu.dyds.trips.domain.repository.CountriesRepository
import edu.dyds.trips.domain.repository.TripsRepository
import edu.dyds.trips.domain.repository.WeatherRepository

class TestCountriesRepository(
    private val countries: List<Country> = listOf(sampleCountry())
) : CountriesRepository {
    override suspend fun getCountries(): Result<List<Country>> = Result.Success(countries)

    override suspend fun searchCountries(query: String): Result<List<Country>> =
        Result.Success(countries.filter { it.name.contains(query, ignoreCase = true) })

    override suspend fun getCountryByCode(code: String): Result<Country> =
        countries.firstOrNull { it.code == code }
            ?.let { Result.Success(it) }
            ?: Result.Failure(NoSuchElementException("Country not found"))
}

class TestTripsRepository(
    private val trips: MutableList<Trip> = mutableListOf(sampleTrip())
) : TripsRepository {
    override suspend fun getTrips(): Result<List<Trip>> = Result.Success(trips.toList())

    override suspend fun getTripById(id: String): Result<Trip?> =
        Result.Success(trips.firstOrNull { it.id == id })

    override suspend fun saveTrip(trip: Trip): Result<Unit> {
        trips.add(trip)
        return Result.Success(Unit)
    }

    override suspend fun updateTrip(trip: Trip): Result<Unit> {
        val index = trips.indexOfFirst { it.id == trip.id }
        if (index >= 0) {
            trips[index] = trip
            return Result.Success(Unit)
        }
        return Result.Failure(NoSuchElementException("Trip not found"))
    }

    override suspend fun deleteTrip(id: String): Result<Unit> {
        trips.removeAll { it.id == id }
        return Result.Success(Unit)
    }
}

class TestWeatherRepository : WeatherRepository {
    override suspend fun getWeatherForecast(
        latitude: Double,
        longitude: Double
    ): Result<List<WeatherForecast>> = Result.Success(
        listOf(
            WeatherForecast(
                date = "2026-06-20",
                tempMinCelsius = 11.0,
                tempMaxCelsius = 19.0,
                precipitationMm = 0.0,
                windSpeedKmh = 12.0,
                weatherCode = 0,
                description = "Sunny"
            )
        )
    )
}

typealias FakeCountriesRepository = TestCountriesRepository
typealias FakeTripsRepository = TestTripsRepository
typealias FakeWeatherRepository = TestWeatherRepository

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

fun sampleTrip(): Trip = Trip(
    id = "trip-1",
    countryCode = "AR",
    countryName = "Argentina",
    startDate = "2026-06-20",
    endDate = "2026-06-25",
    notes = "Vacaciones",
    createdAt = 1L
)

