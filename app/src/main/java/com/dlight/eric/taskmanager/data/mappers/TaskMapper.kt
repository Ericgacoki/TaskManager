package com.dlight.eric.taskmanager.data.mappers

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
            createdAt = DateUtils.formatTimestamp(createdAt),
            updatedAt = DateUtils.formatTimestamp(updatedAt)
        )
    }

    fun Task.toEntity(timestampCreated: Long? = null, timestampUpdated: Long? = null, timestampDueDate: Long? = null): TaskEntity {
        return TaskEntity(
            id = id,
            title = title,
            description = description,
            completed = completed,
            dueDate = timestampDueDate ?: DateUtils.parseIsoString(dueDate),
            createdAt = timestampCreated ?: System.currentTimeMillis(),
            updatedAt = timestampUpdated ?: System.currentTimeMillis()
        )
    }

    fun TaskDto.toDomain(): Task {
        val createdTimestamp =
            createdAt?.let { DateUtils.parseIsoString(it) } ?: System.currentTimeMillis()
        val updatedTimestamp =
            updatedAt?.let { DateUtils.parseIsoString(it) } ?: System.currentTimeMillis()
        return Task(
            id = id ?: UUID.randomUUID().toString(),
            title = title ?: "",
            description = description ?: "",
            completed = completed ?: false,
            dueDate = dueDate ?: DateUtils.formatToIsoString(System.currentTimeMillis()),
            createdAt = DateUtils.formatTimestamp(createdTimestamp),
            updatedAt = DateUtils.formatTimestamp(updatedTimestamp)
        )
    }

    fun Task.toDto(isoCreatedAt: String? = null, isoUpdatedAt: String? = null, isoDueDate: String? = null): TaskDto {
        return TaskDto(
            id = id,
            title = title,
            description = description.ifEmpty { null },
            completed = completed,
            dueDate = isoDueDate ?: dueDate,
            createdAt = isoCreatedAt ?: DateUtils.formatToIsoString(System.currentTimeMillis()),
            updatedAt = isoUpdatedAt ?: DateUtils.formatToIsoString(System.currentTimeMillis())
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
