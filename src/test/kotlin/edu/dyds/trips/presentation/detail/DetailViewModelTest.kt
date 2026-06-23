package edu.dyds.trips.presentation.detail

import edu.dyds.trips.domain.usecase.FakeCountriesRepository
import edu.dyds.trips.domain.usecase.FakeWeatherRepository
import edu.dyds.trips.domain.usecase.GetCountryDetailsUseCaseImpl
import edu.dyds.trips.domain.usecase.sampleCountry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class DetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: DetailViewModel
    private lateinit var getCountryDetailsUseCase: GetCountryDetailsUseCaseImpl
    private lateinit var fakeCountriesRepository: FakeCountriesRepository
    private lateinit var fakeWeatherRepository: FakeWeatherRepository

    private val sampleCountry = sampleCountry()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeCountriesRepository = FakeCountriesRepository(listOf(sampleCountry))
        fakeWeatherRepository = FakeWeatherRepository()
        getCountryDetailsUseCase = GetCountryDetailsUseCaseImpl(fakeCountriesRepository, fakeWeatherRepository)
        viewModel = DetailViewModel(getCountryDetailsUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        viewModel.dispose()
    }

    @Test
    fun `loadCountryDetail with valid code should emit Success`() = runTest {
        val countryCode = sampleCountry.code

        viewModel.loadCountryDetail(countryCode)

        val uiState = viewModel.uiState.first { it !is DetailUiState.Loading && it !is DetailUiState.Idle }
        assertTrue("El estado deberia ser Success", uiState is DetailUiState.Success)
        val detail = (uiState as DetailUiState.Success).detail
        assertEquals(sampleCountry.code, detail.country.code)
        assertTrue("Deberia haber un pronostico del tiempo", detail.weatherForecast.isNotEmpty())
    }

    @Test
    fun `loadCountryDetail with invalid code should emit Error`() = runTest {
        val invalidCode = "XX"

        viewModel.loadCountryDetail(invalidCode)

        val uiState = viewModel.uiState.first { it !is DetailUiState.Loading && it !is DetailUiState.Idle }
        assertTrue("El estado deberia ser Error", uiState is DetailUiState.Error)
        val errorMessage = (uiState as DetailUiState.Error).message
        assertTrue("El mensaje de error deberia indicar que no se encontro", errorMessage.contains("not found", ignoreCase = true))
    }
}
