package edu.dyds.trips.domain.usecase

import edu.dyds.trips.domain.entity.Country
import edu.dyds.trips.domain.entity.Currency
import edu.dyds.trips.domain.entity.Result
import edu.dyds.trips.domain.repository.CountriesRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SearchCountriesUseCaseTest {
    @Test
    fun `filters countries by query`() = runTest {
        val useCase = SearchCountriesUseCaseImpl(FakeCountriesRepository())

        val result = useCase("arg")

        assertTrue(result is Result.Success)
        assertEquals("AR", result.value.first().code)
    }
}


