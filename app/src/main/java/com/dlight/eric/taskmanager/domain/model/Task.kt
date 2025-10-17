package com.dlight.eric.taskmanager.domain.model

import com.dlight.eric.taskmanager.data.mappers.TaskMapper

/**
 * This Model uses dates as String instead of Long for readability.
 *
 * [TaskMapper] Does the job of mapping across these values
 * */
data class Task(
    val id: String,
    val title: String,
    val description: String,
    val completed: Boolean,
    val createdOn: String,
    val updatedOn: String
)
