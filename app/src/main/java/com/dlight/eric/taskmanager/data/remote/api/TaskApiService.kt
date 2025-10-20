package com.dlight.eric.taskmanager.data.remote.api

import com.dlight.eric.taskmanager.data.remote.dto.TaskDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface TaskApiService {
    
    @GET("tasks")
    suspend fun getTasks(): List<TaskDto>
    
    @POST("tasks")
    suspend fun createTask(
        @Body task: TaskDto
    ): TaskDto
    
    @PUT("tasks/{id}")
    suspend fun updateTask(
        @Path("id") id: String,
        @Body task: TaskDto
    ): TaskDto
}
