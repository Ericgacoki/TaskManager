package com.dlight.eric.taskmanager.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.dlight.eric.taskmanager.domain.repository.SyncRepository
import com.dlight.eric.taskmanager.utils.Resource
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncRepository: SyncRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val syncResult = syncRepository.syncTasks()
                .first { it !is Resource.Loading }

            when (syncResult) {
                is Resource.Success -> {
                    Result.success()
                }

                is Resource.Error -> {
                    val outputData = Data.Builder()
                        .putString("error_message", syncResult.message)
                        .build()

                    Result.failure(outputData)
                }

                is Resource.Loading -> {
                    Result.retry() // Should never reach here
                }
            }
        } catch (e: Exception) {
            val outputData = Data.Builder()
                .putString("error_message", e.message)
                .putString("exception_type", e.javaClass.simpleName)
                .build()

            Result.failure(outputData)
        }
    }

    companion object {
        private const val SYNC_WORK_NAME = "task_sync_work"
        private const val PERIODIC_SYNC_WORK_NAME = "periodic_task_sync_work"

        fun enqueueImmediateSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncWork = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    SYNC_WORK_NAME,
                    ExistingWorkPolicy.REPLACE,
                    syncWork
                )
        }

        fun enqueuePeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val periodicSyncWork = PeriodicWorkRequestBuilder<SyncWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    PERIODIC_SYNC_WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    periodicSyncWork
                )
        }

        fun cancelSync(context: Context) {
            WorkManager.getInstance(context)
                .cancelUniqueWork(SYNC_WORK_NAME)
        }
    }
}
