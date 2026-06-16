package edu.dyds.trips.presentation.home

import edu.dyds.trips.domain.entity.Country

sealed interface HomeUiState {
    data object Idle : HomeUiState
    data object Loading : HomeUiState
    data class Success(val countries: List<Country>) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

