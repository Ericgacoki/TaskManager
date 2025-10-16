package com.dlight.eric.taskmanager.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dlight.eric.taskmanager.presentation.auth.screen.LoginScreen
import com.dlight.eric.taskmanager.presentation.auth.screen.AuthViewModel
import com.dlight.eric.taskmanager.utils.TaskMode
import com.dlight.eric.taskmanager.presentation.tasks.screen.list.TaskListScreen
import com.dlight.eric.taskmanager.presentation.tasks.screen.detail.TaskDetailScreen
import com.dlight.eric.taskmanager.utils.Resource

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val authState by authViewModel.isLoggedIn().collectAsStateWithLifecycle(initialValue = Resource.Loading())

    // Show loading screen while checking authentication
    when (authState) {
        is Resource.Loading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        else -> {
            val isLoggedIn = (authState as? Resource.Success)?.data ?: false
            
            NavHost(
                navController = navController,
                startDestination = if (isLoggedIn) "tasks" else "login",
                modifier = modifier
            ) {
                composable("login") {
                    LoginScreen(
                        onLoginSuccess = {
                            navController.navigate("tasks") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    )
                }
                
                composable("tasks") {
                    TaskListScreen(
                        onNavigateToTask = { taskId, mode ->
                            navController.navigate("task_detail/$taskId/$mode")
                        },
                        onLogOut = {
                            navController.navigate("login") {
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = true
                                }
                            }
                        }
                    )
                }
                
                composable("task_detail/{taskId}/{mode}") { backStackEntry ->
                    val taskId = backStackEntry.arguments?.getString("taskId") ?: "0"
                    val modeString = backStackEntry.arguments?.getString("mode") ?: "VIEW"
                    val mode = try {
                        TaskMode.valueOf(modeString)
                    } catch (e: IllegalArgumentException) {
                        TaskMode.VIEW
                    }

                    TaskDetailScreen(taskId = taskId, mode = mode)
                }
            }
        }
    }
}