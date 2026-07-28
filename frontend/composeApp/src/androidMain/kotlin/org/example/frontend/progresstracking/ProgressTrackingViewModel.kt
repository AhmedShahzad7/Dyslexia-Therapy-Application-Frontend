package org.example.frontend.progresstracking

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.example.frontend.NetworkConfig // Import your global routing parameter
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class ProgressUiState(
    val isLoading: Boolean = true,
    val overallProgress: String = "0%",
    val levelFloat: Float = 0.0f,
    val levelText: String = "0%",
    val quizFloat: Float = 0.0f,
    val quizText: String = "0%",
    val screenTimeFloat: Float = 0.0f,
    val screenTimeText: String = "0 mins"
)

class ProgressTrackingViewModel(application: Application) : AndroidViewModel(application) {
    private val screenTimeTracker = ScreenTimeTracker(application)
    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    fun loadData(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val stFloat = screenTimeTracker.getProgressFloat()
            val stText = screenTimeTracker.getFormattedTime()

            val networkResult = withContext(Dispatchers.IO) {
                fetchBackendStats(userId)
            }

            if (networkResult != null) {
                _uiState.value = ProgressUiState(
                    isLoading = false,
                    overallProgress = networkResult.getString("overall_progress_percentage"),
                    levelFloat = networkResult.getJSONObject("levels").getDouble("progress_float").toFloat(),
                    levelText = networkResult.getJSONObject("levels").getString("progress_text"),
                    quizFloat = networkResult.getJSONObject("quizzes").getDouble("progress_float").toFloat(),
                    quizText = networkResult.getJSONObject("quizzes").getString("progress_text"),
                    screenTimeFloat = stFloat,
                    screenTimeText = stText
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    screenTimeFloat = stFloat,
                    screenTimeText = stText
                )
            }
        }
    }

    private fun fetchBackendStats(userId: String): JSONObject? {
        return try {
            val ip = NetworkConfig.SERVER_IP
            val targetUrl = URL("http://$ip/api/user_progress/$userId")

            val connection = targetUrl.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                JSONObject(response)
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}