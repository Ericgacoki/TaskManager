package com.dlight.eric.taskmanager.data.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dlight.eric.taskmanager.data.local.dao.TaskDao
import com.dlight.eric.taskmanager.data.local.database.AppDatabase
import com.dlight.eric.taskmanager.data.local.entities.TaskEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class TaskDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var taskDao: TaskDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        
        taskDao = database.taskDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertTask_andGetById_returns_CorrectTask() = runTest {
        val task = createSampleTask("1")
        
        taskDao.insertTask(task)
        
        val retrievedTask = taskDao.getTaskById("1").first()
        
        assertNotNull(retrievedTask)
        assertEquals(task.title, retrievedTask?.title)
    }

    @Test
    fun insertTask_withDuplicateId_replaces_ExistingTask() = runTest {
        val originalTask = createSampleTask("1", title = "Original Task")
        val updatedTask = createSampleTask("1", title = "Updated Task")
        
        taskDao.insertTask(originalTask)
        taskDao.insertTask(updatedTask)
        
        val retrievedTask = taskDao.getTaskById("1").first()
        
        assertNotNull(retrievedTask)
        assertEquals("Updated Task", retrievedTask?.title)
    }

    @Test
    fun insertTasks_batchInsert_inserts_AllTasks() = runTest {
        val tasks = listOf(
            createSampleTask("1"),
            createSampleTask("2"),
            createSampleTask("3")
        )
        
        taskDao.insertTasks(tasks)
        
        val allTasks = taskDao.getAllTasks().first()
        
        assertEquals(3, allTasks.size)
    }

    @Test
    fun getAllTasks_ordered_ByUpdatedAtDesc() = runTest {
        val currentTime = System.currentTimeMillis()
        val task1 = createSampleTask("1", updatedAt = currentTime)
        val task2 = createSampleTask("2", updatedAt = currentTime + 1000)
        val task3 = createSampleTask("3", updatedAt = currentTime + 2000)
        
        taskDao.insertTasks(listOf(task1, task2, task3))
        
        val allTasks = taskDao.getAllTasks().first()
        
        assertEquals("3", allTasks[0].id) // Most recent first
        assertEquals("1", allTasks[2].id) // Oldest last
    }

    @Test
    fun getTaskById_nonExistentId_returns_Null() = runTest {
        val retrievedTask = taskDao.getTaskById("nonexistent").first()
        
        assertNull(retrievedTask)
    }

    @Test
    fun updateTask_modifies_ExistingTask() = runTest {
        val originalTask = createSampleTask("1", title = "Original")
        taskDao.insertTask(originalTask)
        
        val updatedTask = originalTask.copy(title = "Updated Title")
        
        taskDao.updateTask(updatedTask)
        
        val retrievedTask = taskDao.getTaskById("1").first()
        
        assertEquals("Updated Title", retrievedTask?.title)
    }

    @Test
    fun updateTaskCompletion_toggles_CompletionStatus() = runTest {
        val task = createSampleTask("1", completed = false)
        taskDao.insertTask(task)
        
        val newTimestamp = System.currentTimeMillis()
        taskDao.updateTaskCompletion("1", true, newTimestamp)
        
        val retrievedTask = taskDao.getTaskById("1").first()
        
        assertTrue(retrievedTask?.completed == true)
        assertEquals(newTimestamp, retrievedTask?.updatedAt)
    }

    @Test
    fun deleteTaskById_removes_TaskFromDatabase() = runTest {
        val task = createSampleTask("1")
        taskDao.insertTask(task)
        
        taskDao.deleteTaskById("1")
        
        val retrievedTask = taskDao.getTaskById("1").first()
        assertNull(retrievedTask)
    }

    @Test
    fun deleteAllTasks_removes_AllTasksFromDatabase() = runTest {
        val tasks = listOf(
            createSampleTask("1"),
            createSampleTask("2"),
            createSampleTask("3")
        )
        taskDao.insertTasks(tasks)
        
        taskDao.deleteAllTasks()
        
        val allTasks = taskDao.getAllTasks().first()
        assertTrue(allTasks.isEmpty())
    }


    @Test
    fun getCompletedTaskCount_returns_CorrectCount() = runTest {
        val tasks = listOf(
            createSampleTask("1", completed = true),
            createSampleTask("2", completed = false),
            createSampleTask("3", completed = true),
            createSampleTask("4", completed = false)
        )
        taskDao.insertTasks(tasks)
        
        val completedCount = taskDao.getCompletedTaskCount().first()
        
        assertEquals(2, completedCount)
    }

    @Test
    fun getUnSyncedTasks_returns_TasksModifiedAfterLastSync() = runTest {
        val lastSyncTime = System.currentTimeMillis()
        val tasks = listOf(
            createSampleTask("1", updatedAt = lastSyncTime - 1000), // Before sync
            createSampleTask("2", updatedAt = lastSyncTime + 1000), // After sync
            createSampleTask("3", updatedAt = lastSyncTime + 2000), // After sync
            createSampleTask("4", updatedAt = lastSyncTime - 500)   // Before sync
        )
        taskDao.insertTasks(tasks)
        
        val unSyncedTasks = taskDao.getUnSyncedTasks(lastSyncTime).first()
        
        assertEquals(2, unSyncedTasks.size)
        assertTrue(unSyncedTasks.any { it.id == "2" })
        assertTrue(unSyncedTasks.any { it.id == "3" })
    }


    @Test
    fun flowUpdates_emit_NewValuesWhenDataChanges() = runTest {
        val initialTask = createSampleTask("1", title = "Initial")
        taskDao.insertTask(initialTask)
        
        val firstEmission = taskDao.getTaskById("1").first()
        assertEquals("Initial", firstEmission?.title)
        
        val updatedTask = initialTask.copy(title = "Updated")
        taskDao.updateTask(updatedTask)
        
        val secondEmission = taskDao.getTaskById("1").first()
        assertEquals("Updated", secondEmission?.title)
    }

    private fun createSampleTask(
        id: String,
        title: String = "Sample Task",
        description: String = "Sample Description",
        completed: Boolean = false,
        dueDate: Long = System.currentTimeMillis(),
        createdAt: Long = System.currentTimeMillis(),
        updatedAt: Long = System.currentTimeMillis()
    ): TaskEntity {
        return TaskEntity(
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