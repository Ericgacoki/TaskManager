package com.dlight.eric.taskmanager.domain.repository

import com.dlight.eric.taskmanager.domain.model.Task
import com.dlight.eric.taskmanager.utils.Resource
import kotlinx.coroutines.flow.Flow

interface SyncRepository {

    suspend fun triggerSync()

    suspend fun syncTasks(): Flow<Resource<Unit>>

    fun getServerTasks(): Flow<Resource<List<Task>>>
    
    fun createServerTask(task: Task): Flow<Resource<Task>>
    
    fun updateServerTask(task: Task): Flow<Resource<Task>>
}
