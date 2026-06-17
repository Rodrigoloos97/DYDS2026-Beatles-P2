package edu.dyds.trips.presentation.detail

import edu.dyds.trips.domain.entity.CountryDetail

sealed interface DetailUiState {
    data object Idle : DetailUiState
    data object Loading : DetailUiState
    data class Success(val detail: CountryDetail) : DetailUiState
    data class Error(val message: String) : DetailUiState
}

