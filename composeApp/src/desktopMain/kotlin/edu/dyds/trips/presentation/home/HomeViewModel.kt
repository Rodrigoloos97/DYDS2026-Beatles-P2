package edu.dyds.trips.presentation.home

import edu.dyds.trips.domain.entity.Result
import edu.dyds.trips.domain.usecase.GetCountriesUseCase
import edu.dyds.trips.domain.usecase.SearchCountriesUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getCountriesUseCase: GetCountriesUseCase,
    private val searchCountriesUseCase: SearchCountriesUseCase
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Idle)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun loadCountries() {
        scope.launch {
            _uiState.value = HomeUiState.Loading
            _uiState.value = when (val result = getCountriesUseCase()) {
                is Result.Success -> HomeUiState.Success(result.value)
                is Result.Failure -> HomeUiState.Error(result.exception.message ?: "No se pudieron cargar los paises")
            }
        }
    }

    fun searchCountries(query: String) {
        val normalized = query.trim()
        if (normalized.isEmpty()) {
            loadCountries()
            return
        }

        scope.launch {
            _uiState.value = HomeUiState.Loading
            _uiState.value = when (val result = searchCountriesUseCase(normalized)) {
                is Result.Success -> HomeUiState.Success(result.value)
                is Result.Failure -> HomeUiState.Error(result.exception.message ?: "No se pudo buscar el pais")
            }
        }
    }

    fun dispose() {
        scope.cancel()
    }
}
