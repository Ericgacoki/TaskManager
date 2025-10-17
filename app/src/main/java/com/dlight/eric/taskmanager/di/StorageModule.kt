package com.dlight.eric.taskmanager.di

import android.content.Context
import androidx.room.Room
import com.dlight.eric.taskmanager.data.datastore.AppDataStore
import com.dlight.eric.taskmanager.data.local.dao.TaskDao
import com.dlight.eric.taskmanager.data.local.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StorageModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        ).build()
    }

    @Provides
    fun provideTaskDao(database: AppDatabase): TaskDao {
        return database.taskDao()
    }

    @Provides
    @Singleton
    fun provideAppDataStore(@ApplicationContext context: Context): AppDataStore {
        return AppDataStore(context)
    }
}