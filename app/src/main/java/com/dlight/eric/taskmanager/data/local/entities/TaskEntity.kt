package com.dlight.eric.taskmanager.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val completed: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)
