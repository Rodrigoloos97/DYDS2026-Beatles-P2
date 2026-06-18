package edu.dyds.trips.presentation.trips

import edu.dyds.trips.domain.entity.Trip
import edu.dyds.trips.domain.usecase.DeleteTripUseCaseImpl
import edu.dyds.trips.domain.usecase.FakeTripsRepository
import edu.dyds.trips.domain.usecase.GetTripsUseCaseImpl
import edu.dyds.trips.domain.usecase.SaveTripUseCaseImpl
import edu.dyds.trips.domain.usecase.UpdateTripUseCaseImpl
import edu.dyds.trips.domain.usecase.sampleTrip
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
class TripsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: TripsViewModel
    private lateinit var fakeRepository: FakeTripsRepository

    private val initialTrip = sampleTrip()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeTripsRepository(mutableListOf(initialTrip))
        viewModel = TripsViewModel(
            getTripsUseCase = GetTripsUseCaseImpl(fakeRepository),
            saveTripUseCase = SaveTripUseCaseImpl(fakeRepository),
            updateTripUseCase = UpdateTripUseCaseImpl(fakeRepository),
            deleteTripUseCase = DeleteTripUseCaseImpl(fakeRepository)
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        viewModel.dispose()
    }

    @Test
    fun `loadTrips should emit Success with a list of trips`() = runTest {
        // When
        viewModel.loadTrips()

        // Then
        val uiState = viewModel.uiState.first { it !is TripsUiState.Loading }
        assertTrue("El estado deberia ser Success", uiState is TripsUiState.Success)
        val trips = (uiState as TripsUiState.Success).trips
        assertEquals(1, trips.size)
        assertEquals(initialTrip.id, trips.first().id)
    }

    @Test
    fun `saveTrip should add a new trip and emit Success operation state`() = runTest {
        // Given
        val newTrip = Trip(id = "trip-2", countryCode = "BR", countryName = "Brazil", startDate = "2026-08-01", endDate = "2026-08-10")

        // When
        viewModel.saveTrip(newTrip)

        // Then
        val operationState = viewModel.operationState.first { it is TripOperationUiState.Success || it is TripOperationUiState.Error }
        assertTrue("El estado de la operacion deberia ser Success", operationState is TripOperationUiState.Success)

        val uiState = viewModel.uiState.first { it !is TripsUiState.Loading }
        val trips = (uiState as TripsUiState.Success).trips
        assertEquals("Deberia haber 2 viajes ahora", 2, trips.size)
        assertTrue("El nuevo viaje deberia estar en la lista", trips.any { it.id == "trip-2" })
    }

    @Test
    fun `deleteTrip should remove a trip and emit Success operation state`() = runTest {
        // Given
        val tripIdToDelete = initialTrip.id

        // When
        viewModel.deleteTrip(tripIdToDelete)

        // Then
        val operationState = viewModel.operationState.first { it is TripOperationUiState.Success || it is TripOperationUiState.Error }
        assertTrue("El estado de la operacion deberia ser Success", operationState is TripOperationUiState.Success)

        val uiState = viewModel.uiState.first { it !is TripsUiState.Loading }
        val trips = (uiState as TripsUiState.Success).trips
        assertEquals("La lista de viajes deberia estar vacia", 0, trips.size)
    }

    @Test
    fun `updateTrip should modify an existing trip`() = runTest {
        // Given
        val updatedTrip = initialTrip.copy(notes = "Notas actualizadas")

        // When
        viewModel.updateTrip(updatedTrip)

        // Then
        val operationState = viewModel.operationState.first { it is TripOperationUiState.Success || it is TripOperationUiState.Error }
        assertTrue("El estado de la operacion deberia ser Success", operationState is TripOperationUiState.Success)

        val uiState = viewModel.uiState.first { it !is TripsUiState.Loading }
        val trips = (uiState as TripsUiState.Success).trips
        assertEquals(1, trips.size)
        assertEquals("Notas actualizadas", trips.first().notes)
    }
}
