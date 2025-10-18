package com.dlight.eric.taskmanager.data.repository

import com.dlight.eric.taskmanager.data.datastore.AppDataStore
import com.dlight.eric.taskmanager.data.local.dao.TaskDao
import com.dlight.eric.taskmanager.data.mapper.TaskMapper.toDomain
import com.dlight.eric.taskmanager.data.mapper.TaskMapper.toEntity
import com.dlight.eric.taskmanager.domain.model.Task
import com.dlight.eric.taskmanager.domain.repository.TaskRepository
import com.dlight.eric.taskmanager.utils.DateUtils
import com.dlight.eric.taskmanager.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TaskDataRepository @Inject constructor(
    private val taskDao: TaskDao,
    private val appDataStore: AppDataStore
) : TaskRepository {

    override fun getAllTasks(): Flow<Resource<List<Task>>> {
        return taskDao.getAllTasks().map { entities ->
            try {
                val tasks = entities.map { it.toDomain() }
                Resource.Success(tasks)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Failed to load tasks")
            }
        }
    }

    override fun getTaskById(id: String): Flow<Resource<Task?>> {
        return taskDao.getTaskById(id).map { entity ->
            try {
                val task = entity?.toDomain()
                Resource.Success(task)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Failed to load task")
            }
        }
    }

    override suspend fun insertTask(task: Task): Resource<Unit> {
        return try {
            val entity = task.toEntity(
                timestampCreated = System.currentTimeMillis(),
                timestampUpdated = System.currentTimeMillis()
            )
            taskDao.insertTask(entity)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to insert task")
        }
    }

    override suspend fun updateTask(task: Task): Resource<Unit> {
        return try {
            val entity = task.toEntity(
                timestampUpdated = System.currentTimeMillis()
            )
            taskDao.updateTask(entity)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update task")
        }
    }

    override suspend fun updateTaskCompletion(id: String, completed: Boolean): Resource<Unit> {
        return try {
            val updatedAt = System.currentTimeMillis()
            taskDao.updateTaskCompletion(id, completed, updatedAt)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update task completion")
        }
    }

    override suspend fun deleteTask(task: Task): Resource<Unit> {
        return try {
            val entity = task.toEntity()
            taskDao.deleteTask(entity)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete task")
        }
    }

    override suspend fun deleteTaskById(id: String): Resource<Unit> {
        return try {
            taskDao.deleteTaskById(id)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete task")
        }
    }

    override fun getTaskCount(): Flow<Resource<Int>> {
        return taskDao.getTaskCount().map { count ->
            try {
                Resource.Success(count)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Failed to get task count")
            }
        }
    }

    override fun getUnSyncedTaskCount(lastSyncTimestamp: Long): Flow<Resource<Int>> {
        return taskDao.getUnSyncedTaskCount(lastSyncTimestamp).map { count ->
            try {
                Resource.Success(count)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Failed to get task count")
            }
        }
    }

    override fun getCompletedTaskCount(): Flow<Resource<Int>> {
        return taskDao.getCompletedTaskCount().map { count ->
            try {
                Resource.Success(count)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Failed to get completed task count")
            }
        }
    }

    override suspend fun deleteAllTasks(): Resource<Unit> {
        return try {
            taskDao.deleteAllTasks()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete all tasks")
        }
    }

    override fun getFormatedLastSyncTime(): Flow<String?> {
        return appDataStore.lastSyncTime.map { timestamp ->
            try {
                if (timestamp != null && timestamp > 0) {
                    DateUtils.formatTimestamp(timestamp)
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    override fun getLastSyncTimestamp(): Flow<Long?> = appDataStore.lastSyncTime
}
