package edu.dyds.trips.data.repository

import edu.dyds.trips.data.remote.weather.WeatherRemoteDataSource
import edu.dyds.trips.data.remote.weather.RemoteWeatherDTO
import edu.dyds.trips.data.remote.weather.DailyDTO
import edu.dyds.trips.domain.entity.Result
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class WeatherRepositoryImplTest {
    @Test
    fun `getWeatherForecast returns forecast list`() = runTest {
        val weather = RemoteWeatherDTO(
            daily = DailyDTO(
                time = listOf("2026-06-16"),
                temperature2mMax = listOf(20.0),
                temperature2mMin = listOf(11.0),
                precipitationSum = listOf(0.5),
                windspeed10mMax = listOf(15.0),
                weatherCode = listOf(1)
            )
        )

        val fake = object : WeatherRemoteDataSource {
            override suspend fun fetchForecast(latitude: Double, longitude: Double, timezone: String) =
                kotlin.Result.success(weather)
        }

        val repo = WeatherRepositoryImpl(fake)
        val result = repo.getWeatherForecast(-34.6, -58.4)

        assertIs<Result.Success<*>>(result)
        assertEquals(1, (result as Result.Success).value.size)
        assertEquals("2026-06-16", result.value.first().date)
        assertEquals(20.0, result.value.first().tempMaxCelsius)
    }

    @Test
    fun `getWeatherForecast handles empty days gracefully`() = runTest {
        val weather = RemoteWeatherDTO(
            daily = DailyDTO(
                time = emptyList(),
                temperature2mMax = emptyList(),
                temperature2mMin = emptyList(),
                precipitationSum = emptyList(),
                windspeed10mMax = emptyList(),
                weatherCode = emptyList()
            )
        )

        val fake = object : WeatherRemoteDataSource {
            override suspend fun fetchForecast(latitude: Double, longitude: Double, timezone: String) =
                kotlin.Result.success(weather)
        }

        val repo = WeatherRepositoryImpl(fake)
        val result = repo.getWeatherForecast(-34.6, -58.4)

        assertIs<Result.Success<*>>(result)
        assertEquals(0, (result as Result.Success).value.size)
    }
}


