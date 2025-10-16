package com.dlight.eric.taskmanager.di

import com.dlight.eric.taskmanager.data.repository.AuthDataRepository
import com.dlight.eric.taskmanager.domain.repository.AuthRepository
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
}
