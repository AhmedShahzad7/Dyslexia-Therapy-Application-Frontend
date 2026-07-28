package org.example.frontend.quizzes.quiz1

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier

@Composable
fun Quiz1Router(
    viewModel: Quiz1ViewModel,
    onQuizComplete: (finalScore: Int, totalItems: Int) -> Unit
) {
    val currentScreenState = viewModel.currentScreen.value
    val activeQuestion = viewModel.getCurrentQuestion()
    val progressRatio = viewModel.quizProgress.floatValue

    Box(modifier = Modifier.fillMaxSize()) {
        when (currentScreenState) {
            Quiz1Screen.Intro -> {
                BouncyQuizIntroScreen(
                    viewModel = viewModel,
                    onIntroFinished = {
                        viewModel.navigateTo(Quiz1Screen.ActiveSession)
                    }
                )
            }

            Quiz1Screen.ActiveSession -> {
                if (activeQuestion != null) {
                    when (activeQuestion.uiSlotAssigned) {
                        1 -> {
                            QuizQuestionL1_Shell(
                                questionData = activeQuestion,
                                currentProgress = progressRatio,
                                questionNumber = viewModel.currentIndex.intValue + 1,
                                onAnswerSubmitted = { payloadBytes ->
                                    viewModel.submitAnswerWithPayload(payloadBytes)
                                }
                            )
                        }
                        2 -> {
                            QuizQuestionL2_Shell(
                                questionData = activeQuestion,
                                currentProgress = progressRatio,
                                questionNumber = viewModel.currentIndex.intValue + 1,
                                onAnswerSubmitted = { payloadBytes ->
                                    viewModel.submitAnswerWithPayload(payloadBytes)
                                }
                            )
                        }
                        3 -> {
                            QuizQuestionL3_Shell(
                                questionData = activeQuestion,
                                currentProgress = progressRatio,
                                questionNumber = viewModel.currentIndex.intValue + 1,
                                onAnswerSubmitted = { payloadBytes ->
                                    viewModel.submitAnswerWithPayload(payloadBytes)
                                }
                            )
                        }
                        4 -> {
                            QuizQuestionL4_Shell(
                                questionData = activeQuestion,
                                currentProgress = progressRatio,
                                questionNumber = viewModel.currentIndex.intValue + 1,
                                onAnswerSubmitted = { payloadBytes ->
                                    viewModel.submitAnswerWithPayload(payloadBytes)
                                }
                            )
                        }
                        else -> {
                            LaunchedEffect(activeQuestion) {
                                viewModel.submitAnswerWithPayload(null)
                            }
                        }
                    }
                } else {
                    LaunchedEffect(Unit) {
                        viewModel.navigateTo(Quiz1Screen.Summary)
                    }
                }
            }

            Quiz1Screen.Summary -> {
                LaunchedEffect(Unit) {
                    onQuizComplete(
                        viewModel.correctAnswersCount.intValue,
                        viewModel.quizQuestions.size
                    )
                }
            }
        }
    }
}