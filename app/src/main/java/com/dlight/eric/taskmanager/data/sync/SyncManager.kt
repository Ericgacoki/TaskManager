package com.dlight.eric.taskmanager.data.sync

import android.content.Context
import com.dlight.eric.taskmanager.data.datastore.AppDataStore
import com.dlight.eric.taskmanager.data.mapper.TaskMapper.toDomain
import com.dlight.eric.taskmanager.data.mapper.TaskMapper.toDto
import com.dlight.eric.taskmanager.data.remote.api.TaskApiService
import com.dlight.eric.taskmanager.domain.model.Task
import com.dlight.eric.taskmanager.domain.repository.SyncRepository
import com.dlight.eric.taskmanager.domain.repository.TaskRepository
import com.dlight.eric.taskmanager.utils.DateUtils
import com.dlight.eric.taskmanager.utils.Resource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

sealed class ConflictResult {
    data class ServerWins(val task: Task) : ConflictResult()
    data class LocalWins(val task: Task) : ConflictResult()
}

@Singleton
class SyncManager @Inject constructor(
    private val taskRepository: TaskRepository,
    private val taskApiService: TaskApiService,
    private val appDataStore: AppDataStore,
    @ApplicationContext private val context: Context
) : SyncRepository {

    override suspend fun syncTasks(): Flow<Resource<Unit>> = flow {
        try {
            emit(Resource.Loading())

            // Capture sync start time
            val syncStartTime = System.currentTimeMillis()
            val lastSyncTime = taskRepository.getLastSyncTimestamp().first()

            // STEP 1: Get unsynced local tasks
            val localUnsynced = if (lastSyncTime != null) {
                val localUnsyncedResult = taskRepository.getUnSyncedTasks(lastSyncTime)
                    .first { it !is Resource.Loading }

                if (localUnsyncedResult !is Resource.Success) {
                    emit(Resource.Error("Failed to get unsynced local tasks"))
                    return@flow
                }
                localUnsyncedResult.data ?: emptyList()
            } else {
                // First local sync... get all tasks
                val allTasksResult = taskRepository.getAllTasks()
                    .first { it !is Resource.Loading }

                if (allTasksResult !is Resource.Success) {
                    emit(Resource.Error("Failed to get local tasks"))
                    return@flow
                }
                allTasksResult.data ?: emptyList()
            }

            // STEP 2: Get unsynced tasks from server
            val allServerTasksResult = getServerTasks()
                .first { it !is Resource.Loading }
            if (allServerTasksResult !is Resource.Success) {
                emit(Resource.Error(allServerTasksResult.message ?: "Failed to fetch server tasks"))
                return@flow
            }

            val allServerTasks = allServerTasksResult.data ?: emptyList()
            val serverUnsyncedTasks = if (lastSyncTime != null) {
                // Mock timestamp filtering since json-server doesn't custom queries
                allServerTasks.filter { serverTask ->
                    val serverUpdatedAt = DateUtils.parseIsoString(serverTask.updatedAt)
                    serverUpdatedAt > lastSyncTime
                }
            } else {
                allServerTasks // First remote sync
            }

            // STEP 3: Find server-only tasks
            val localTaskIds = localUnsynced.map { it.id }.toSet()
            val serverOnly = serverUnsyncedTasks.filter { it.id !in localTaskIds }

            // STEP 4: Find local-only tasks  
            val serverTaskIds = serverUnsyncedTasks.map { it.id }.toSet()
            val localOnly = localUnsynced.filter { it.id !in serverTaskIds }

            // STEP 5: Find conflicting tasks
            val conflicts = localUnsynced.filter { localTask ->
                serverUnsyncedTasks.any { serverTask -> serverTask.id == localTask.id }
            }

            // STEP 6: Resolve conflicts (latest updatedAt wins)
            // This low-key takes advantage of that we can not have two tasks with same id.
            val conflictResolutions = conflicts.map { localTask ->
                val serverTask = serverUnsyncedTasks.first { it.id == localTask.id }
                val serverUpdatedAt = DateUtils.parseIsoString(serverTask.updatedAt)
                val localUpdatedAt = DateUtils.parseIsoString(localTask.updatedAt)

                if (serverUpdatedAt > localUpdatedAt) {
                    ConflictResult.ServerWins(serverTask)
                } else {
                    ConflictResult.LocalWins(localTask)
                }
            }

            // STEP 7: Save to Room
            // Save server-only tasks (bulk insert)
            if (serverOnly.isNotEmpty()) {
                val insertResult = taskRepository.insertTasks(serverOnly)
                if (insertResult !is Resource.Success) {
                    emit(Resource.Error(insertResult.message ?: "Failed to save server tasks"))
                    return@flow
                }
            }

            // Save server winners from conflicts
            conflictResolutions.filterIsInstance<ConflictResult.ServerWins>().forEach { result ->
                val updateResult = taskRepository.updateTask(result.task)
                if (updateResult !is Resource.Success) {
                    emit(Resource.Error("Failed to update task ${result.task.id}"))
                    return@flow
                }
            }

            // STEP 8: Upload to server
            // Upload local-only tasks
            localOnly.forEach { localTask ->
                val createResult = createServerTask(localTask)
                    .first { it !is Resource.Loading }
                if (createResult !is Resource.Success) {
                    emit(
                        Resource.Error(
                            createResult.message ?: "Failed to create task ${localTask.id}"
                        )
                    )
                    return@flow
                }
            }

            // Upload local winners from conflicts
            conflictResolutions.filterIsInstance<ConflictResult.LocalWins>().forEach { result ->
                val updateResult = updateServerTask(result.task)
                    .first { it !is Resource.Loading }
                if (updateResult !is Resource.Success) {
                    emit(
                        Resource.Error(
                            updateResult.message ?: "Failed to update task ${result.task.id}"
                        )
                    )
                    return@flow
                }
            }

            // STEP 9: Update sync time
            appDataStore.saveLastSyncTime(syncStartTime)

            emit(Resource.Success(Unit))

        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Sync failed"))
        }
    }

    override suspend fun triggerSync() {
        SyncWorker.enqueueImmediateSync(context)
    }

    override fun getServerTasks(): Flow<Resource<List<Task>>> = flow {
        try {
            val serverTasks = taskApiService.getTasks()
            val domainTasks = serverTasks.map { it.toDomain() }
            emit(Resource.Success(domainTasks))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to fetch server tasks"))
        }
    }

    override fun createServerTask(task: Task): Flow<Resource<Task>> = flow {
        try {
            val taskDto = task.toDto()
            val createdTaskDto = taskApiService.createTask(taskDto)
            val createdTask = createdTaskDto.toDomain()
            emit(Resource.Success(createdTask))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to create server task"))
        }
    }

    override fun updateServerTask(task: Task): Flow<Resource<Task>> = flow {
        try {
            val taskDto = task.toDto()
            val updatedTaskDto = taskApiService.updateTask(task.id, taskDto)
            val updatedTask = updatedTaskDto.toDomain()
            emit(Resource.Success(updatedTask))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to update server task"))
        }
    }
}
