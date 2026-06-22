package edu.dyds.trips.domain.usecase

import edu.dyds.trips.domain.entity.Country
import edu.dyds.trips.domain.entity.Currency
import edu.dyds.trips.domain.entity.Result
import edu.dyds.trips.domain.repository.CountriesRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetCountryDetailsUseCaseTest {
    @Test
    fun `returns country detail with empty forecast in stage1`() = runTest {
        val useCase = GetCountryDetailsUseCaseImpl(FakeCountriesRepository())

        val result = useCase("AR")

        assertTrue(result is Result.Success)
        assertEquals("Argentina", result.value.country.name)
        assertTrue(result.value.weatherForecast.isEmpty())
    }
}


