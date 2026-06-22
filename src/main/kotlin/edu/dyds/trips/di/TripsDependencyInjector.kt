package edu.dyds.trips.di

import edu.dyds.trips.config.AppConfig
import edu.dyds.trips.config.AppConfigImpl
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

object TripsDependencyInjector {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val appConfig: AppConfig by lazy {
        AppConfigImpl.fromEnvironment()
    }

    private val httpClient: HttpClient by lazy {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }

    private val countriesRepository by lazy {
        CountriesRepositoryImpl(
            CountriesRemoteDataSourceImpl(RestCountriesClient(httpClient, appConfig))
        )
    }

    private val weatherRepository by lazy {
        WeatherRepositoryImpl(
            WeatherRemoteDataSourceImpl(OpenMeteoClient(httpClient, appConfig))
        )
    }

    private val tripsRepository by lazy {
        TripsRepositoryImpl(
            TripsLocalDataSourceImpl(
                TripsJsonPersistence(filePath = appConfig.tripsCacheFilePath)
            )
        )
    }

    private val getCountriesUseCase by lazy {
        GetCountriesUseCaseImpl(countriesRepository)
    }

    private val searchCountriesUseCase by lazy {
        SearchCountriesUseCaseImpl(countriesRepository)
    }

    private val getCountryDetailsUseCase by lazy {
        GetCountryDetailsUseCaseImpl(
            countriesRepository = countriesRepository,
            weatherRepository = weatherRepository
        )
    }

    private val getTripsUseCase by lazy {
        GetTripsUseCaseImpl(tripsRepository)
    }

    private val saveTripUseCase by lazy {
        SaveTripUseCaseImpl(tripsRepository)
    }

    private val updateTripUseCase by lazy {
        UpdateTripUseCaseImpl(tripsRepository)
    }

    private val deleteTripUseCase by lazy {
        DeleteTripUseCaseImpl(tripsRepository)
    }

    fun appScope(): CoroutineScope = appScope

    fun createHomeViewModel(): HomeViewModel = HomeViewModel(
        getCountriesUseCase = getCountriesUseCase,
        searchCountriesUseCase = searchCountriesUseCase
    )

    fun createDetailViewModel(): DetailViewModel = DetailViewModel(getCountryDetailsUseCase)

    fun createTripsViewModel(): TripsViewModel = TripsViewModel(
        getTripsUseCase = getTripsUseCase,
        saveTripUseCase = saveTripUseCase,
        updateTripUseCase = updateTripUseCase,
        deleteTripUseCase = deleteTripUseCase
    )

    fun shutdown() {
        appScope.cancel()
        httpClient.close()
    }
}

