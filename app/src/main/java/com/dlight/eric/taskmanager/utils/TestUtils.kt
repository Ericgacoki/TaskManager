package com.dlight.eric.taskmanager.utils

/**
 * Utility function to detect if the app is running in a test environment.
 * This helps disable animations and infinite loops during UI tests.
 */
fun isInTest(): Boolean {
    return try {
        Class.forName("androidx.test.espresso.Espresso")
        true
    } catch (e: ClassNotFoundException) {
        false
    }
}