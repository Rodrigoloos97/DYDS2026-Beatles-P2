package edu.dyds.trips.data.local

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class TripsJsonPersistence(
    private val filePath: String = "trips_data.json",
    private val json: Json = Json { prettyPrint = true; ignoreUnknownKeys = true }
) {
    suspend fun loadTrips(): List<LocalTripDTO> = withContext(Dispatchers.IO) {
        val file = ensureFile()
        if (file.readText().isBlank()) {
            emptyList()
        } else {
            json.decodeFromString<AppDataFile>(file.readText()).trips
        }
    }

    suspend fun saveTrip(trip: LocalTripDTO) = withContext(Dispatchers.IO) {
        val current = loadAppData()
        val updatedTrips = current.trips.filterNot { it.id == trip.id } + trip
        persist(current.copy(trips = updatedTrips))
    }

    suspend fun updateTrip(trip: LocalTripDTO) = withContext(Dispatchers.IO) {
        val current = loadAppData()
        val updatedTrips = current.trips.map { existing ->
            if (existing.id == trip.id) trip else existing
        }
        persist(current.copy(trips = updatedTrips))
    }

    suspend fun deleteTrip(id: String) = withContext(Dispatchers.IO) {
        val current = loadAppData()
        persist(current.copy(trips = current.trips.filterNot { it.id == id }))
    }

    private fun loadAppData(): AppDataFile {
        val file = ensureFile()
        val text = file.readText()
        return if (text.isBlank()) AppDataFile() else json.decodeFromString(text)
    }

    private fun persist(data: AppDataFile) {
        val file = ensureFile()
        file.writeText(json.encodeToString(data))
    }

    private fun ensureFile(): File {
        val file = File(filePath)
        if (!file.exists()) {
            file.parentFile?.mkdirs()
            file.writeText(json.encodeToString(AppDataFile()))
        }
        return file
    }
}


