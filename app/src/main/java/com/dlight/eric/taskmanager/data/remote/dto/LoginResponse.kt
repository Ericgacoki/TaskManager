package com.dlight.eric.taskmanager.data.remote.dto

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    @SerializedName("email")
    val email: String
)

@Serializable
data class LoginResponse(
    @SerializedName("token")
    val token: String?
)
