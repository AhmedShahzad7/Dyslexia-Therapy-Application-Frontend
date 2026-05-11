package org.example.frontend.therapy.level4

// Represents an individual dynamic mini-question pairing
data class MiniQuestionTarget(
    val word: String,
    val sentence: String
)

data class SessionQuestion4(
    val dbQuestionNumber: Int,
    val questionType: String,
    val uiSlotAssigned: Int,
    val targetLetter: String, // Explicitly retained for network mapping and UI cues
    val miniQuestions: List<MiniQuestionTarget>, // Replaces flat targetSentences and static targetWord
    val instructionText: String,
    val audioUrl: String?
)