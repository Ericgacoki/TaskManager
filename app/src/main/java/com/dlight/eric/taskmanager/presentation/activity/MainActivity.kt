package com.dlight.eric.taskmanager.presentation.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.dlight.eric.taskmanager.data.sync.NetworkMonitor
import com.dlight.eric.taskmanager.presentation.navigation.AppNavigation
import com.dlight.eric.taskmanager.presentation.theme.TaskManagerTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity(), NetworkMonitor.ConnectivityListener {
    
    @Inject
    lateinit var networkMonitor: NetworkMonitor
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TaskManagerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavigation(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Ensure monitoring is active when app is in foreground
        networkMonitor.startMonitoring()
        networkMonitor.addConnectivityListener(this)
    }
    
    override fun onPause() {
        super.onPause()
        networkMonitor.removeConnectivityListener(this)
        networkMonitor.stopMonitoring()
    }
    
    override fun onConnectivityChanged(isConnected: Boolean) {
        runOnUiThread {
            val message = if (isConnected) {
                "Back online! Syncing your tasks..."
            } else {
                "You're offline."
            }
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }
}

