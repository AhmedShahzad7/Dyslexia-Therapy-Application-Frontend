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
    val currentItem = viewModel.getQuestionForIndex(currentIndex)

    // 1. Completion Check
    if (currentItem == null) {
        LaunchedEffect(Unit) {
            onSessionComplete()
        }
        return
    }

    // 2. Dynamic Traffic Cop for Level 4
    Box(modifier = Modifier.fillMaxSize()) {
        when (currentItem.questionType) {
            "VOICE" -> {
                // Mounts the Q1 Read Aloud Voice Shell
                QuestionL4_Q1_VoiceShell(
                    sessionItem = currentItem,
                    uiSequenceNumber = currentIndex + 1,
                    onNext = { currentIndex++ }
                )
            }
            "WRITING" -> {
                QuestionL4_Q2_WritingShell(
                    sessionItem = currentItem,
                    uiSequenceNumber = currentIndex + 1,
                    onNext = { currentIndex++ }
                )
            }
            "GRID_SELECT" -> {
                QuestionL4_Q3_GridShell(
                    sessionItem = currentItem,
                    uiSequenceNumber = currentIndex + 1,
                    onNext = { currentIndex++ }
                )

            }
            "COMBO" -> {
                // ---> CRITICAL FIX: Fully mounts the integrated Combo verification shell <---
                QuestionL4_Q4_ComboShell(
                    sessionItem = currentItem,
                    uiSequenceNumber = currentIndex + 1,
                    onNext = { currentIndex++ }
                )
            }
            else -> {
                LaunchedEffect(Unit) { currentIndex++ }
            }
        }
    }
}

