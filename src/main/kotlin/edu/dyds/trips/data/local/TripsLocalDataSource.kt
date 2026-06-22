package edu.dyds.trips.data.local

interface TripsLocalDataSource {
    suspend fun getTrips(): kotlin.Result<List<LocalTripDTO>>
    suspend fun saveTrip(trip: LocalTripDTO): kotlin.Result<Unit>
    suspend fun updateTrip(trip: LocalTripDTO): kotlin.Result<Unit>
    suspend fun deleteTrip(id: String): kotlin.Result<Unit>
}

class TripsLocalDataSourceImpl(
    private val persistence: TripsJsonPersistence
) : TripsLocalDataSource {
    override suspend fun getTrips(): kotlin.Result<List<LocalTripDTO>> =
        kotlin.runCatching { persistence.loadTrips() }

    override suspend fun saveTrip(trip: LocalTripDTO): kotlin.Result<Unit> =
        kotlin.runCatching { persistence.saveTrip(trip) }

    override suspend fun updateTrip(trip: LocalTripDTO): kotlin.Result<Unit> =
        kotlin.runCatching { persistence.updateTrip(trip) }

    override suspend fun deleteTrip(id: String): kotlin.Result<Unit> =
        kotlin.runCatching { persistence.deleteTrip(id) }
}

