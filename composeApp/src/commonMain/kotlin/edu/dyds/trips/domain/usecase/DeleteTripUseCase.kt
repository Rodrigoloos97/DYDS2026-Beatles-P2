package edu.dyds.trips.domain.usecase

import edu.dyds.trips.domain.entity.Result
import edu.dyds.trips.domain.repository.TripsRepository

interface DeleteTripUseCase {
    suspend operator fun invoke(id: String): Result<Unit>
}

class DeleteTripUseCaseImpl(
    private val repository: TripsRepository
) : DeleteTripUseCase {
    override suspend fun invoke(id: String): Result<Unit> = repository.deleteTrip(id)
}

