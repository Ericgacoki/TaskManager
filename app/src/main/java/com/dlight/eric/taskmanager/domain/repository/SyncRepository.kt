package com.dlight.eric.taskmanager.domain.repository

import com.dlight.eric.taskmanager.utils.Resource
import kotlinx.coroutines.flow.Flow

interface SyncRepository {

    suspend fun syncTasks(): Flow<Resource<Unit>>

    suspend fun triggerSync()
}