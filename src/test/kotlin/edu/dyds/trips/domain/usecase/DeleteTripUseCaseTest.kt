package edu.dyds.trips.domain.usecase

import edu.dyds.trips.domain.entity.Result
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class DeleteTripUseCaseTest {
    @Test
    fun `deletes trip by id`() = runTest {
        val useCase = DeleteTripUseCaseImpl(TestTripsRepository())

        val result = useCase("trip-1")

        assertTrue(result is Result.Success)
    }
}
