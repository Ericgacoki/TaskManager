package com.dlight.eric.taskmanager.data.repository

import com.dlight.eric.taskmanager.data.datastore.AppDataStore
import com.dlight.eric.taskmanager.data.remote.api.AuthApiService
import com.dlight.eric.taskmanager.data.remote.dto.LoginRequest
import com.dlight.eric.taskmanager.data.remote.dto.LoginResponse
import com.dlight.eric.taskmanager.domain.model.User
import com.dlight.eric.taskmanager.domain.repository.AuthRepository
import com.dlight.eric.taskmanager.utils.Resource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import java.util.UUID
import javax.inject.Inject

class AuthDataRepository @Inject constructor(
    private val authApiService: AuthApiService,
    private val dataStore: AppDataStore
) : AuthRepository {

    private suspend fun <T> authenticatedCall(call: suspend (String?) -> T): T {
        val token = dataStore.authToken.first()
        return call(token)
    }

    override fun login(email: String, useApi: Boolean): Flow<Resource<User>> = flow {
        emit(Resource.Loading())

        val response = if (useApi) {
            // Real API calls used in tests
            val request = LoginRequest(email = email)
            authApiService.login(request)
        } else {
            // Mock auth with delay for regular usage...
            delay(5000L)
            val mockToken = "mock-token-${UUID.randomUUID()}"
            LoginResponse(token = mockToken)
        }

        // Save auth data
        response.token?.let { dataStore.saveAuthToken(it) }

        val user = User(email = email)
        emit(Resource.Success(user))
    }.catch { e ->
        emit(Resource.Error(e.message ?: "Login failed"))
    }

    override suspend fun logout() {
        dataStore.clearAuthData()
    }

    override fun getLoggedInUserToken() = flow<Resource<String?>> {
        val token = dataStore.authToken.first()
        emit(Resource.Success(token))
    }.catch { e ->
        emit(Resource.Error(e.message ?: "Failed to get auth token"))
    }
}
