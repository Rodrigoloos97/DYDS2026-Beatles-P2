package edu.dyds.trips.domain.usecase

import edu.dyds.trips.domain.entity.Result
import edu.dyds.trips.domain.entity.Trip
import edu.dyds.trips.domain.repository.TripsRepository

interface UpdateTripUseCase {
    suspend operator fun invoke(trip: Trip): Result<Unit>
}

class UpdateTripUseCaseImpl(
    private val repository: TripsRepository
) : UpdateTripUseCase {
    override suspend fun invoke(trip: Trip): Result<Unit> {
        if (trip.startDate > trip.endDate) {
            return Result.Failure(IllegalArgumentException("Start date cannot be after end date"))
        }
        return repository.updateTrip(trip)
    }
}


