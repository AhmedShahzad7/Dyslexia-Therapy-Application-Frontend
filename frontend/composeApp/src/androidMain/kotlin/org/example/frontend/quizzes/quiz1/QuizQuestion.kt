package org.example.frontend.quizzes.quiz1

data class QuizQuestion(
    val dbQuestionNumber: Int,
    val questionType: String,
    val uiSlotAssigned: Int,
    val targetWord: String,
    val instructionText: String,
    val audioUrl: String?,
    // ---> ADDED: Buffer to hold user's binary image array locally <---
    var capturedAnswerBytes: ByteArray? = null
)