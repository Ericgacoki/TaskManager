package com.dlight.eric.taskmanager.data.sync

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.dlight.eric.taskmanager.domain.repository.SyncRepository

class TestSyncWorkerFactory(
    private val syncRepository: SyncRepository
) : WorkerFactory() {
    
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            SyncWorker::class.java.name -> {
                SyncWorker(appContext, workerParameters, syncRepository)
            }
            else -> null
        }
    }
}