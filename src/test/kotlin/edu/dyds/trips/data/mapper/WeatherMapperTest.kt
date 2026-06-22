package edu.dyds.trips.data.mapper

import edu.dyds.trips.data.remote.weather.DailyDTO
import edu.dyds.trips.data.remote.weather.RemoteWeatherDTO
import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherMapperTest {

    @Test
    fun `toDomain should map a full RemoteWeatherDTO correctly`() {
        // Given
        val remoteDto = RemoteWeatherDTO(
            daily = DailyDTO(
                time = listOf("2026-06-20", "2026-06-21"),
                temperature2mMax = listOf(25.0, 26.0),
                temperature2mMin = listOf(15.0, 16.0),
                precipitationSum = listOf(0.0, 5.0),
                windspeed10mMax = listOf(10.0, 15.0),
                weatherCode = listOf(0, 61) // Sunny, Rainy
            )
        )

        // When
        val domainForecasts = remoteDto.toDomain()

        // Then
        assertEquals(2, domainForecasts.size)

        val firstDay = domainForecasts[0]
        assertEquals("2026-06-20", firstDay.date)
        assertEquals(15.0, firstDay.tempMinCelsius, 0.0)
        assertEquals(25.0, firstDay.tempMaxCelsius, 0.0)
        assertEquals(0.0, firstDay.precipitationMm, 0.0)
        assertEquals(10.0, firstDay.windSpeedKmh, 0.0)
        assertEquals(0, firstDay.weatherCode)
        assertEquals("Sunny", firstDay.description)

        val secondDay = domainForecasts[1]
        assertEquals("2026-06-21", secondDay.date)
        assertEquals(16.0, secondDay.tempMinCelsius, 0.0)
        assertEquals(26.0, secondDay.tempMaxCelsius, 0.0)
        assertEquals(5.0, secondDay.precipitationMm, 0.0)
        assertEquals(15.0, secondDay.windSpeedKmh, 0.0)
        assertEquals(61, secondDay.weatherCode)
        assertEquals("Rainy", secondDay.description)
    }

    @Test
    fun `toDomain should handle mismatched list sizes gracefully`() {
        // Given
        val remoteDto = RemoteWeatherDTO(
            daily = DailyDTO(
                time = listOf("2026-06-20", "2026-06-21"),
                temperature2mMax = listOf(25.0), // Shorter list
                temperature2mMin = listOf(15.0, 16.0),
                precipitationSum = listOf(0.0, 5.0),
                windspeed10mMax = listOf(10.0, 15.0),
                weatherCode = listOf(0) // Shorter list
            )
        )

        // When
        val domainForecasts = remoteDto.toDomain()

        // Then
        assertEquals(2, domainForecasts.size)

        val firstDay = domainForecasts[0]
        assertEquals(25.0, firstDay.tempMaxCelsius, 0.0)
        assertEquals("Sunny", firstDay.description)

        val secondDay = domainForecasts[1]
        assertEquals("2026-06-21", secondDay.date)
        // Check that default values are used due to shorter lists
        assertEquals(0.0, secondDay.tempMaxCelsius, 0.0)
        assertEquals(-1, secondDay.weatherCode)
        assertEquals("Unknown", secondDay.description)
    }

    @Test
    fun `weatherCodeToDescription should return correct descriptions`() {
        assertEquals("Sunny", weatherCodeToDescription(0))
        assertEquals("Partly cloudy", weatherCodeToDescription(2))
        assertEquals("Overcast", weatherCodeToDescription(3))
        assertEquals("Foggy", weatherCodeToDescription(45))
        assertEquals("Rainy", weatherCodeToDescription(63))
        assertEquals("Snowy", weatherCodeToDescription(77))
        assertEquals("Stormy", weatherCodeToDescription(95))
        assertEquals("Unknown", weatherCodeToDescription(100))
        assertEquals("Unknown", weatherCodeToDescription(-1))
    }
}
