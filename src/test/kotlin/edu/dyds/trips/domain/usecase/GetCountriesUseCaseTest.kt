package edu.dyds.trips.domain.usecase

import edu.dyds.trips.domain.entity.Country
import edu.dyds.trips.domain.entity.Currency
import edu.dyds.trips.domain.entity.Result
import edu.dyds.trips.domain.repository.CountriesRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetCountriesUseCaseTest {
    @Test
    fun `returns countries from repository`() = runTest {
        val useCase = GetCountriesUseCaseImpl(FakeCountriesRepository())

        val result = useCase()

        assertTrue(result is Result.Success)
        assertEquals(1, result.value.size)
    }
}


