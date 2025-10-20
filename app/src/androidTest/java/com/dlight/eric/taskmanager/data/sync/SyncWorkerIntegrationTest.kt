package com.dlight.eric.taskmanager.data.sync

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Configuration
import androidx.work.NetworkType
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class SyncWorkerIntegrationTest {

    private lateinit var context: Context
    private lateinit var workManager: WorkManager

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()
        
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
        workManager = WorkManager.getInstance(context)
    }

    @Test
    fun testEnqueueImmediateSync() {
        SyncWorker.enqueueImmediateSync(context)

        val workInfos = workManager.getWorkInfosForUniqueWork("task_sync_work").get()
        assertTrue("Work should be enqueued", workInfos.isNotEmpty())

        val workInfo = workInfos[0]
        assertEquals("Work should be enqueued", WorkInfo.State.ENQUEUED, workInfo.state)
        
        val constraints = workInfo.constraints
        assertEquals("Should require connected network", NetworkType.CONNECTED, constraints.requiredNetworkType)
    }

    @Test
    fun testEnqueuePeriodicSync() {
        SyncWorker.enqueuePeriodicSync(context)

        val workInfos = workManager.getWorkInfosForUniqueWork("periodic_task_sync_work").get()
        assertTrue("Periodic work should be enqueued", workInfos.isNotEmpty())

        val workInfo = workInfos[0]
        assertEquals("Work should be enqueued", WorkInfo.State.ENQUEUED, workInfo.state)
        
        val constraints = workInfo.constraints
        assertEquals("Should require connected network", NetworkType.CONNECTED, constraints.requiredNetworkType)
    }

    @Test
    fun testCancelSync() {
        SyncWorker.enqueueImmediateSync(context)
        
        var workInfos = workManager.getWorkInfosForUniqueWork("task_sync_work").get()
        assertTrue("Work should be enqueued first", workInfos.isNotEmpty())
        assertEquals("Work should be enqueued", WorkInfo.State.ENQUEUED, workInfos[0].state)

        SyncWorker.cancelSync(context)

        workInfos = workManager.getWorkInfosForUniqueWork("task_sync_work").get()
        if (workInfos.isNotEmpty()) {
            val workState = workInfos[0].state
            assertTrue(
                "Work should be cancelled or finished", 
                workState == WorkInfo.State.CANCELLED || workState.isFinished
            )
        }
    }

    @Test
    fun testWorkReplacement() {
        SyncWorker.enqueueImmediateSync(context)
        val firstWorkInfos = workManager.getWorkInfosForUniqueWork("task_sync_work").get()
        val firstWorkId = firstWorkInfos[0].id

        SyncWorker.enqueueImmediateSync(context)
        val secondWorkInfos = workManager.getWorkInfosForUniqueWork("task_sync_work").get()
        
        assertTrue("Should have work enqueued", secondWorkInfos.isNotEmpty())
        assertTrue(
            "New work should replace old work due to REPLACE policy",
            secondWorkInfos.none { it.id == firstWorkId } || 
            secondWorkInfos.any { it.id != firstWorkId && it.state == WorkInfo.State.ENQUEUED }
        )
    }

    @Test
    fun testPeriodicWorkInterval() {
        SyncWorker.enqueuePeriodicSync(context)

        val workInfos = workManager.getWorkInfosForUniqueWork("periodic_task_sync_work").get()
        assertTrue("Periodic work should be enqueued", workInfos.isNotEmpty())

        val workInfo = workInfos[0]
        assertTrue("Should be periodic work", workInfo.tags.contains("androidx.work.PeriodicWorkRequest"))
    }

    @Test
    fun testMultipleImmediateSyncEnqueue() {
        repeat(3) {
            SyncWorker.enqueueImmediateSync(context)
        }

        val workInfos = workManager.getWorkInfosForUniqueWork("task_sync_work").get()
        assertEquals(
            "Should only have one work due to unique work name", 
            1, 
            workInfos.size
        )
    }

    @Test
    fun testWorkConstraints() {
        SyncWorker.enqueueImmediateSync(context)

        val workInfos = workManager.getWorkInfosForUniqueWork("task_sync_work").get()
        val workInfo = workInfos[0]
        val constraints = workInfo.constraints

        assertEquals("Should require connected network", NetworkType.CONNECTED, constraints.requiredNetworkType)
        assertEquals("Should not require charging", false, constraints.requiresCharging())
        assertEquals("Should not require device idle", false, constraints.requiresDeviceIdle())
        assertEquals("Should not require battery not low", false, constraints.requiresBatteryNotLow())
        assertEquals("Should not require storage not low", false, constraints.requiresStorageNotLow())
    }
}