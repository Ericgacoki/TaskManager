package com.dlight.eric.taskmanager.data.sync

import android.content.Context
import android.util.Log
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
import kotlinx.coroutines.flow.catch
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

    override suspend fun syncTasks() = flow<Resource<Unit>> {
        emit(Resource.Loading())

        runCatching {
            // Capture sync start time
            val syncStartTime = System.currentTimeMillis()
            val lastSyncTime = taskRepository.getLastSyncTimestamp().first()

            // STEP 1: Get unsynced local tasks
            val localUnsynced = if (lastSyncTime != null) {
                val localUnsyncedResult = taskRepository.getUnSyncedTasks(lastSyncTime)
                    .first { it !is Resource.Loading }

                if (localUnsyncedResult !is Resource.Success) {
                    val error = "Failed to get unsynced local tasks: ${localUnsyncedResult.message}"
                    emit(Resource.Error(error))
                    return@flow
                }
                val tasks = localUnsyncedResult.data ?: emptyList()
                tasks
            } else {
                // First local sync... get all tasks
                val allTasksResult = taskRepository.getAllTasks()
                    .first { it !is Resource.Loading }

                if (allTasksResult !is Resource.Success) {
                    val error = "Failed to get local tasks: ${allTasksResult.message}"
                    emit(Resource.Error(error))
                    return@flow
                }
                val tasks = allTasksResult.data ?: emptyList()
                tasks
            }

            // STEP 2: Get unsynced tasks from server
            val allServerTasksResult = getServerTasks()
                .first { it !is Resource.Loading }
            if (allServerTasksResult !is Resource.Success) {
                val error = allServerTasksResult.message ?: "Failed to fetch server tasks"
                emit(Resource.Error(error))
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

            // STEP 4: Find local-only tasks (not on server at all)
            val allServerTaskIds = allServerTasks.map { it.id }.toSet()
            val localOnly = localUnsynced.filter { it.id !in allServerTaskIds }

            // STEP 5: Find conflicting tasks (exist on both local and server)
            val conflicts = localUnsynced.filter { localTask ->
                allServerTasks.any { serverTask -> serverTask.id == localTask.id }
            }

            // STEP 6: Resolve conflicts (latest updatedAt wins)
            val conflictResolutions = conflicts.map { localTask ->
                val serverTask = allServerTasks.first { it.id == localTask.id }
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
                    val error = "Failed to save server tasks: ${insertResult.message}"
                    emit(Resource.Error(error))
                    return@flow
                }
            }

            // Save server winners from conflicts
            val serverWinners = conflictResolutions.filterIsInstance<ConflictResult.ServerWins>()
            serverWinners.forEach { result ->
                val updateResult = taskRepository.updateTask(result.task)
                if (updateResult !is Resource.Success) {
                    val error = "Failed to update task ${result.task.id}: ${updateResult.message}"
                    emit(Resource.Error(error))
                    return@flow
                }
            }

            // STEP 8: Upload to server
            // Upload local-only tasks
            localOnly.forEach { localTask ->
                val createResult = createServerTask(localTask)
                    .first { it !is Resource.Loading }
                if (createResult !is Resource.Success) {
                    val error = createResult.message ?: "Failed to create task ${localTask.id}"
                    emit(Resource.Error(error))
                    return@flow
                }
            }

            // Upload local winners from conflicts
            val localWinners = conflictResolutions.filterIsInstance<ConflictResult.LocalWins>()
            localWinners.forEach { result ->
                val updateResult = updateServerTask(result.task)
                    .first { it !is Resource.Loading }
                if (updateResult !is Resource.Success) {
                    val error = updateResult.message ?: "Failed to update task ${result.task.id}"
                    emit(Resource.Error(error))
                    return@flow
                }
            }

            // STEP 9: Update sync time
            appDataStore.saveLastSyncTime(syncStartTime)

        }.onSuccess {
            emit(Resource.Success(Unit))
        }.onFailure { e ->
            val errorMsg = "Sync failed: ${e.message}"
            emit(Resource.Error(errorMsg))
        }
    }

    override suspend fun triggerSync() {
        SyncWorker.enqueueImmediateSync(context)
    }

    override fun getServerTasks() = flow<Resource<List<Task>>> {
        val serverTasks = taskApiService.getTasks()
        val domainTasks = serverTasks.map { it.toDomain() }
        emit(Resource.Success(domainTasks))
    }.catch { e ->
        val error = "Failed to fetch server tasks: ${e.message}"
        emit(Resource.Error<List<Task>>(error))
    }

    override fun createServerTask(task: Task) = flow<Resource<Task>> {
        val taskDto = task.toDto()
        val createdTaskDto = taskApiService.createTask(taskDto)
        val createdTask = createdTaskDto.toDomain()
        emit(Resource.Success(createdTask))
    }.catch { e ->
        val error = "Failed to create server task: ${e.message}"
        emit(Resource.Error<Task>(error))
    }

    override fun updateServerTask(task: Task) = flow<Resource<Task>> {
        val taskDto = task.toDto()
        val updatedTaskDto = taskApiService.updateTask(task.id, taskDto)
        val updatedTask = updatedTaskDto.toDomain()
        emit(Resource.Success(updatedTask))
    }.catch { e ->
        val error = "Failed to update server task: ${e.message}"
        emit(Resource.Error<Task>(error))
    }
}
