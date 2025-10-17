package com.dlight.eric.taskmanager.domain.model

import com.dlight.eric.taskmanager.utils.DateUtils

/**
 * This Model uses dates isoFormat ["2025-01-01T10:00:00Z"] .
 *
 * Entity uses Long tileStamps [123423344]
 *
 * [DateUtils] Does the job of mapping across these values
 * */
data class Task(
    val id: String,
    val title: String,
    val description: String,
    val completed: Boolean,
    val dueDate: String,
    val createdAt: String,
    val updatedAt: String
)
