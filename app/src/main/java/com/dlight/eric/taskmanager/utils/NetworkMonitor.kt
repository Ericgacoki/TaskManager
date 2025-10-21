package com.dlight.eric.taskmanager.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import com.dlight.eric.taskmanager.data.sync.SyncWorker

class NetworkMonitor(
    private val context: Context,
    private val connectivityManager: ConnectivityManager
) {
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var isCurrentlyConnected = false
    private var isMonitoring = false
    private val connectivityListeners = mutableListOf<ConnectivityListener>()

    interface ConnectivityListener {
        fun onConnectivityChanged(isConnected: Boolean)
    }

    companion object {
        private const val TAG = "NetworkMonitor"
    }

    init {
        isCurrentlyConnected = checkInitialConnectivity()
    }

    private fun checkInitialConnectivity(): Boolean {
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    fun addConnectivityListener(listener: ConnectivityListener) {
        connectivityListeners.add(listener)
    }

    fun removeConnectivityListener(listener: ConnectivityListener) {
        connectivityListeners.remove(listener)
    }

    private fun notifyConnectivityChange(isConnected: Boolean) {
        connectivityListeners.forEach { listener ->
            listener.onConnectivityChanged(isConnected)
        }
    }

    fun startMonitoring() {
        if (isMonitoring) {
            Log.d(TAG, "Network monitoring already active")
            return
        }

        Log.d(TAG, "Starting network monitoring")
        
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.d(TAG, "Network became available")
                
                if (!isCurrentlyConnected) {
                    Log.d(TAG, "Device was offline, triggering immediate sync")
                    isCurrentlyConnected = true

                    notifyConnectivityChange(true)
                    SyncWorker.enqueueImmediateSync(context)
                }
            }

            override fun onLost(network: Network) {
                Log.d(TAG, "Network lost")
                if (isCurrentlyConnected) {
                    isCurrentlyConnected = false
                    notifyConnectivityChange(false)
                }
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                val hasInternet = networkCapabilities.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_INTERNET
                )
                val hasValidated = networkCapabilities.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_VALIDATED
                )
                
                if (hasInternet && hasValidated && !isCurrentlyConnected) {
                    Log.d(TAG, "Network validated with internet, triggering sync")
                    isCurrentlyConnected = true
                    notifyConnectivityChange(true)
                    SyncWorker.enqueueImmediateSync(context)
                } else if (!hasInternet && isCurrentlyConnected) {
                    isCurrentlyConnected = false
                    notifyConnectivityChange(false)
                }
            }
        }

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            .build()

        try {
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback!!)
            isMonitoring = true
            Log.d(TAG, "Network monitoring started successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register network callback", e)
            isMonitoring = false
        }
    }

    fun stopMonitoring() {
        if (!isMonitoring) {
            return
        }

        Log.d(TAG, "Stopping network monitoring")
        
        networkCallback?.let {
            try {
                connectivityManager.unregisterNetworkCallback(it)
                Log.d(TAG, "Network monitoring stopped")
            } catch (e: Exception) {
                Log.e(TAG, "Error unregistering network callback", e)
            }
        }
        
        networkCallback = null
        isMonitoring = false
    }

    fun isConnected(): Boolean = isCurrentlyConnected
}
