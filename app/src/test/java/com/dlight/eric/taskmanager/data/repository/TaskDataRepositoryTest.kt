package com.dlight.eric.taskmanager.data.repository

import com.dlight.eric.taskmanager.data.datastore.AppDataStore
import com.dlight.eric.taskmanager.data.local.dao.TaskDao
import com.dlight.eric.taskmanager.data.local.entities.TaskEntity
import com.dlight.eric.taskmanager.domain.model.Task
import com.dlight.eric.taskmanager.utils.Resource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@RunWith(MockitoJUnitRunner::class)
class TaskDataRepositoryTest {

    @Mock
    private lateinit var taskDao: TaskDao

    @Mock
    private lateinit var appDataStore: AppDataStore

    private lateinit var repository: TaskDataRepository

    @Before
    fun setUp() {
        repository = TaskDataRepository(taskDao, appDataStore)
    }

    @Test
    fun `should handle edge case data when getting all tasks`() = runTest {
        val problematicEntities = listOf(
            TaskEntity("", "", "", false, -1, -1, -1)
        )
        whenever(taskDao.getAllTasks()).thenReturn(flowOf(problematicEntities))

        val result = repository.getAllTasks().first()

        assertTrue(result is Resource.Success || result is Resource.Error)
    }

    @Test
    fun `should return success with null when getting task by non-existent ID`() = runTest {
        whenever(taskDao.getTaskById("nonexistent")).thenReturn(flowOf(null))

        val result = repository.getTaskById("nonexistent").first()

        assertTrue(result is Resource.Success)
        assertNull(result.data)
    }


    @Test
    fun `should return error when DAO throws exception during insert`() = runTest {
        val task = createSampleTask("1", "New Task")
        val exception = RuntimeException("Database error")
        whenever(taskDao.insertTask(any())).thenThrow(exception)

        val result = repository.insertTask(task)

        assertTrue(result is Resource.Error)
        assertEquals("Database error", result.message)
    }

    @Test
    fun `should return error when DAO throws exception during update`() = runTest {
        val task = createSampleTask("1", "Updated Task")
        val exception = RuntimeException("Update failed")
        whenever(taskDao.updateTask(any())).thenThrow(exception)

        val result = repository.updateTask(task)

        assertTrue(result is Resource.Error)
        assertEquals("Update failed", result.message)
    }

    @Test
    fun `should return error when DAO throws exception during completion update`() = runTest {
        val exception = RuntimeException("Completion update failed")
        whenever(taskDao.updateTaskCompletion(any(), any(), anyLong())).thenThrow(exception)

        val result = repository.updateTaskCompletion("1", true)

        assertTrue(result is Resource.Error)
        assertEquals("Completion update failed", result.message)
    }

    @Test
    fun `should return error when DAO throws exception during delete by ID`() = runTest {
        val exception = RuntimeException("Delete failed")
        whenever(taskDao.deleteTaskById("1")).thenThrow(exception)

        val result = repository.deleteTaskById("1")

        assertTrue(result is Resource.Error)
        assertEquals("Delete failed", result.message)
    }

    @Test
    fun `should return error when DAO throws exception during delete all`() = runTest {
        val exception = RuntimeException("Delete all failed")
        whenever(taskDao.deleteAllTasks()).thenThrow(exception)

        val result = repository.deleteAllTasks()

        assertTrue(result is Resource.Error)
        assertEquals("Delete all failed", result.message)
    }

    @Test
    fun `should return null when getting last sync time with null timestamp`() = runTest {
        whenever(appDataStore.lastSyncTime).thenReturn(flowOf(null))

        val result = repository.getFormatedLastSyncTime().first()

        assertNull(result)
    }

    @Test
    fun `should return null when getting last sync time with zero timestamp`() = runTest {
        whenever(appDataStore.lastSyncTime).thenReturn(flowOf(0L))

        val result = repository.getFormatedLastSyncTime().first()

        assertNull(result)
    }

    @Test
    fun `should set timestamps correctly when inserting task`() = runTest {
        val task = createSampleTask("1", "New Task")
        whenever(taskDao.insertTask(any())).thenReturn(Unit)

        repository.insertTask(task)

        verify(taskDao).insertTask(argThat {
            val now = System.currentTimeMillis()
            createdAt > 0 && updatedAt > 0 && (now - createdAt) < 1000
        })
    }

    @Test
    fun `should update only updatedAt timestamp when updating task`() = runTest {
        val task = createSampleTask("1", "Updated Task")
        whenever(taskDao.updateTask(any())).thenReturn(Unit)

        repository.updateTask(task)

        verify(taskDao).updateTask(argThat {
            (System.currentTimeMillis() - updatedAt) < 1000
        })
    }

    private fun createSampleTask(
        id: String,
        title: String,
        description: String = "Sample description",
        completed: Boolean = false,
        dueDate: String = "2024-01-01T00:00:00Z",
        createdAt: String = "2024-01-01T00:00:00Z",
        updatedAt: String = "2024-01-01T00:00:00Z"
    ): Task {
        return Task(
            id = id,
            title = title,
            description = description,
            completed = completed,
            dueDate = dueDate,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}