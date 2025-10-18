package com.dlight.eric.taskmanager.domain.repository

import com.dlight.eric.taskmanager.domain.model.Task
import com.dlight.eric.taskmanager.utils.Resource
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    
    // Core CRUD operations
    fun getAllTasks(): Flow<Resource<List<Task>>>
    
    fun getTaskById(id: String): Flow<Resource<Task?>>
    
    suspend fun insertTask(task: Task): Resource<Unit>
    
    suspend fun updateTask(task: Task): Resource<Unit>
    
    suspend fun updateTaskCompletion(id: String, completed: Boolean): Resource<Unit>
    
    suspend fun deleteTask(task: Task): Resource<Unit>
    
    suspend fun deleteTaskById(id: String): Resource<Unit>
    
    // Statistics
    fun getTaskCount(): Flow<Resource<Int>>

    fun getUnSyncedTaskCount(lastSyncTimestamp: Long): Flow<Resource<Int>>
    
    fun getCompletedTaskCount(): Flow<Resource<Int>>
    
    // Utility operations
    suspend fun deleteAllTasks(): Resource<Unit>
    
    // Sync operations
    fun getFormatedLastSyncTime(): Flow<String?>

    fun getLastSyncTimestamp(): Flow<Long?>
}
