package org.example.frontend.therapy.level4

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import okhttp3.*
import org.example.frontend.NetworkConfig
import org.example.frontend.R // Ensure R is imported for resource resolution
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class TherapyViewModel4 : ViewModel() {
    val sessionQuestions = mutableStateListOf<SessionQuestion4>()
    val isLoading = mutableStateOf(true)
    val errorMessage = mutableStateOf<String?>(null)

    // ---> NEW: Dynamic Cartoon State (Defaults to a stable fallback) <---
    val cartoonResId = mutableStateOf(R.drawable.mickey1)

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

        // 60-second timeouts are crucial here to accommodate subsequent audio file uploads
        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        val request = Request.Builder()
            .url("http://$ip/init_level4_session?user_id=$uid")
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
                                // ---> NEW: Safely parse cartoon choice from backend payload <---
                                val selectedCartoonStr = json.optString("cartoon_selection", "mickey")
                                cartoonResId.value = mapCartoonStringToDrawable(selectedCartoonStr)

                                val jsonArray = json.getJSONArray("questions")
                                val fetchedList = mutableListOf<SessionQuestion4>()

                                for (i in 0 until jsonArray.length()) {
                                    val item = jsonArray.getJSONObject(i)
                                    fetchedList.add(
                                        SessionQuestion4(
                                            dbQuestionNumber = item.getInt("db_question_number"),
                                            questionType = item.getString("question_type"),
                                            uiSlotAssigned = item.optInt("ui_slot_assigned", 1),
                                            targetLetter = item.optString("target_letter", "b"),

                                            // Explicitly map paired mini-questions
                                            miniQuestions = if (item.has("mini_questions")) {
                                                val qArray = item.getJSONArray("mini_questions")
                                                List(qArray.length()) { idx ->
                                                    val obj = qArray.getJSONObject(idx)
                                                    MiniQuestionTarget(
                                                        word = obj.getString("word"),
                                                        sentence = obj.getString("sentence")
                                                    )
                                                }
                                            } else {
                                                listOf(MiniQuestionTarget("Fast", "He ran fast."))
                                            },

                                            instructionText = item.getString("instruction_text"),
                                            audioUrl = if (item.isNull("audio_url")) null else item.getString("audio_url")
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
                            Log.e("TherapyViewModel4", "JSON Parsing Error", e)
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

    // ---> NEW: Type-Safe Resource Mapper <---
    private fun mapCartoonStringToDrawable(cartoon: String): Int {
        return when (cartoon.lowercase().trim()) {
            "mickey" -> R.drawable.mickey1
            "pooh" -> R.drawable.pooh1
            "tom" -> R.drawable.tom1
            "duffy" -> R.drawable.duffy2
            else -> R.drawable.mickey1
        }
    }

    fun getQuestionForIndex(arrayIndex: Int): SessionQuestion4? {
        return if (arrayIndex < sessionQuestions.size) sessionQuestions[arrayIndex] else null
    }
}