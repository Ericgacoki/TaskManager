package com.dlight.eric.taskmanager.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("email")
    val email: String
)

data class LoginResponse(
    @SerializedName("token")
    val token: String?
)
