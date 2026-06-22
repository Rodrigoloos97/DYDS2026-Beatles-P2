package edu.dyds.trips.domain.repository

import edu.dyds.trips.domain.entity.Result
import edu.dyds.trips.domain.entity.Trip

interface TripsRepository {
    suspend fun getTrips(): Result<List<Trip>>
    suspend fun getTripById(id: String): Result<Trip?>
    suspend fun saveTrip(trip: Trip): Result<Unit>
    suspend fun updateTrip(trip: Trip): Result<Unit>
    suspend fun deleteTrip(id: String): Result<Unit>
}

