package edu.dyds.trips.presentation.trips

sealed interface TripOperationUiState {
    data object Idle : TripOperationUiState
    data object InFlight : TripOperationUiState
    data class Success(val message: String) : TripOperationUiState
    data class Error(val message: String) : TripOperationUiState
}

