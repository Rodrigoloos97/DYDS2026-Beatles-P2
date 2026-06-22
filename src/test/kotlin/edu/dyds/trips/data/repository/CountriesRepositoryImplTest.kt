package edu.dyds.trips.data.repository

import edu.dyds.trips.data.mapper.toDomain
import edu.dyds.trips.data.remote.countries.CountriesRemoteDataSource
import edu.dyds.trips.data.remote.countries.RemoteCountryDTO
import edu.dyds.trips.data.remote.countries.RemoteCountryNameDTO
import edu.dyds.trips.data.remote.countries.RemoteCurrencyDTO
import edu.dyds.trips.data.remote.countries.RemoteFlagsDTO
import edu.dyds.trips.domain.entity.Result
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class CountriesRepositoryImplTest {
    @Test
    fun `getCountries returns success when datasource returns list`() = runTest {
        val countries = listOf(
            RemoteCountryDTO(
                cca2 = "AR",
                name = RemoteCountryNameDTO("Argentina", "Argentine Republic"),
                region = "Americas",
                capital = listOf("Buenos Aires"),
                currencies = mapOf("ARS" to RemoteCurrencyDTO("Peso", "$")),
                languages = mapOf("es" to "Spanish"),
                timezones = listOf("UTC-03:00"),
                latlng = listOf(-34.6, -58.4),
                flags = RemoteFlagsDTO("https://flagcdn.com/ar.png", "https://flagcdn.com/ar.svg"),
                population = 46000000
            )
        )

        val fake = object : CountriesRemoteDataSource {
            override suspend fun fetchCountries() = kotlin.Result.success(countries)
            override suspend fun searchCountries(query: String) = kotlin.Result.success(countries.filter { it.name.common.contains(query, true) })
        }

        val repo = CountriesRepositoryImpl(fake)
        val result = repo.getCountries()

        assertIs<Result.Success<*>>(result)
        assertEquals(1, (result as Result.Success).value.size)
        assertEquals("Argentina", result.value.first().name)
    }

    @Test
    fun `searchCountries filters by name`() = runTest {
        val countries = listOf(
            RemoteCountryDTO(
                cca2 = "AR",
                name = RemoteCountryNameDTO("Argentina", "Argentine Republic"),
                region = "Americas",
                latlng = listOf(-34.6, -58.4),
                flags = RemoteFlagsDTO(png = "https://flagcdn.com/ar.png"),
                population = 46000000
            ),
            RemoteCountryDTO(
                cca2 = "BR",
                name = RemoteCountryNameDTO("Brazil", "Federative Republic of Brazil"),
                region = "Americas",
                latlng = listOf(-10.0, -55.0),
                flags = RemoteFlagsDTO(png = "https://flagcdn.com/br.png"),
                population = 215000000
            )
        )

        val fake = object : CountriesRemoteDataSource {
            override suspend fun fetchCountries() = kotlin.Result.success(countries)
            override suspend fun searchCountries(query: String) = kotlin.Result.success(countries.filter { it.name.common.contains(query, true) })
        }

        val repo = CountriesRepositoryImpl(fake)
        val result = repo.searchCountries("arg")

        assertIs<Result.Success<*>>(result)
        assertEquals(1, (result as Result.Success).value.size)
        assertEquals("AR", result.value.first().code)
    }

    @Test
    fun `getCountryByCode returns single country`() = runTest {
        val countries = listOf(
            RemoteCountryDTO(
                cca2 = "AR",
                name = RemoteCountryNameDTO("Argentina", "Argentine Republic"),
                region = "Americas",
                latlng = listOf(-34.6, -58.4),
                flags = RemoteFlagsDTO(png = "https://flagcdn.com/ar.png"),
                population = 46000000
            )
        )

        val fake = object : CountriesRemoteDataSource {
            override suspend fun fetchCountries() = kotlin.Result.success(countries)
            override suspend fun searchCountries(query: String) = kotlin.Result.success<List<RemoteCountryDTO>>(emptyList())
        }

        val repo = CountriesRepositoryImpl(fake)
        val result = repo.getCountryByCode("AR")

        assertIs<Result.Success<*>>(result)
        assertEquals("AR", (result as Result.Success).value.code)
    }
}







