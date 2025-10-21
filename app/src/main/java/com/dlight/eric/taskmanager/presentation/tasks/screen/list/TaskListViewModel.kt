package com.dlight.eric.taskmanager.presentation.tasks.screen.list

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo.State.BLOCKED
import androidx.work.WorkInfo.State.CANCELLED
import androidx.work.WorkInfo.State.ENQUEUED
import androidx.work.WorkInfo.State.FAILED
import androidx.work.WorkInfo.State.RUNNING
import androidx.work.WorkInfo.State.SUCCEEDED
import androidx.work.WorkManager
import com.dlight.eric.taskmanager.domain.repository.AuthRepository
import com.dlight.eric.taskmanager.domain.repository.SyncRepository
import com.dlight.eric.taskmanager.domain.repository.TaskRepository
import com.dlight.eric.taskmanager.presentation.tasks.event.TaskEvent
import com.dlight.eric.taskmanager.presentation.tasks.state.SyncState
import com.dlight.eric.taskmanager.presentation.tasks.state.TaskListUiState
import com.dlight.eric.taskmanager.utils.NetworkMonitor
import com.dlight.eric.taskmanager.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository,
    private val networkMonitor: NetworkMonitor,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _tasksUiState = MutableStateFlow(TaskListUiState())
    val tasksUiState: StateFlow<TaskListUiState> = _tasksUiState.asStateFlow()
    
    private var connectivityListener: NetworkMonitor.ConnectivityListener? = null

    init {
        getLastSyncTime()
        getUnSyncedTaskCount()
        onEvent(TaskEvent.LoadTasks)

        observeSyncWorker()
        observeNetworkState()
    }

    fun onEvent(event: TaskEvent) {
        when (event) {
            is TaskEvent.LoadTasks -> loadTasks()
            is TaskEvent.ToggleTaskCompletion -> toggleTaskCompletion(
                event.taskId,
                event.isCompleted
            )

            is TaskEvent.DeleteTask -> deleteTask(event.taskId)
            is TaskEvent.DeleteAll -> deleteAllTasks()
            is TaskEvent.LogOut -> logOut()
            is TaskEvent.PullToRefresh -> pullToRefresh()
            is TaskEvent.Retry -> retry()
            is TaskEvent.TriggerSync -> triggerSync()
        }
    }

    private fun loadTasks(pullToRefresh: Boolean = false) {
        viewModelScope.launch {
            if (pullToRefresh) {
                _tasksUiState.update {
                    it.copy(
                        isLoading = false,
                        isPullingToRefresh = true,
                        error = null
                    )
                }
                delay(3000) // simulate await call
            }

            taskRepository.getAllTasks().collectLatest { result ->
                when (result) {
                    is Resource.Loading -> {
                        _tasksUiState.update {
                            it.copy(
                                isLoading = !pullToRefresh,
                                isPullingToRefresh = pullToRefresh,
                                error = null
                            )
                        }
                    }

                    is Resource.Success -> {
                        _tasksUiState.update {
                            it.copy(
                                isLoading = false,
                                isPullingToRefresh = false,
                                tasks = result.data ?: emptyList(),
                                error = null
                            )
                        }
                    }

                    is Resource.Error -> {
                        _tasksUiState.update {
                            it.copy(
                                isLoading = false,
                                isPullingToRefresh = false,
                                error = result.message
                            )
                        }
                    }
                }
            }
        }
    }

    private fun toggleTaskCompletion(taskId: String, isCompleted: Boolean) {
        viewModelScope.launch {
            val result = taskRepository.updateTaskCompletion(taskId, isCompleted)
            if (result is Resource.Error) {
                _tasksUiState.update {
                    it.copy(error = result.message)
                }
            } else if (result is Resource.Success) {
                // Trigger sync if network is available
                if (networkMonitor.isConnected()) {
                    syncRepository.triggerSync()
                }
            }
        }
    }

    private fun deleteTask(taskId: String) {
        viewModelScope.launch {
            val result = taskRepository.deleteTaskById(taskId)
            if (result is Resource.Error) {
                _tasksUiState.update {
                    it.copy(error = result.message)
                }
            }
        }
    }

    private fun deleteAllTasks() {
        viewModelScope.launch {
            val result = taskRepository.deleteAllTasks()
            if (result is Resource.Error) {
                _tasksUiState.update {
                    it.copy(error = result.message)
                }
            }
        }
    }

    private fun logOut() {
        viewModelScope.launch {
            // Clear all tasks from local database after logging out
            authRepository.logout()
            taskRepository.deleteAllTasks()
        }
    }

    private fun getLastSyncTime() {
        viewModelScope.launch {
            taskRepository.getFormatedLastSyncTime().collectLatest { lastSyncTime ->
                _tasksUiState.update {
                    it.copy(lastSyncTime = lastSyncTime)
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getUnSyncedTaskCount() {
        viewModelScope.launch {
            taskRepository.getLastSyncTimestamp()
                .flatMapLatest { timestamp ->
                    if (timestamp != null) {
                        taskRepository.getUnSyncedTaskCount(timestamp)
                    } else {
                        // if lastSyncTime is null, then all the local tasks are not synced
                        taskRepository.getTaskCount()
                    }
                }
                .collect { result ->
                    if (result is Resource.Success) {
                        val count = result.data ?: 0
                        _tasksUiState.update { state ->
                            state.copy(
                                unSyncedTaskCount = count,
                                syncState = if (count > 0 && state.syncState != SyncState.SYNCING) {
                                    SyncState.PENDING
                                } else if (count == 0 && state.syncState != SyncState.FAILED) {
                                    SyncState.SYNCED
                                } else {
                                    state.syncState
                                }
                            )
                        }
                    }
                }
        }
    }

    private fun pullToRefresh() {
        loadTasks(pullToRefresh = true)
        triggerSync()
    }

    private fun retry() {
        loadTasks()
    }

    private fun triggerSync() {
        viewModelScope.launch {
            try {
                syncRepository.triggerSync()
            } catch (_: Exception) {
            }
        }
    }

    private fun observeSyncWorker() {
        viewModelScope.launch {
            WorkManager.getInstance(context)
                .getWorkInfosForUniqueWorkFlow("task_sync_work")
                .collectLatest { workInfos ->
                    val workInfo = workInfos.firstOrNull()
                    workInfo?.let { info ->
                        when (info.state) {
                            RUNNING -> {
                                _tasksUiState.update {
                                    it.copy(
                                        syncState = SyncState.SYNCING,
                                        syncError = null
                                    )
                                }
                            }

                            SUCCEEDED -> {
                                val count = _tasksUiState.value.unSyncedTaskCount
                                _tasksUiState.update {
                                    it.copy(
                                        syncState = if (count > 0) SyncState.PENDING else SyncState.SYNCED,
                                        syncError = null
                                    )
                                }
                            }

                            FAILED -> {
                                val errorMessage =
                                    info.outputData.getString("error_message")
                                        ?: "Sync failed"
                                _tasksUiState.update {
                                    it.copy(
                                        syncState = SyncState.FAILED,
                                        syncError = errorMessage
                                    )
                                }
                                Log.e("WORK OBSERVER", "SYNC FAILED: $errorMessage")
                            }

                            CANCELLED -> {
                                _tasksUiState.update {
                                    it.copy(
                                        syncState = SyncState.PENDING,
                                        syncError = null
                                    )
                                }
                                Log.d("WORK OBSERVER", "SYNC CANCELLED")
                            }

                            ENQUEUED, BLOCKED -> {
                                // Work is waiting or blocked - keep current state
                                Log.d("WORK OBSERVER", "SYNC ${info.state.name}")
                            }
                        }
                    }
                }
        }
    }

    private fun observeNetworkState() {
        connectivityListener = object : NetworkMonitor.ConnectivityListener {
            override fun onConnectivityChanged(isConnected: Boolean) {
                val currentState = _tasksUiState.value.syncState

                if (currentState == SyncState.SYNCING && !isConnected) {
                    _tasksUiState.update {
                        it.copy(
                            syncState = SyncState.FAILED,
                            syncError = "Network connection lost"
                        )
                    }
                }
            }
        }
        
        connectivityListener?.let { listener ->
            networkMonitor.addConnectivityListener(listener)
            networkMonitor.startMonitoring()
        }
    }

    override fun onCleared() {
        super.onCleared()
        connectivityListener?.let { listener ->
            networkMonitor.removeConnectivityListener(listener)
            networkMonitor.stopMonitoring()
        }
    }
}
