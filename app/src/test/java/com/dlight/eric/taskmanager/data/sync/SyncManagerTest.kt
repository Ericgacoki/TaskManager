package com.dlight.eric.taskmanager.data.sync

import android.content.Context
import com.dlight.eric.taskmanager.data.datastore.AppDataStore
import com.dlight.eric.taskmanager.data.remote.api.TaskApiService
import com.dlight.eric.taskmanager.data.remote.dto.TaskDto
import com.dlight.eric.taskmanager.domain.model.Task
import com.dlight.eric.taskmanager.domain.repository.TaskRepository
import com.dlight.eric.taskmanager.utils.Resource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@RunWith(MockitoJUnitRunner::class)
class SyncManagerTest {

    @Mock
    private lateinit var taskRepository: TaskRepository

    @Mock
    private lateinit var taskApiService: TaskApiService

    @Mock
    private lateinit var appDataStore: AppDataStore

    @Mock
    private lateinit var context: Context

    private lateinit var syncManager: SyncManager

    private val mockTask = Task(
        id = "task1",
        title = "Test Task",
        description = "Test Description",
        completed = false,
        dueDate = "2024-01-01T00:00:00Z",
        createdAt = "2024-01-01T00:00:00Z",
        updatedAt = "2024-01-01T00:00:00Z"
    )

    private val mockTaskDto = TaskDto(
        id = "task1",
        title = "Test Task",
        description = "Test Description",
        completed = false,
        dueDate = "2024-01-01T00:00:00Z",
        createdAt = "2024-01-01T00:00:00Z",
        updatedAt = "2024-01-01T00:00:00Z"
    )

    @Before
    fun setUp() {
        syncManager = SyncManager(taskRepository, taskApiService, appDataStore, context)
    }

    @Test
    fun `syncTasks should succeed when no local or server tasks exist`() = runTest {
        whenever(taskRepository.getLastSyncTimestamp()).thenReturn(flowOf(null))
        whenever(taskRepository.getAllTasks()).thenReturn(flowOf(Resource.Success(emptyList())))
        whenever(taskApiService.getTasks()).thenReturn(emptyList())

        val result = syncManager.syncTasks().first { it !is Resource.Loading }

        assertTrue(result is Resource.Success)
        verify(appDataStore).saveLastSyncTime(any())
    }

    @Test
    fun `syncTasks should handle server-only tasks correctly`() = runTest {
        whenever(taskRepository.getLastSyncTimestamp()).thenReturn(flowOf(null))
        whenever(taskRepository.getAllTasks()).thenReturn(flowOf(Resource.Success(emptyList())))
        whenever(taskApiService.getTasks()).thenReturn(listOf(mockTaskDto))
        whenever(taskRepository.insertTasks(any())).thenReturn(Resource.Success(Unit))

        val result = syncManager.syncTasks().first { it !is Resource.Loading }

        assertTrue(result is Resource.Success)
        verify(taskRepository).insertTasks(any())
        verify(appDataStore).saveLastSyncTime(any())
    }

    @Test
    fun `syncTasks should handle local-only tasks correctly`() = runTest {
        whenever(taskRepository.getLastSyncTimestamp()).thenReturn(flowOf(null))
        whenever(taskRepository.getAllTasks()).thenReturn(flowOf(Resource.Success(listOf(mockTask))))
        whenever(taskApiService.getTasks()).thenReturn(emptyList())
        whenever(taskApiService.createTask(any())).thenReturn(mockTaskDto)

        val result = syncManager.syncTasks().first { it !is Resource.Loading }

        assertTrue(result is Resource.Success)
        verify(taskApiService).createTask(any())
        verify(appDataStore).saveLastSyncTime(any())
    }

    @Test
    fun `syncTasks should fail when server API throws exception`() = runTest {
        whenever(taskRepository.getLastSyncTimestamp()).thenReturn(flowOf(null))
        whenever(taskRepository.getAllTasks()).thenReturn(flowOf(Resource.Success(emptyList())))
        whenever(taskApiService.getTasks()).thenThrow(RuntimeException("Network error"))

        val result = syncManager.syncTasks().first { it !is Resource.Loading }

        assertTrue(result is Resource.Error)
    }

    @Test
    fun `createServerTask should return success when API call succeeds`() = runTest {
        whenever(taskApiService.createTask(any())).thenReturn(mockTaskDto)

        val result = syncManager.createServerTask(mockTask).first()

        assertTrue(result is Resource.Success)
        verify(taskApiService).createTask(any())
    }

    @Test
    fun `updateServerTask should return success when API call succeeds`() = runTest {
        whenever(taskApiService.updateTask(any(), any())).thenReturn(mockTaskDto)

        val result = syncManager.updateServerTask(mockTask).first()

        assertTrue(result is Resource.Success)
        verify(taskApiService).updateTask(any(), any())
    }
}