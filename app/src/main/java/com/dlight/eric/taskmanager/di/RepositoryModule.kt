package com.dlight.eric.taskmanager.di

import com.dlight.eric.taskmanager.data.repository.AuthDataRepository
import com.dlight.eric.taskmanager.data.repository.TaskDataRepository
import com.dlight.eric.taskmanager.domain.repository.AuthRepository
import com.dlight.eric.taskmanager.domain.repository.TaskRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindAuthRepository(
        authDataRepository: AuthDataRepository
    ): AuthRepository

    @Binds
    abstract fun bindTaskRepository(
        taskDataRepository: TaskDataRepository
    ): TaskRepository
}
