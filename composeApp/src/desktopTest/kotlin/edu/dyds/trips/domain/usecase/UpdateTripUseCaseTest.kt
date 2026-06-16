package edu.dyds.trips.domain.usecase

import edu.dyds.trips.domain.entity.Result
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class UpdateTripUseCaseTest {
    @Test
    fun `updates valid trip`() = runTest {
        val useCase = UpdateTripUseCaseImpl(TestTripsRepository())

        val result = useCase(sampleTrip().copy(notes = "Actualizado"))

        assertTrue(result is Result.Success)
    }

    @Test
    fun `fails when start date is after end date`() = runTest {
        val useCase = UpdateTripUseCaseImpl(TestTripsRepository())

        val result = useCase(
            sampleTrip().copy(
                startDate = "2026-07-20",
                endDate = "2026-07-01"
            )
        )

        assertTrue(result is Result.Failure)
    }
}

