package edu.dyds.trips.presentation.trips

import edu.dyds.trips.domain.entity.Trip

sealed interface TripsUiState {
    data object Loading : TripsUiState
    data class Success(val trips: List<Trip>) : TripsUiState
    data class Error(val message: String) : TripsUiState
}

