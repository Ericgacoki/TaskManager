package com.dlight.eric.taskmanager.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.dlight.eric.taskmanager.utils.NetworkMonitor
import com.dlight.eric.taskmanager.data.sync.SyncWorker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class TaskManagerApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var networkMonitor: NetworkMonitor

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        SyncWorker.enqueuePeriodicSync(this)
        
        // Start network monitoring for immediate sync when network becomes available
        networkMonitor.startMonitoring()
    }
}
