package com.dlight.eric.taskmanager.data.mappers

import com.dlight.eric.taskmanager.data.remote.dto.LoginRequest
import com.dlight.eric.taskmanager.data.remote.dto.LoginResponse
import com.dlight.eric.taskmanager.domain.model.User

object AuthMapper {
    
    fun LoginResponse.toToken(): String {
        return token ?: ""
    }
    
    fun String.toLoginRequest(): LoginRequest {
        return LoginRequest(
            email = this
        )
    }
    
    fun String.toUser(): User {
        return User(
            email = this
        )
    }
}
