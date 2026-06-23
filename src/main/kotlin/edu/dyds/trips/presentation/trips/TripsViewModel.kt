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

    private val _operationState = MutableStateFlow<TripOperationUiState>(TripOperationUiState.Idle)
    @Suppress("unused")
    val operationState: StateFlow<TripOperationUiState> = _operationState.asStateFlow()

    fun loadTrips() {
        scope.launch {
            _uiState.value = TripsUiState.Loading
            _uiState.value = when (val result = getTripsUseCase()) {
                is Result.Success -> TripsUiState.Success(result.value.sortedBy { it.startDate })
                is Result.Failure -> TripsUiState.Error(result.exception.message ?: "No se pudieron cargar los viajes")
            }
        }
    }

    fun saveTrip(trip: Trip) {
        scope.launch {
            _operationState.value = TripOperationUiState.InFlight
            val result = saveTripUseCase(trip)
            _operationState.value = when (result) {
                is Result.Success -> {
                    loadTrips()
                    TripOperationUiState.Success("Viaje guardado correctamente")
                }
                is Result.Failure -> TripOperationUiState.Error(result.exception.message ?: "No se pudo guardar el viaje")
            }
        }
    }

    fun updateTrip(trip: Trip) {
        scope.launch {
            _operationState.value = TripOperationUiState.InFlight
            val result = updateTripUseCase(trip)
            _operationState.value = when (result) {
                is Result.Success -> {
                    loadTrips()
                    TripOperationUiState.Success("Viaje actualizado correctamente")
                }
                is Result.Failure -> TripOperationUiState.Error(result.exception.message ?: "No se pudo actualizar el viaje")
            }
        }
    }

    fun deleteTrip(id: String) {
        scope.launch {
            _operationState.value = TripOperationUiState.InFlight
            val result = deleteTripUseCase(id)
            _operationState.value = when (result) {
                is Result.Success -> {
                    loadTrips()
                    TripOperationUiState.Success("Viaje eliminado correctamente")
                }
                is Result.Failure -> TripOperationUiState.Error(result.exception.message ?: "No se pudo eliminar el viaje")
            }
        }
    }

    @Suppress("unused")
    fun clearOperationState() {
        _operationState.value = TripOperationUiState.Idle
    }

    fun dispose() {
        scope.cancel()
    }
}

