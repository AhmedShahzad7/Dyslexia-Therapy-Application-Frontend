package org.example.frontend.therapy.level1

data class SessionQuestion(
    val dbQuestionNumber: Int,  // Maps back to Firestore document ID (1-5, or 99)
    val questionType: String,   // "DRAWING" or "MCQ"
    val uiSlotAssigned: Int,    // CRITICAL: Explicitly tracks the fixed UI stage (1, 2, 3, etc.)
    val targetWord: String,     // e.g., "Up", "Left"
    val instructionText: String,// e.g., "Click the direction of the given arrow"
    val audioUrl: String?,      // Pre-cached static URL
    val isGenuineError: Boolean = true // True if pulled from DB failures, false if filler
)