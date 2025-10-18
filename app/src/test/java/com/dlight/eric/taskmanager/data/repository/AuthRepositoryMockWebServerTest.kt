package com.dlight.eric.taskmanager.data.repository

import com.dlight.eric.taskmanager.data.datastore.AppDataStore
import com.dlight.eric.taskmanager.data.remote.api.AuthApiService
import com.dlight.eric.taskmanager.utils.Resource
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@RunWith(MockitoJUnitRunner::class)
class AuthRepositoryMockWebServerTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var authApiService: AuthApiService

    @Mock
    private lateinit var appDataStore: AppDataStore

    private lateinit var repository: AuthDataRepository

    private val testEmail = "eric@dlight.com"

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        authApiService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApiService::class.java)

        repository = AuthDataRepository(authApiService, appDataStore)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `should handle successful API login response`() = runTest {
        val mockResponseBody = """{"token": "api-token-123"}"""

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(mockResponseBody)
                .addHeader("Content-Type", "application/json")
        )

        val result = repository.login(testEmail, useApi = true).drop(1).first()

        assertTrue(result is Resource.Success)
        assertEquals(testEmail, result.data?.email)

        val request = mockWebServer.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/auth/login", request.path)

        verify(appDataStore).saveAuthToken("api-token-123")
    }

    @Test
    fun `should handle API login failure with 401 response`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(401)
                .setBody("""{"error": "Invalid credentials"}""")
                .addHeader("Content-Type", "application/json")
        )

        val result = repository.login(testEmail, useApi = true).drop(1).first()

        assertTrue(result is Resource.Error)
    }

    @Test
    fun `should handle API network error during login`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error")
        )

        val result = repository.login(testEmail, useApi = true).drop(1).first()

        assertTrue(result is Resource.Error)
    }

    @Test
    fun `should handle malformed JSON response from API`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("invalid json{")
                .addHeader("Content-Type", "application/json")
        )

        val result = repository.login(testEmail, useApi = true).drop(1).first()

        assertTrue(result is Resource.Error)
    }

    @Test
    fun `should use datastore token for authenticated calls`() = runTest {
        val mockToken = "stored-token-456"
        whenever(appDataStore.authToken).thenReturn(flowOf(mockToken))

        val result = repository.getLoggedInUserToken().first()

        assertTrue(result is Resource.Success)
        assertEquals(mockToken, result.data)
    }
}
