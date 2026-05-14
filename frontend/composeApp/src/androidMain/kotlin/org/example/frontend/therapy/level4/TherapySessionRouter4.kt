package org.example.frontend.therapy.level4

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun TherapySessionRouter4(
    viewModel: TherapyViewModel4,
    onSessionComplete: () -> Unit
) {
    var currentIndex by remember { mutableStateOf(0) }

    // 1. Safely extract the active question payload
    val currentItem = viewModel.getQuestionForIndex(currentIndex)

    // ---> CRITICAL FIX: Read the globally resolved helper ID directly from ViewModel state <---
    val helperCartoonResId = viewModel.cartoonResId.value

    // 2. Completion Check
    if (currentItem == null) {
        LaunchedEffect(Unit) {
            onSessionComplete()
        } // Removed the stray 'a' typo here
        return
    }

    // 3. Dynamic Traffic Cop for Level 4 Interface Shells
    Box(modifier = Modifier.fillMaxSize()) {
        // We use 'currentItem' consistently instead of the undefined 'question' reference
        when (currentItem.questionType) {
            "VOICE" -> {
                // Mounts the Q1 Read Aloud Voice Shell
                QuestionL4_Q1_VoiceShell(
                    sessionItem = currentItem,
                    uiSequenceNumber = currentIndex + 1,
                    cartoonResId = helperCartoonResId, // Passes your type-safe mapped int downstream
                    onNext = { currentIndex++ }
                )
            }
            "WRITING" -> {
                // Mounts the Q2 Handwriting Practice Shell
                QuestionL4_Q2_WritingShell(
                    sessionItem = currentItem,
                    uiSequenceNumber = currentIndex + 1,
                    cartoonResId = helperCartoonResId, // Inject dynamic cartoon helper ID
                    onNext = { currentIndex++ }
                )
            }
            "GRID_SELECT" -> {
                // Mounts the Q3 Visual Matrix Identification Shell
                QuestionL4_Q3_GridShell(
                    sessionItem = currentItem,
                    uiSequenceNumber = currentIndex + 1,
                    cartoonResId = helperCartoonResId, // Inject dynamic cartoon helper ID
                    onNext = { currentIndex++ }
                )
            }
            "COMBO" -> {
                // ---> CRITICAL FIX: Mounts the final integrated Combo verification shell <---
                QuestionL4_Q4_ComboShell(
                    sessionItem = currentItem,
                    uiSequenceNumber = currentIndex + 1,
                    cartoonResId = helperCartoonResId, // Inject dynamic cartoon helper ID
                    onNext = { currentIndex++ }
                )
            }
            else -> {
                // Failsafe execution blocks unrecognized payloads safely to avoid lockups
                LaunchedEffect(Unit) { currentIndex++ }
            }
        }
    }
}