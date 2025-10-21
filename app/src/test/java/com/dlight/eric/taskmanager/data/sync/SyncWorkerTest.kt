package com.dlight.eric.taskmanager.data.sync

import android.content.Context
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.dlight.eric.taskmanager.domain.repository.SyncRepository
import com.dlight.eric.taskmanager.utils.Resource
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever

@RunWith(MockitoJUnitRunner::class)
class SyncWorkerTest {

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var syncRepository: SyncRepository

    private lateinit var workerFactory: TestSyncWorkerFactory

    @Before
    fun setUp() {
        workerFactory = TestSyncWorkerFactory(syncRepository)
    }

    @Test
    fun `doWork should return success when sync succeeds`() = runTest {
        whenever(syncRepository.syncTasks()).thenReturn(
            flowOf(Resource.Loading(), Resource.Success(Unit))
        )

        val worker = TestListenableWorkerBuilder<SyncWorker>(context)
            .setWorkerFactory(workerFactory)
            .build()

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
    }

    @Test
    fun `doWork should return failure when sync throws exception`() = runTest {
        val exception = RuntimeException("Database error")
        whenever(syncRepository.syncTasks()).thenThrow(exception)

        val worker = TestListenableWorkerBuilder<SyncWorker>(context)
            .setWorkerFactory(workerFactory)
            .build()

        val result = worker.doWork()

        val expectedData = Data.Builder()
            .putString("error_message", "Database error")
            .putString("exception_type", "RuntimeException")
            .build()

        assertEquals(ListenableWorker.Result.failure(expectedData), result)
    }

    @Test
    fun `doWork should skip loading state and wait for final result`() = runTest {
        whenever(syncRepository.syncTasks()).thenReturn(
            flowOf(
                Resource.Loading(), 
                Resource.Loading(), 
                Resource.Success(Unit)
            )
        )

        val worker = TestListenableWorkerBuilder<SyncWorker>(context)
            .setWorkerFactory(workerFactory)
            .build()

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
    }
}
