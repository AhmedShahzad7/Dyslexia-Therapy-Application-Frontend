package org.example.frontend.therapy.level1

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun TherapySessionRouter(
    viewModel: TherapyViewModel,
    onSessionComplete: () -> Unit // Called when all surviving errors are answered
) {
    // Tracks our current progression through the shuffled session array
    var currentIndex by remember { mutableStateOf(0) }
    val currentItem = viewModel.getQuestionForIndex(currentIndex)

    // Read the dynamically mapped character resource ID directly from the ViewModel state
    val helperCartoonResId = viewModel.cartoonResId.value

    // 1. GRADUATION / COMPLETION CHECK
    if (currentItem == null) {
        LaunchedEffect(Unit) {
            onSessionComplete()
        }
        return
    }

    // 2. DYNAMIC TRAFFIC COP
    // Renders the appropriate UI shell based on the assigned visual stage
    Box(modifier = Modifier.fillMaxSize()) {
        when (currentItem.questionType) {
            "DRAWING" -> {
                // Mounts the Canvas Drawing Shell
                QuestionL1_Shell(
                    sessionItem = currentItem,
                    uiSequenceNumber = currentIndex + 1,
                    cartoonResId = helperCartoonResId, // Passed downstream cleanly
                    onNext = { currentIndex++ }
                )
            }
            "MCQ" -> {
                when (currentItem.uiSlotAssigned) {
                    3 -> {
                        // Mounts the 2-Button Rotated Arrow Shell (Visual Stage 3)
                        QuestionL3_Shell(
                            sessionItem = currentItem,
                            uiSequenceNumber = currentIndex + 1,
                            cartoonResId = helperCartoonResId, // Passed downstream cleanly
                            onNext = { currentIndex++ }
                        )
                    }
                    4 -> {
                        // Mounts the 4-Arrow Matching Shell (Visual Stage 4)
                        QuestionL4_Shell(
                            sessionItem = currentItem,
                            uiSequenceNumber = currentIndex + 1,
                            cartoonResId = helperCartoonResId, // Passed downstream cleanly
                            onNext = { currentIndex++ }
                        )
                    }
                    else -> {
                        // Mounts the 8-Option Grid Selection Shell (Visual Stage 2)
                        QuestionL2_Shell(
                            sessionItem = currentItem,
                            uiSequenceNumber = currentIndex + 1,
                            cartoonResId = helperCartoonResId, // Passed downstream cleanly
                            onNext = { currentIndex++ }
                        )
                    }
                }
            }
            else -> {
                // Failsafe fallback: skip unrecognized types safely
                LaunchedEffect(Unit) { currentIndex++ }
            }
        }
    }
}