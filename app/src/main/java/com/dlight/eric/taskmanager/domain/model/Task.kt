package com.dlight.eric.taskmanager.domain.model

import com.dlight.eric.taskmanager.utils.DateUtils

/**
 * This Model uses dates as String instead of Long for readability.
 *
 * [DateUtils] Does the job of mapping across these values
 * */
data class Task(
    val id: String,
    val title: String,
    val description: String,
    val completed: Boolean,
    val createdAt: String,
    val updatedAt: String
)
