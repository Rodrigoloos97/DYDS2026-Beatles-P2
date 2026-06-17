package edu.dyds.trips

import edu.dyds.trips.data.local.TripsJsonPersistence
import edu.dyds.trips.data.local.TripsLocalDataSourceImpl
import edu.dyds.trips.data.remote.countries.CountriesRemoteDataSourceImpl
import edu.dyds.trips.data.remote.countries.RestCountriesClient
import edu.dyds.trips.data.remote.weather.OpenMeteoClient
import edu.dyds.trips.data.remote.weather.WeatherRemoteDataSourceImpl
import edu.dyds.trips.data.repository.CountriesRepositoryImpl
import edu.dyds.trips.data.repository.TripsRepositoryImpl
import edu.dyds.trips.data.repository.WeatherRepositoryImpl
import edu.dyds.trips.domain.usecase.DeleteTripUseCaseImpl
import edu.dyds.trips.domain.usecase.GetCountriesUseCaseImpl
import edu.dyds.trips.domain.usecase.GetCountryDetailsUseCaseImpl
import edu.dyds.trips.domain.usecase.GetTripsUseCaseImpl
import edu.dyds.trips.domain.usecase.SaveTripUseCaseImpl
import edu.dyds.trips.domain.usecase.SearchCountriesUseCaseImpl
import edu.dyds.trips.domain.usecase.UpdateTripUseCaseImpl
import edu.dyds.trips.presentation.detail.DetailViewModel
import edu.dyds.trips.presentation.home.HomeViewModel
import edu.dyds.trips.presentation.navigation.createAndShowAppWindow
import edu.dyds.trips.presentation.trips.TripsViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.serialization.json.Json
import javax.swing.SwingUtilities

fun main() {
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    val countriesRepository = CountriesRepositoryImpl(
        CountriesRemoteDataSourceImpl(RestCountriesClient(httpClient))
    )

    val weatherRepository = WeatherRepositoryImpl(
        WeatherRemoteDataSourceImpl(OpenMeteoClient(httpClient))
    )

    val tripsRepository = TripsRepositoryImpl(
        TripsLocalDataSourceImpl(
            TripsJsonPersistence(filePath = "app_data/trips_data.json")
        )
    )

    val homeViewModel = HomeViewModel(
        getCountriesUseCase = GetCountriesUseCaseImpl(countriesRepository),
        searchCountriesUseCase = SearchCountriesUseCaseImpl(countriesRepository)
    )

    val tripsViewModel = TripsViewModel(
        getTripsUseCase = GetTripsUseCaseImpl(tripsRepository),
        saveTripUseCase = SaveTripUseCaseImpl(tripsRepository),
        updateTripUseCase = UpdateTripUseCaseImpl(tripsRepository),
        deleteTripUseCase = DeleteTripUseCaseImpl(tripsRepository)
    )

    val getCountryDetailsUseCase = GetCountryDetailsUseCaseImpl(
        countriesRepository = countriesRepository,
        weatherRepository = weatherRepository
    )

    SwingUtilities.invokeLater {
        createAndShowAppWindow(
            scope = appScope,
            homeViewModel = homeViewModel,
            tripsViewModel = tripsViewModel,
            createDetailViewModel = { DetailViewModel(getCountryDetailsUseCase) },
            onClose = {
                appScope.cancel()
                httpClient.close()
            }
        )
    }
}

