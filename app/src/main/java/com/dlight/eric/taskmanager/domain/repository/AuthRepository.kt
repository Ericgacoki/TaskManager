package com.dlight.eric.taskmanager.domain.repository

import com.dlight.eric.taskmanager.domain.model.User
import com.dlight.eric.taskmanager.utils.Resource
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    fun login(email: String): Flow<Resource<User>>

    suspend fun logout()

    fun getLoggedInUserToken(): Flow<Resource<String?>>
}
