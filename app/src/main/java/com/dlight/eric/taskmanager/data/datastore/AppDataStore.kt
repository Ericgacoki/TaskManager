package com.dlight.eric.taskmanager.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AppDataStore(
    @ApplicationContext private val context: Context
) {
    companion object {
        val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")
    }

    private val authTokenKey = stringPreferencesKey("auth_token")
    private val userEmailKey = stringPreferencesKey("user_email")
    private val lastSyncTimeKey = longPreferencesKey("last_sync_time")

    val authToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[authTokenKey]
    }

    val userEmail: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[userEmailKey]
    }

    val lastSyncTime: Flow<Long?> = context.dataStore.data.map { preferences ->
        preferences[lastSyncTimeKey]
    }

    suspend fun saveAuthToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[authTokenKey] = token
        }
    }

    suspend fun saveUserEmail(email: String) {
        context.dataStore.edit { preferences ->
            preferences[userEmailKey] = email
        }
    }

    suspend fun saveLastSyncTime(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[lastSyncTimeKey] = timestamp
        }
    }

    suspend fun clearAuthData() {
        context.dataStore.edit { preferences ->
            preferences.remove(authTokenKey)
            preferences.remove(userEmailKey)
        }
    }
}
