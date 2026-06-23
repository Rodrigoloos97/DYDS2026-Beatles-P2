package edu.dyds.trips.data.broker

import edu.dyds.trips.domain.entity.Result
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertSame

class GetCountryDetailUnexpectedExceptionTest {

    @Test
    fun `getCountryDetail returns Failure when countries repository throws unexpected exception`() = runTest {
        val unexpectedException = RuntimeException("Unexpected crash in repository")
        val broker = CountryWeatherBroker(
            countriesRepository = FakeCountriesRepository(
                throwOnCall = unexpectedException
            ),
            weatherRepository = FakeWeatherRepository()
        )

        val result = broker.getCountryDetail("AR")

        assertIs<Result.Failure>(result)
        assertSame(unexpectedException, (result as Result.Failure).exception)
    }
}


