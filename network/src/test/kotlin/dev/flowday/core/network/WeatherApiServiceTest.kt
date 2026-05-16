package dev.flowday.core.network

import dev.flowday.core.network.api.WeatherApiService
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.fail
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory


class WeatherApiServiceTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var weatherApiService: WeatherApiService

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        weatherApiService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(WeatherApiService::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `returns correct data in a 200 status code`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """
            {
                "latitude": 41.14,
                "longitude": -8.61,
                "current": {
                    "temperature_2m": 18.5,
                    "weathercode": 0
                }
            }
        """.trimIndent()
                )
        )

        val result = weatherApiService.getWeather(41.14, -8.61, "temperature_2m,weathercode")

        assertEquals(18.5, result.current.temperature)

        val request = mockWebServer.takeRequest()
        assertEquals("GET", request.method)
        assert(request.path!!.contains("latitude=41.14"))
    }

    @Test
    fun `returns error on 500 response`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(500))

        try {
            weatherApiService.getWeather(41.14, -8.61, "temperature_2m,weathercode")
            fail("Expected exception was not thrown")
        } catch (e: Exception) {
            // test passes
        }
    }

    @Test
    fun `returns UNKNOWN condition on malformed JSON`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("{ invalid json }")
        )

        try {
            weatherApiService.getWeather(41.14, -8.61, "temperature_2m,weathercode")
            fail("Expected exception was not thrown")
        } catch (e: Exception) {
            // test passes
        }
    }

}