package com.dlight.eric.taskmanager.data.mapper

import com.dlight.eric.taskmanager.data.mapper.TaskMapper.toDomain
import com.dlight.eric.taskmanager.data.mapper.TaskMapper.toDto
import com.dlight.eric.taskmanager.data.mapper.TaskMapper.toEntity
import com.dlight.eric.taskmanager.data.remote.dto.TaskDto
import com.dlight.eric.taskmanager.domain.model.Task
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskMapperTest {

    @Test
    fun `should map TaskDto to Domain with null fields using defaults`() {
        val dto = TaskDto(
            id = null,
            title = null,
            description = null,
            completed = null,
            dueDate = null,
            createdAt = null,
            updatedAt = null
        )

        val domain = dto.toDomain()

        assertNotNull(domain.id)
        assertTrue(domain.id.isNotEmpty())
        assertEquals("", domain.title)
        assertEquals("", domain.description)
        assertFalse(domain.completed)
    }

    @Test
    fun `should convert empty description to null in Domain to DTO mapping`() {
        val domain = createSampleTask(description = "")

        val dto = domain.toDto()

        assertEquals(null, dto.description)
    }

    @Test
    fun `should auto fill Entity when dto fields are null`() {
        val dto = TaskDto(
            id = null,
            title = null,
            description = null,
            completed = null,
            dueDate = null,
            createdAt = null,
            updatedAt = null
        )

        val entity = dto.toEntity()

        assertNotNull(entity.id)
        assertEquals("", entity.title)
        assertEquals("", entity.description)
        assertFalse(entity.completed)
        assertTrue(entity.dueDate > 0)
        assertTrue(entity.createdAt > 0)
        assertTrue(entity.updatedAt > 0)
    }

    private fun createSampleTask(
        id: String = "test-id",
        title: String = "Test Task",
        description: String = "",
        completed: Boolean = false,
        dueDate: String = "2022-01-01T00:00:00Z",
        createdAt: String = "2022-01-01T00:00:00Z",
        updatedAt: String = "2022-01-01T00:00:00Z"
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