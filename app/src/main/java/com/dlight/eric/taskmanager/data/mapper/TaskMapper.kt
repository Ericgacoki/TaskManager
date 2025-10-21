package com.dlight.eric.taskmanager.data.mapper

import com.dlight.eric.taskmanager.data.local.entities.TaskEntity
import com.dlight.eric.taskmanager.data.remote.dto.TaskDto
import com.dlight.eric.taskmanager.domain.model.Task
import com.dlight.eric.taskmanager.utils.DateUtils
import java.util.UUID

object TaskMapper {

    fun TaskEntity.toDomain(): Task {
        return Task(
            id = id,
            title = title,
            description = description,
            completed = completed,
            dueDate = DateUtils.formatToIsoString(dueDate),
            createdAt = DateUtils.formatToIsoString(createdAt),
            updatedAt = DateUtils.formatToIsoString(updatedAt)
        )
    }

    fun Task.toEntity(timestampUpdated: Long? = null): TaskEntity {
        return TaskEntity(
            id = id.ifEmpty { UUID.randomUUID().toString() },
            title = title,
            description = description,
            completed = completed,
            dueDate = DateUtils.parseIsoString(dueDate),
            createdAt = DateUtils.parseIsoString(createdAt),
            updatedAt = timestampUpdated ?: DateUtils.parseIsoString(updatedAt)
        )
    }

    fun TaskDto.toDomain(): Task {
        return Task(
            id = id ?: UUID.randomUUID().toString(),
            title = title ?: "",
            description = description ?: "",
            completed = completed ?: false,
            dueDate = dueDate ?: DateUtils.formatToIsoString(System.currentTimeMillis()),
            createdAt = createdAt ?: DateUtils.formatToIsoString(System.currentTimeMillis()),
            updatedAt = updatedAt ?: DateUtils.formatToIsoString(System.currentTimeMillis())
        )
    }

    fun Task.toDto(): TaskDto {
        return TaskDto(
            id = id.ifEmpty { UUID.randomUUID().toString() },
            title = title,
            description = description.ifEmpty { null },
            completed = completed,
            dueDate = dueDate,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    fun TaskEntity.toDto(): TaskDto {
        return TaskDto(
            id = id,
            title = title,
            description = description.ifEmpty { null },
            completed = completed,
            dueDate = DateUtils.formatToIsoString(dueDate),
            createdAt = DateUtils.formatToIsoString(createdAt),
            updatedAt = DateUtils.formatToIsoString(updatedAt)
        )
    }

    fun TaskDto.toEntity(): TaskEntity {
        return TaskEntity(
            id = id ?: UUID.randomUUID().toString(),
            title = title ?: "",
            description = description ?: "",
            completed = completed ?: false,
            dueDate = dueDate?.let { DateUtils.parseIsoString(it) }
                ?: System.currentTimeMillis(),
            createdAt = createdAt?.let { DateUtils.parseIsoString(it) }
                ?: System.currentTimeMillis(),
            updatedAt = updatedAt?.let { DateUtils.parseIsoString(it) }
                ?: System.currentTimeMillis()
        )
    }

}
