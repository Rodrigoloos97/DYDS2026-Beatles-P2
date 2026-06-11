package edu.dyds.trips.domain.usecase

import edu.dyds.trips.domain.entity.Country
import edu.dyds.trips.domain.entity.CountryDetail
import edu.dyds.trips.domain.entity.Currency
import edu.dyds.trips.domain.entity.Result
import edu.dyds.trips.domain.repository.CountriesRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GetCountryDetailsUseCaseTest {
    private lateinit var fakeRepository: FakeCountriesRepository
    private lateinit var useCase: GetCountryDetailsUseCase

    @Before
    fun setup() {
        fakeRepository = FakeCountriesRepository()
        useCase = GetCountryDetailsUseCaseImpl(fakeRepository)
    }

    @Test
    fun `invoke should return Success with CountryDetail`() = runTest {
        // Arrange
        val country = createMockCountry("AR", "Argentina")
        fakeRepository.setCountryByCodeResult(Result.Success(country))

        // Act
        val result = useCase("AR")

        // Assert
        assertTrue(result is Result.Success)
        val detail = (result as Result.Success).value
        assertNotNull(detail)
        assertEquals(detail.country.code, "AR")
        assertEquals(detail.weatherForecast.size, 0)
    }

    @Test
    fun `invoke should return Failure when country not found`() = runTest {
        // Arrange
        val exception = Exception("Country not found")
        fakeRepository.setCountryByCodeResult(Result.Failure(exception))

        // Act
        val result = useCase("XX")

        // Assert
        assertTrue(result is Result.Failure)
    }

    @Test
    fun `invoke should populate country and empty weather forecast`() = runTest {
        // Arrange
        val country = createMockCountry("BR", "Brazil")
        fakeRepository.setCountryByCodeResult(Result.Success(country))

        // Act
        val result = useCase("BR")

        // Assert
        assertTrue(result is Result.Success)
        val detail = (result as Result.Success).value
        assertEquals(detail.country.name, "Brazil")
        assertTrue(detail.weatherForecast.isEmpty())
    }
}

private class FakeCountriesRepository : CountriesRepository {
    private var countryByCodeResult: Result<Country> = Result.Success(
        createMockCountry("XX", "Test")
    )

    fun setCountryByCodeResult(result: Result<Country>) {
        countryByCodeResult = result
    }

    override suspend fun getCountries(): Result<List<Country>> =
        Result.Success(emptyList())

    override suspend fun searchCountries(query: String): Result<List<Country>> =
        Result.Success(emptyList())

    override suspend fun getCountryByCode(code: String): Result<Country> =
        countryByCodeResult
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

