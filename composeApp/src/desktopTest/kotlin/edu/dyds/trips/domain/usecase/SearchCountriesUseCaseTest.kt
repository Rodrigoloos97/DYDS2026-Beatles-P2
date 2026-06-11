package edu.dyds.trips.domain.usecase

import edu.dyds.trips.domain.entity.Country
import edu.dyds.trips.domain.entity.Currency
import edu.dyds.trips.domain.entity.Result
import edu.dyds.trips.domain.repository.CountriesRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SearchCountriesUseCaseTest {
    private lateinit var fakeRepository: FakeCountriesRepository
    private lateinit var useCase: SearchCountriesUseCase

    @Before
    fun setup() {
        fakeRepository = FakeCountriesRepository()
        useCase = SearchCountriesUseCaseImpl(fakeRepository)
    }

    @Test
    fun `invoke should return Success with countries list matching query`() = runTest {
        // Arrange
        val countries = listOf(createMockCountry("AR", "Argentina"))
        fakeRepository.setSearchResult(Result.Success(countries))

        // Act
        val result = useCase("Argentina")

        // Assert
        assertTrue(result is Result.Success)
        assertEquals((result as Result.Success).value.size, 1)
        assertEquals((result as Result.Success).value[0].name, "Argentina")
    }

    @Test
    fun `invoke should return Success with empty list when no matches`() = runTest {
        // Arrange
        fakeRepository.setSearchResult(Result.Success(emptyList()))

        // Act
        val result = useCase("NonExistent")

        // Assert
        assertTrue(result is Result.Success)
        assertEquals((result as Result.Success).value.size, 0)
    }

    @Test
    fun `invoke should return Failure on exception`() = runTest {
        // Arrange
        val exception = Exception("Search failed")
        fakeRepository.setSearchResult(Result.Failure(exception))

        // Act
        val result = useCase("Argentina")

        // Assert
        assertTrue(result is Result.Failure)
        assertEquals((result as Result.Failure).exception.message, "Search failed")
    }
}

private class FakeCountriesRepository : CountriesRepository {
    private var searchResult: Result<List<Country>> = Result.Success(emptyList())

    fun setSearchResult(newResult: Result<List<Country>>) {
        searchResult = newResult
    }

    override suspend fun getCountries(): Result<List<Country>> =
        Result.Success(emptyList())

    override suspend fun searchCountries(query: String): Result<List<Country>> =
        searchResult

    override suspend fun getCountryByCode(code: String): Result<Country> =
        Result.Success(createMockCountry(code, "Test"))
}

private fun createMockCountry(code: String, name: String): Country = Country(
    code = code,
    name = name,
    officialName = "$name Official",
    region = "Americas",
    subregion = "South America",
    capital = "Capital",
    currencies = mapOf("CUR" to Currency("CUR", "Currency", "C")),
    languages = mapOf("es" to "Spanish"),
    timezones = listOf("UTC-03:00"),
    latitude = -34.6037,
    longitude = -58.3816,
    flagUrl = "https://example.com/flag.png",
    population = 1000000
)

