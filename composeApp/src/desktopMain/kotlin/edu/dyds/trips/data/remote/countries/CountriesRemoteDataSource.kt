package edu.dyds.trips.data.remote.countries

interface CountriesRemoteDataSource {
    suspend fun fetchCountries(): kotlin.Result<List<RemoteCountryDTO>>
    suspend fun searchCountries(query: String): kotlin.Result<List<RemoteCountryDTO>>
}

class CountriesRemoteDataSourceImpl(
    private val client: RestCountriesClient
) : CountriesRemoteDataSource {
    override suspend fun fetchCountries(): kotlin.Result<List<RemoteCountryDTO>> =
        kotlin.runCatching { client.getCountries() }

    override suspend fun searchCountries(query: String): kotlin.Result<List<RemoteCountryDTO>> =
        kotlin.runCatching { client.searchCountries(query) }
}

