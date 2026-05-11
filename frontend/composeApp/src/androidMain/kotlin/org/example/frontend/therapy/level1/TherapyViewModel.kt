package org.example.frontend.therapy.level1

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import okhttp3.*
import org.example.frontend.NetworkConfig
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class TherapyViewModel : ViewModel() {
    // Observable Compose States
    val sessionQuestions = mutableStateListOf<SessionQuestion>()
    val isLoading = mutableStateOf(true)
    val errorMessage = mutableStateOf<String?>(null)

    fun initSession() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            errorMessage.value = "User not logged in."
            isLoading.value = false
            return
        }

        isLoading.value = true
        errorMessage.value = null
        val ip = NetworkConfig.SERVER_IP

        // Extended timeouts prevent Android from dropping connections prematurely
        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        val request = Request.Builder()
            .url("http://$ip/init_level_session?user_id=$uid")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Handler(Looper.getMainLooper()).post {
                    errorMessage.value = "Network timeout or connection error."
                    isLoading.value = false
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                Handler(Looper.getMainLooper()).post {
                    if (responseData != null) {
                        try {
                            val json = JSONObject(responseData)
                            if (json.optString("status") == "success") {
                                val jsonArray = json.getJSONArray("questions")
                                val fetchedList = mutableListOf<SessionQuestion>()

                                for (i in 0 until jsonArray.length()) {
                                    val item = jsonArray.getJSONObject(i)
                                    fetchedList.add(
                                        SessionQuestion(
                                            dbQuestionNumber = item.getInt("db_question_number"),
                                            questionType = item.getString("question_type"),

                                            // ---> CRITICAL PARSING ADDITION <---
                                            // Reads the assigned UI slot safely. Defaults to 1 if missing.
                                            uiSlotAssigned = item.optInt("ui_slot_assigned", 1),

                                            targetWord = item.getString("target_word"),
                                            instructionText = item.getString("instruction_text"),
                                            audioUrl = if (item.isNull("audio_url")) null else item.getString("audio_url"),

                                            // Securely tracks whether this item writes to analytics counters
                                            isGenuineError = item.optBoolean("is_genuine_error", true)
                                        )
                                    )
                                }

                                sessionQuestions.clear()
                                sessionQuestions.addAll(fetchedList)
                                isLoading.value = false
                            } else {
                                errorMessage.value = json.optString("error", "Failed to load session.")
                                isLoading.value = false
                            }
                        } catch (e: Exception) {
                            Log.e("TherapyViewModel", "JSON Parsing Error", e)
                            errorMessage.value = "Data parsing error."
                            isLoading.value = false
                        }
                    } else {
                        errorMessage.value = "Empty response from server."
                        isLoading.value = false
                    }
                }
            }
        })
    }

    // Helper to pull the specific question object for a sequence index
    fun getQuestionForIndex(arrayIndex: Int): SessionQuestion? {
        return if (arrayIndex < sessionQuestions.size) sessionQuestions[arrayIndex] else null
    }
}