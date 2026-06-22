package edu.dyds.trips.presentation.detail

import edu.dyds.trips.data.broker.CountryWeatherBroker
import edu.dyds.trips.domain.entity.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetailViewModel(
    private val broker: CountryWeatherBroker
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Idle)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    fun loadCountryDetail(countryCode: String) {
        scope.launch {
            _uiState.value = DetailUiState.Loading
            _uiState.value = when (val result = broker.getCountryDetail(countryCode)) {
                is Result.Success -> DetailUiState.Success(result.value)
                is Result.Failure -> DetailUiState.Error(result.exception.message ?: "No se pudo cargar el detalle")
            }
        }
    }

    fun dispose() {
        scope.cancel()
    }
}

