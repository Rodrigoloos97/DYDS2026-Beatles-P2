package edu.dyds.trips.presentation.home

import edu.dyds.trips.domain.entity.Country
import edu.dyds.trips.domain.entity.Result
import edu.dyds.trips.domain.repository.CountriesRepository
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
    private lateinit var getCountriesUseCase: GetCountriesUseCaseImpl
    private lateinit var searchCountriesUseCase: SearchCountriesUseCaseImpl

    private val sampleCountries = listOf(
        sampleCountry().copy(code = "AR", name = "Argentina"),
        sampleCountry().copy(code = "BR", name = "Brazil")
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        val fakeRepository = object : CountriesRepository {
            override suspend fun getCountries() = Result.Success(sampleCountries)
            override suspend fun searchCountries(query: String) = Result.Success(
                sampleCountries.filter { it.name.contains(query, ignoreCase = true) }
            )
            override suspend fun getCountryByCode(code: String) =
                sampleCountries.firstOrNull { it.code == code }
                    ?.let { Result.Success(it) }
                    ?: Result.Failure(NoSuchElementException("Country not found"))
        }
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
        viewModel.loadCountries()

        val uiState = viewModel.uiState.first { it !is HomeUiState.Loading && it !is HomeUiState.Idle }
        assertTrue("El estado deberia ser Success", uiState is HomeUiState.Success)
        assertEquals(sampleCountries, (uiState as HomeUiState.Success).countries)
    }

    @Test
    fun `searchCountries with valid query should emit Success with filtered countries`() = runTest {
        val query = "Brazil"

        viewModel.searchCountries(query)

        val uiState = viewModel.uiState.first { it !is HomeUiState.Loading && it !is HomeUiState.Idle }
        assertTrue("El estado deberia ser Success", uiState is HomeUiState.Success)
        val filteredCountries = (uiState as HomeUiState.Success).countries
        assertEquals(1, filteredCountries.size)
        assertEquals("BR", filteredCountries.first().code)
    }

    @Test
    fun `searchCountries with empty query should load all countries`() = runTest {
        val query = "   "

        viewModel.searchCountries(query)

        val uiState = viewModel.uiState.first { it !is HomeUiState.Loading && it !is HomeUiState.Idle }
        assertTrue("El estado deberia ser Success", uiState is HomeUiState.Success)
        assertEquals(sampleCountries.size, (uiState as HomeUiState.Success).countries.size)
    }

    @Test
    fun `loadCountries should emit Error when repository fails`() = runTest {
        val errorMessage = "Error al cargar"
        val failingRepository = object : CountriesRepository {
            override suspend fun getCountries(): Result<List<Country>> = Result.Failure(Exception(errorMessage))
            override suspend fun searchCountries(query: String): Result<List<Country>> = Result.Success(emptyList<Country>())
            override suspend fun getCountryByCode(code: String): Result<Country> = Result.Failure(Exception(errorMessage))
        }
        val failingGetUseCase = GetCountriesUseCaseImpl(failingRepository)
        val failingSearchUseCase = SearchCountriesUseCaseImpl(failingRepository)
        val failingViewModel = HomeViewModel(failingGetUseCase, failingSearchUseCase)

        failingViewModel.loadCountries()

        val uiState = failingViewModel.uiState.first { it !is HomeUiState.Loading && it !is HomeUiState.Idle }
        assertTrue("El estado deberia ser Error", uiState is HomeUiState.Error)
        assertEquals(errorMessage, (uiState as HomeUiState.Error).message)
    }
}
