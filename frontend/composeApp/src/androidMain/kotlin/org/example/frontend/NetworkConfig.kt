package org.example.frontend

import android.content.Context

object NetworkConfig {
    // No longer a 'const'
    var SERVER_IP = "192.168.1.7:5001"

    // Helper to load the saved IP when the app starts
    fun loadIp(context: Context) {
        val prefs = context.getSharedPreferences("DebugPrefs", Context.MODE_PRIVATE)
        SERVER_IP = prefs.getString("saved_ip", "192.168.1.7:5001") ?: "192.168.1.7:5001"
    }

    // Helper to save a new IP
    fun saveIp(context: Context, newIp: String) {
        SERVER_IP = newIp
        val prefs = context.getSharedPreferences("DebugPrefs", Context.MODE_PRIVATE)
        prefs.edit().putString("saved_ip", newIp).apply()
    }
}