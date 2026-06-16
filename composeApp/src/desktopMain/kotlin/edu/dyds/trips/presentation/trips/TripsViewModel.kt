package edu.dyds.trips.presentation.trips

import edu.dyds.trips.domain.entity.Result
import edu.dyds.trips.domain.entity.Trip
import edu.dyds.trips.domain.usecase.DeleteTripUseCase
import edu.dyds.trips.domain.usecase.GetTripsUseCase
import edu.dyds.trips.domain.usecase.SaveTripUseCase
import edu.dyds.trips.domain.usecase.UpdateTripUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TripsViewModel(
    private val getTripsUseCase: GetTripsUseCase,
    private val saveTripUseCase: SaveTripUseCase,
    private val updateTripUseCase: UpdateTripUseCase,
    private val deleteTripUseCase: DeleteTripUseCase
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _uiState = MutableStateFlow<TripsUiState>(TripsUiState.Loading)
    val uiState: StateFlow<TripsUiState> = _uiState.asStateFlow()

    fun loadTrips() {
        scope.launch {
            _uiState.value = TripsUiState.Loading
            _uiState.value = when (val result = getTripsUseCase()) {
                is Result.Success -> TripsUiState.Success(result.value.sortedBy { it.startDate })
                is Result.Failure -> TripsUiState.Error(result.exception.message ?: "No se pudieron cargar los viajes")
            }
        }
    }

    fun saveTrip(trip: Trip, onDone: (Result<Unit>) -> Unit = {}) {
        scope.launch {
            val result = saveTripUseCase(trip)
            onDone(result)
            if (result is Result.Success) loadTrips()
        }
    }

    fun updateTrip(trip: Trip, onDone: (Result<Unit>) -> Unit = {}) {
        scope.launch {
            val result = updateTripUseCase(trip)
            onDone(result)
            if (result is Result.Success) loadTrips()
        }
    }

    fun deleteTrip(id: String, onDone: (Result<Unit>) -> Unit = {}) {
        scope.launch {
            val result = deleteTripUseCase(id)
            onDone(result)
            if (result is Result.Success) loadTrips()
        }
    }

    fun dispose() {
        scope.cancel()
    }
}

