package com.dlight.eric.taskmanager.data.repository

import com.dlight.eric.taskmanager.data.datastore.AppDataStore
import com.dlight.eric.taskmanager.data.remote.api.AuthApiService
import com.dlight.eric.taskmanager.domain.model.User
import com.dlight.eric.taskmanager.utils.Resource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.check
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@RunWith(MockitoJUnitRunner::class)
class AuthDataRepositoryTest {

    @Mock
    private lateinit var authApiService: AuthApiService

    @Mock
    private lateinit var appDataStore: AppDataStore

    private lateinit var repository: AuthDataRepository

    private val testEmail = "eric@dlight.com"

    @Before
    fun setUp() {
        repository = AuthDataRepository(authApiService, appDataStore)
    }

    @Test
    fun `should return success with user when login is successful`() = runTest {
        val expectedUser = User(email = testEmail)

        val results = repository.login(testEmail, useApi = false).toList()

        val result = results.last()
        assertTrue(result is Resource.Success)
        assertEquals(expectedUser.email, result.data?.email)
    }

    @Test
    fun `should save auth token to datastore on successful login`() = runTest {
        repository.login(testEmail, useApi = false).toList()

        verify(appDataStore).saveAuthToken(check { token ->
            assertTrue(token.startsWith("mock-token-"))
        })
    }

    @Test
    fun `should generate unique tokens for different login attempts`() = runTest {
        repository.login(testEmail, useApi = false).toList().last()
        repository.login(testEmail, useApi = false).toList().last()

        // Verify saveAuthToken called twice and capture the tokens
        val captor = argumentCaptor<String>()
        verify(appDataStore, times(2)).saveAuthToken(captor.capture())

        val tokens = captor.allValues
        assertEquals(2, tokens.size)
        assertNotEquals(tokens[0], tokens[1], "Tokens should be unique between login attempts")
    }

    @Test
    fun `should return error when login throws exception`() = runTest {
        whenever(appDataStore.saveAuthToken(any())).thenThrow(RuntimeException("DataStore error"))

        val results = repository.login(testEmail, useApi = false).toList()

        val result = results.last()
        assertTrue(result is Resource.Error)
        assertEquals("DataStore error", result.message)
    }

    @Test
    fun `should clear auth data on logout`() = runTest {
        repository.logout()

        verify(appDataStore).clearAuthData()
    }

    @Test
    fun `should return success with token when token exists`() = runTest {
        val expectedToken = "mock-token-123"
        whenever(appDataStore.authToken).thenReturn(flowOf(expectedToken))

        val result = repository.getLoggedInUserToken().first()

        assertTrue(result is Resource.Success)
        assertEquals(expectedToken, result.data)
    }

    @Test
    fun `should return success with null when no token exists`() = runTest {
        whenever(appDataStore.authToken).thenReturn(flowOf(null))

        val result = repository.getLoggedInUserToken().first()

        assertTrue(result is Resource.Success)
        assertNull(result.data)
    }

    @Test
    fun `should return error when getting token throws exception`() = runTest {
        whenever(appDataStore.authToken).thenReturn(
            flow { throw RuntimeException("DataStore error") }
        )

        val result = repository.getLoggedInUserToken().first()

        assertTrue(result is Resource.Error)
        assertEquals("DataStore error", result.message)
    }

    @Test
    fun `should handle empty email during login`() = runTest {
        val results = repository.login("", useApi = false).toList()

        val result = results.last()
        assertTrue(result is Resource.Success)
        assertEquals("", result.data?.email)
    }

    @Test
    fun `should handle null token in login response gracefully`() = runTest {
        whenever(appDataStore.saveAuthToken(any())).thenThrow(RuntimeException("Null token"))

        val results = repository.login(testEmail, useApi = false).toList()

        val result = results.last()
        assertTrue(result is Resource.Error)
    }
}
