package org.example.frontend.progresstracking

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ScreenTimeTracker(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("dyslexia_screen_time", Context.MODE_PRIVATE)

    // Daily target set to 20 minutes (1200 seconds) for standard visual/phonetic therapy
    private val DAILY_TARGET_SECONDS = 1200f

    private val _screenTimeSeconds = MutableStateFlow(prefs.getLong("total_seconds", 0L))
    val screenTimeSeconds: StateFlow<Long> = _screenTimeSeconds.asStateFlow()

    fun addSessionTime(seconds: Long) {
        val current = prefs.getLong("total_seconds", 0L)
        val updated = current + seconds
        prefs.edit().putLong("total_seconds", updated).apply()
        _screenTimeSeconds.value = updated
    }

    fun getProgressFloat(): Float {
        val currentSeconds = _screenTimeSeconds.value.toFloat()
        // Cap progress bar visually at 1.0f (100%)
        return if (currentSeconds >= DAILY_TARGET_SECONDS) 1.0f else currentSeconds / DAILY_TARGET_SECONDS
    }

    fun getFormattedTime(): String {
        val totalSecs = _screenTimeSeconds.value
        val minutes = totalSecs / 60
        return "$minutes mins"
    }
}