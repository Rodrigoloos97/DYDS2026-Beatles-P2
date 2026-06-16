package edu.dyds.trips.data.repository

import edu.dyds.trips.data.local.TripsLocalDataSource
import edu.dyds.trips.data.mapper.toDTO
import edu.dyds.trips.data.mapper.toDomain
import edu.dyds.trips.domain.entity.Result
import edu.dyds.trips.domain.entity.Trip
import edu.dyds.trips.domain.entity.toDomainResult
import edu.dyds.trips.domain.repository.TripsRepository

class TripsRepositoryImpl(
    private val localDataSource: TripsLocalDataSource
) : TripsRepository {
    override suspend fun getTrips(): Result<List<Trip>> =
        localDataSource.getTrips()
            .map { trips -> trips.map { it.toDomain() } }
            .toDomainResult()

    override suspend fun getTripById(id: String): Result<Trip?> =
        localDataSource.getTrips()
            .map { trips -> trips.firstOrNull { it.id == id }?.toDomain() }
            .toDomainResult()

    override suspend fun saveTrip(trip: Trip): Result<Unit> =
        localDataSource.saveTrip(trip.toDTO()).toDomainResult()

    override suspend fun updateTrip(trip: Trip): Result<Unit> =
        localDataSource.updateTrip(trip.toDTO()).toDomainResult()

    override suspend fun deleteTrip(id: String): Result<Unit> =
        localDataSource.deleteTrip(id).toDomainResult()
}

