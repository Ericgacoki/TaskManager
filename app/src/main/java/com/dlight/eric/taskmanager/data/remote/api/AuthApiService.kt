package com.dlight.eric.taskmanager.data.remote.api

import com.dlight.eric.taskmanager.data.remote.dto.LoginRequest
import com.dlight.eric.taskmanager.data.remote.dto.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse
}
