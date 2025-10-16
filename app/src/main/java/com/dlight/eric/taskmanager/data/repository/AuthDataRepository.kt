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

    override fun login(email: String): Flow<Resource<User>> = flow {
        try {
            emit(Resource.Loading())

            // Mock authentication with 5 second delay...
            delay(5000L)
            
            // Generate mock token
            val mockToken = "mock-token-${UUID.randomUUID()}"
            val response = LoginResponse(token = mockToken)
            
            // real API calls
            // val request = LoginRequest(email = email)
            // val response = authApiService.login(request)

            // Save auth data
            response.token?.let { dataStore.saveAuthToken(it) }
            dataStore.saveUserEmail(email)

            val user = User(email = email)
            emit(Resource.Success(user))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Login failed"))
        }
    }

    override suspend fun logout() {
        dataStore.clearAuthData()
    }

    override fun getLoggedInUserToken(): Flow<Resource<String?>> = flow {
        try {
            val token = dataStore.authToken.first()
            emit(Resource.Success(token))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get auth token"))
        }
    }
}
