package com.dlight.eric.taskmanager.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dlight.eric.taskmanager.data.local.entities.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    
    @Query("SELECT * FROM tasks ORDER BY updatedAt DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>
    
    @Query("SELECT * FROM tasks WHERE id = :id")
    fun getTaskById(id: String): Flow<TaskEntity?>
    
    @Query("SELECT * FROM tasks WHERE updatedAt > :lastSyncTime")
    fun getUnSyncedTasks(lastSyncTime: Long): Flow<List<TaskEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)
    
    @Update
    suspend fun updateTask(task: TaskEntity)
    
    @Query("UPDATE tasks SET completed = :completed, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateTaskCompletion(id: String, completed: Boolean, updatedAt: Long)
    
    @Delete
    suspend fun deleteTask(task: TaskEntity)
    
    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: String)
    
    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()
    
    @Query("SELECT COUNT(*) FROM tasks")
    fun getTaskCount(): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM tasks WHERE completed = 1")
    fun getCompletedTaskCount(): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM tasks WHERE updatedAt > :lastSyncTime")
    fun getUnSyncedTaskCount(lastSyncTime: Long): Flow<Int>
}
