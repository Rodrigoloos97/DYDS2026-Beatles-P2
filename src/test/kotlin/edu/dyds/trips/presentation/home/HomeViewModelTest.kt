package edu.dyds.trips.presentation.home

import edu.dyds.trips.domain.entity.Country
import edu.dyds.trips.domain.usecase.FakeCountriesRepository
import edu.dyds.trips.domain.usecase.GetCountriesUseCaseImpl
import edu.dyds.trips.domain.usecase.SearchCountriesUseCaseImpl
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
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: HomeViewModel
    private lateinit var fakeRepository: FakeCountriesRepository
    private lateinit var getCountriesUseCase: GetCountriesUseCaseImpl
    private lateinit var searchCountriesUseCase: SearchCountriesUseCaseImpl

    private val sampleCountries = listOf(
        sampleCountry().copy(code = "AR", name = "Argentina"),
        sampleCountry().copy(code = "BR", name = "Brazil")
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeCountriesRepository(sampleCountries)
        getCountriesUseCase = GetCountriesUseCaseImpl(fakeRepository)
        searchCountriesUseCase = SearchCountriesUseCaseImpl(fakeRepository)
        viewModel = HomeViewModel(getCountriesUseCase, searchCountriesUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        viewModel.dispose()
    }

    @Test
    fun `loadCountries should emit Success with countries`() = runTest {
        // When
        viewModel.loadCountries()

        // Then
        val uiState = viewModel.uiState.first()
        assertTrue("El estado deberia ser Success", uiState is HomeUiState.Success)
        assertEquals(sampleCountries, (uiState as HomeUiState.Success).countries)
    }

    @Test
    fun `searchCountries with valid query should emit Success with filtered countries`() = runTest {
        // Given
        val query = "Brazil"

        // When
        viewModel.searchCountries(query)

        // Then
        val uiState = viewModel.uiState.first()
        assertTrue("El estado deberia ser Success", uiState is HomeUiState.Success)
        val filteredCountries = (uiState as HomeUiState.Success).countries
        assertEquals(1, filteredCountries.size)
        assertEquals("BR", filteredCountries.first().code)
    }

    @Test
    fun `searchCountries with empty query should load all countries`() = runTest {
        // Given
        val query = "   "

        // When
        viewModel.searchCountries(query)

        // Then
        val uiState = viewModel.uiState.first()
        assertTrue("El estado deberia ser Success", uiState is HomeUiState.Success)
        assertEquals(sampleCountries.size, (uiState as HomeUiState.Success).countries.size)
    }

    @Test
    fun `loadCountries should emit Error when repository fails`() = runTest {
        // Given
        val errorMessage = "Error al cargar"
        val failingRepository = object : FakeCountriesRepository(emptyList()) {
            override suspend fun getCountries(): edu.dyds.trips.domain.entity.Result<List<Country>> {
                return edu.dyds.trips.domain.entity.Result.Failure(Exception(errorMessage))
            }
        }
        val failingGetUseCase = GetCountriesUseCaseImpl(failingRepository)
        val failingSearchUseCase = SearchCountriesUseCaseImpl(failingRepository)
        val failingViewModel = HomeViewModel(failingGetUseCase, failingSearchUseCase)

        // When
        failingViewModel.loadCountries()

        // Then
        val uiState = failingViewModel.uiState.first()
        assertTrue("El estado deberia ser Error", uiState is HomeUiState.Error)
        assertEquals(errorMessage, (uiState as HomeUiState.Error).message)
    }
}
