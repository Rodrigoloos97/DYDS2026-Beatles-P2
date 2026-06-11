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

class GetCountriesUseCaseTest {
    private lateinit var fakeRepository: FakeCountriesRepository
    private lateinit var useCase: GetCountriesUseCase

    @Before
    fun setup() {
        fakeRepository = FakeCountriesRepository()
        useCase = GetCountriesUseCaseImpl(fakeRepository)
    }

    @Test
    fun `invoke should return Success with countries list`() = runTest {
        // Arrange
        val countries = listOf(
            createMockCountry("AR", "Argentina"),
            createMockCountry("BR", "Brazil")
        )
        fakeRepository.setResult(Result.Success(countries))

        // Act
        val result = useCase()

        // Assert
        assertTrue(result is Result.Success)
        assertEquals((result as Result.Success).value, countries)
    }

    @Test
    fun `invoke should return Failure on exception`() = runTest {
        // Arrange
        val exception = Exception("Network error")
        fakeRepository.setResult(Result.Failure(exception))

        // Act
        val result = useCase()

        // Assert
        assertTrue(result is Result.Failure)
        assertEquals((result as Result.Failure).exception.message, "Network error")
    }
}

private class FakeCountriesRepository : CountriesRepository {
    private var result: Result<List<Country>> = Result.Success(emptyList())

    fun setResult(newResult: Result<List<Country>>) {
        result = newResult
    }

    override suspend fun getCountries(): Result<List<Country>> = result

    override suspend fun searchCountries(query: String): Result<List<Country>> =
        Result.Success(emptyList())

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

