package edu.dyds.trips.domain.usecase

import edu.dyds.trips.domain.entity.Result
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetTripsUseCaseTest {
    @Test
    fun `returns trips from repository`() = runTest {
        val useCase = GetTripsUseCaseImpl(TestTripsRepository())

        val result = useCase()

        assertTrue(result is Result.Success)
        assertEquals(1, result.value.size)
        assertEquals("Argentina", result.value.first().countryName)
    }
}

