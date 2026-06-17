package edu.dyds.trips.domain.usecase

import edu.dyds.trips.domain.entity.Result
import edu.dyds.trips.domain.entity.Trip
import edu.dyds.trips.domain.repository.TripsRepository

interface GetTripsUseCase {
    suspend operator fun invoke(): Result<List<Trip>>
}

class GetTripsUseCaseImpl(
    private val repository: TripsRepository
) : GetTripsUseCase {
    override suspend fun invoke(): Result<List<Trip>> = repository.getTrips()
}

