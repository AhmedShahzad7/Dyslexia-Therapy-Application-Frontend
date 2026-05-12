package org.example.frontend.quizzes.quiz1

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.example.frontend.NetworkConfig
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class Quiz1ViewModel : ViewModel() {
    val quizQuestions = mutableStateListOf<QuizQuestion>()
    val isLoading = mutableStateOf(true)
    val errorMessage = mutableStateOf<String?>(null)

    val currentScreen = mutableStateOf(Quiz1Screen.Intro)
    val currentIndex = mutableIntStateOf(0)
    val correctAnswersCount = mutableIntStateOf(0)
    val quizProgress = mutableFloatStateOf(0.0f)

    // Tracks overall submission state during backend evaluation runs
    val isFinalizingEvaluation = mutableStateOf(false)

    // 1. INITIALIZE QUIZ SESSION (Fetches randomized sequence from Flask)
    fun initQuizSession() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            errorMessage.value = "User not logged in."
            isLoading.value = false
            return
        }

        isLoading.value = true
        errorMessage.value = null
        val ip = NetworkConfig.SERVER_IP

        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        val request = Request.Builder()
            .url("http://$ip/generate_quiz1?user_id=$uid")
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
                                val fetchedList = mutableListOf<QuizQuestion>()

                                for (i in 0 until jsonArray.length()) {
                                    val item = jsonArray.getJSONObject(i)
                                    fetchedList.add(
                                        QuizQuestion(
                                            dbQuestionNumber = item.getInt("db_question_number"),
                                            questionType = item.getString("question_type"),
                                            uiSlotAssigned = item.optInt("ui_slot_assigned", 1),
                                            targetWord = item.getString("target_word"),
                                            instructionText = item.getString("instruction_text"),
                                            audioUrl = if (item.isNull("audio_url")) null else item.getString("audio_url")
                                        )
                                    )
                                }

                                quizQuestions.clear()
                                quizQuestions.addAll(fetchedList)

                                currentIndex.intValue = 0
                                correctAnswersCount.intValue = 0
                                updateProgressMetric()

                                isLoading.value = false
                            } else {
                                errorMessage.value = json.optString("error", "Failed to load quiz session.")
                                isLoading.value = false
                            }
                        } catch (e: Exception) {
                            Log.e("Quiz1ViewModel", "JSON Parsing Error", e)
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

    fun getCurrentQuestion(): QuizQuestion? {
        return if (currentIndex.intValue < quizQuestions.size) {
            quizQuestions[currentIndex.intValue]
        } else null
    }

    // 2. INTERMEDIATE SUBMISSION (Saves drawing bytes locally and moves forward)
    fun submitAnswerWithPayload(imageBytes: ByteArray?) {
        getCurrentQuestion()?.capturedAnswerBytes = imageBytes

        currentIndex.intValue++
        updateProgressMetric()

        if (currentIndex.intValue >= quizQuestions.size) {
            finalizeQuizEvaluation()
        }
    }

    // 3. BATCH EVALUATION (Transmits all collected answers to Flask at summary stage)
    private fun finalizeQuizEvaluation() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        isFinalizingEvaluation.value = true
        currentScreen.value = Quiz1Screen.Summary

        val ip = NetworkConfig.SERVER_IP
        val client = OkHttpClient.Builder()
            .connectTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build()

        val multipartBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("user_id", uid)

        val metadataArray = JSONArray()

        quizQuestions.forEachIndexed { index, question ->
            val metaObject = JSONObject().apply {
                put("question_index", index)
                put("db_question_number", question.dbQuestionNumber)
                put("target_word", question.targetWord)
                put("question_type", question.questionType)
            }
            metadataArray.put(metaObject)

            // Attach user input files directly to the multipart request
            question.capturedAnswerBytes?.let { bytes ->
                multipartBuilder.addFormDataPart(
                    "file_$index",
                    "drawing_$index.png",
                    bytes.toRequestBody("image/png".toMediaTypeOrNull())
                )
            }
        }

        multipartBuilder.addFormDataPart("metadata", metadataArray.toString())

        val request = Request.Builder()
            .url("http://$ip/evaluate_quiz1_batch")
            .post(multipartBuilder.build())
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Handler(Looper.getMainLooper()).post {
                    errorMessage.value = "Failed to sync final assessment analytics."
                    isFinalizingEvaluation.value = false
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                Handler(Looper.getMainLooper()).post {
                    try {
                        val json = JSONObject(responseData ?: "")
                        if (json.optString("status") == "success") {
                            correctAnswersCount.intValue = json.getInt("final_score")
                            isFinalizingEvaluation.value = false
                        } else {
                            errorMessage.value = json.optString("error", "Evaluation engine faulted.")
                            isFinalizingEvaluation.value = false
                        }
                    } catch (e: Exception) {
                        Log.e("QuizFinalizer", "Parsing Output Exception", e)
                        errorMessage.value = "Analytics stream corrupt."
                        isFinalizingEvaluation.value = false
                    }
                }
            }
        })
    }

    private fun updateProgressMetric() {
        quizProgress.floatValue = if (quizQuestions.isNotEmpty()) {
            currentIndex.intValue.toFloat() / quizQuestions.size.toFloat()
        } else {
            0.0f
        }
    }

    fun navigateTo(screen: Quiz1Screen) {
        currentScreen.value = screen
    }
}

enum class Quiz1Screen {
    Intro, ActiveSession, Summary
}