package edu.dyds.trips.data.mapper

import edu.dyds.trips.data.remote.countries.RemoteCountryDTO
import edu.dyds.trips.data.remote.countries.RemoteCountryNameDTO
import edu.dyds.trips.data.remote.countries.RemoteCurrencyDTO
import edu.dyds.trips.data.remote.countries.RemoteFlagsDTO
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CountryMapperTest {

    @Test
    fun `toDomain should map a full RemoteCountryDTO correctly`() {
        // Given
        val remoteDto = RemoteCountryDTO(
            cca2 = "AR",
            name = RemoteCountryNameDTO(common = "Argentina", official = "Argentine Republic"),
            region = "Americas",
            subregion = "South America",
            capital = listOf("Buenos Aires"),
            currencies = mapOf("ARS" to RemoteCurrencyDTO(name = "Peso", symbol = "$")),
            languages = mapOf("es" to "Spanish"),
            timezones = listOf("UTC-03:00"),
            latlng = listOf(-34.6, -58.4),
            flags = RemoteFlagsDTO(png = "flag.png", svg = "flag.svg"),
            population = 46000000
        )

        // When
        val domainCountry = remoteDto.toDomain()

        // Then
        assertEquals("AR", domainCountry.code)
        assertEquals("Argentina", domainCountry.name)
        assertEquals("Argentine Republic", domainCountry.officialName)
        assertEquals("Americas", domainCountry.region)
        assertEquals("South America", domainCountry.subregion)
        assertEquals("Buenos Aires", domainCountry.capital)
        assertEquals("Peso", domainCountry.currencies["ARS"]?.name)
        assertEquals("Spanish", domainCountry.languages["es"])
        assertEquals("UTC-03:00", domainCountry.timezones.first())
        assertEquals(-34.6, domainCountry.latitude, 0.0)
        assertEquals(-58.4, domainCountry.longitude, 0.0)
        assertEquals("flag.png", domainCountry.flagUrl)
        assertEquals(46000000, domainCountry.population)
    }

    @Test
    fun `toDomain should handle nullable and empty fields gracefully`() {
        // Given
        val remoteDto = RemoteCountryDTO(
            cca2 = "XX",
            name = RemoteCountryNameDTO(common = "Testland", official = "Republic of Test"),
            region = "Null Island",
            subregion = null,
            capital = null,
            currencies = null,
            languages = emptyMap(),
            timezones = emptyList(),
            latlng = emptyList(),
            flags = RemoteFlagsDTO(png = "", svg = "fallback.svg"),
            population = 0
        )

        // When
        val domainCountry = remoteDto.toDomain()

        // Then
        assertEquals("XX", domainCountry.code)
        assertEquals("Testland", domainCountry.name)
        assertTrue(domainCountry.subregion == null)
        assertTrue(domainCountry.capital == null)
        assertTrue(domainCountry.currencies.isEmpty())
        assertTrue(domainCountry.languages.isEmpty())
        assertTrue(domainCountry.timezones.isEmpty())
        assertEquals(0.0, domainCountry.latitude, 0.0)
        assertEquals(0.0, domainCountry.longitude, 0.0)
        assertEquals("fallback.svg", domainCountry.flagUrl) // Checks fallback
        assertEquals(0, domainCountry.population)
    }

    @Test
    fun `toDomain should use svg flag when png is blank`() {
        // Given
        val remoteDto = RemoteCountryDTO(
            cca2 = "AR",
            name = RemoteCountryNameDTO(common = "Argentina", official = "Argentine Republic"),
            region = "Americas",
            flags = RemoteFlagsDTO(png = "  ", svg = "flag.svg")
        )

        // When
        val domainCountry = remoteDto.toDomain()

        // Then
        assertEquals("flag.svg", domainCountry.flagUrl)
    }
}
