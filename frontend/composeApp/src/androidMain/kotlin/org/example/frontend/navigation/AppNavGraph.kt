package org.example.frontend.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
//import org.example.frontend.question1.Question1
//import org.example.frontend.question2.Question2
import org.example.frontend.LoginScreen.LoginScreen
import org.example.frontend.SignUpScreen.SignUpScreen
import org.example.frontend.homescreenpage.HomePage
import androidx.navigation.compose.dialog
import org.example.frontend.debugmenu.DebugMenu
import org.example.frontend.Question1.Question1
import org.example.frontend.Question2.Question2
import org.example.frontend.Question3.Question3
import org.example.frontend.Question4.Question4
import org.example.frontend.Levelselection.Levelselection
import org.example.frontend.AssesmentTest.Level1.Question1 as Alvl1Q1
import org.example.frontend.AssesmentTest.Level1.Question2 as Alvl1Q2
import org.example.frontend.AssesmentTest.Level1.Question3 as Alvl1Q3
import org.example.frontend.AssesmentTest.Level1.Question4 as Alvl1Q4
import org.example.frontend.AssesmentTest.Level1.Question5 as Alvl1Q5
import org.example.frontend.AssesmentTest.Level2.Question6 as Alvl2Q6
import org.example.frontend.AssesmentTest.Level2.Question7 as Alvl2Q7
import org.example.frontend.AssesmentTest.Level2.Question8 as Alvl2Q8
import org.example.frontend.AssesmentTest.Level2.Question9 as Alvl2Q9
import org.example.frontend.AssesmentTest.Level2.Question10 as Alvl2Q10
import org.example.frontend.AssesmentTest.Level3.Question11 as Alvl3Q11
import org.example.frontend.AssesmentTest.Level3.Question12 as Alvl3Q12
import org.example.frontend.AssesmentTest.Level3.Question13 as Alvl3Q13
import org.example.frontend.AssesmentTest.Level3.Question14 as Alvl3Q14
import org.example.frontend.AssesmentTest.Level3.Question15 as Alvl3Q15
import org.example.frontend.AssesmentTest.Level4.Question16 as Alvl4Q16
import org.example.frontend.AssesmentTest.Level4.Question17 as Alvl4Q17
import org.example.frontend.AssesmentTest.Level4.Question18 as Alvl4Q18
import org.example.frontend.AssesmentTest.Level4.Question19 as Alvl4Q19
import org.example.frontend.AssesmentTest.Level3.Question11 as Alvl3Q11
import org.example.frontend.cartoonselection.CartoonSelectionScreen
import org.example.frontend.progresstracking.ProgressTrackingScreen
import org.example.frontend.progresstracking.CommonErrorTestListScreen
//LEVEL 1
import org.example.frontend.therapy.level1.BouncyLevelScreen
import org.example.frontend.therapy.level1.TherapySessionRouter
import org.example.frontend.therapy.level1.TherapyViewModel
import androidx.lifecycle.viewmodel.compose.viewModel


//LEVEL 2
import org.example.frontend.therapy.level1.BouncyLevelScreen as Level1introScreen
import org.example.frontend.therapy.Level2.BouncyLevel2Screen as Level2introScreen
import org.example.frontend.therapy.Level2.QuestionL2Therapy as Level2Therapy

//LEVEL 3
import org.example.frontend.therapy.level3.BouncyLevel3Screen
import org.example.frontend.therapy.level3.QuestionL11
import org.example.frontend.therapy.level3.QuestionL12
import org.example.frontend.therapy.level3.QuestionL13
import org.example.frontend.therapy.level3.QuestionL14
import org.example.frontend.therapy.level3.QuestionL15


// LEVEL 4
import org.example.frontend.therapy.level4.BouncyLevelScreen as BouncyLevelScreen4
import org.example.frontend.therapy.level4.TherapySessionRouter4
import org.example.frontend.therapy.level4.TherapyViewModel4


///////QUIZZES

//QUIZ 1
import org.example.frontend.quizzes.quiz1.BouncyQuizIntroScreen
import org.example.frontend.quizzes.quiz1.Quiz1Router
import org.example.frontend.quizzes.quiz1.Quiz1ViewModel

//Quiz2
import org.example.frontend.therapy.Quiz2.QuestionL2Quiz as Quiz2

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.frontend.NetworkConfig
import org.example.frontend.R
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException


@Composable
fun AppNavGraph(startDestination: String = "Quiz1Router") {
    val navController = rememberNavController()
    val sharedTherapyViewModel: TherapyViewModel = viewModel()
    val sharedTherapyViewModel4: TherapyViewModel4 = viewModel()
    val sharedQuiz1ViewModel: Quiz1ViewModel = viewModel()
    NavHost(navController = navController, startDestination = startDestination) {
        composable("LoginScreen") {
            // pass navigation callback or navController to screen
            LoginScreen(
                navController = navController,onSignUpScreen = { navController.navigate("SignUpScreen") },
                onassessmentScreen={navController.navigate("Alvl1Q1")},
                onhomescreen={navController.navigate("HomePage")} //HomePage
                )


        }


        dialog("DebugMenu") {
            DebugMenu(
                onDismiss = { navController.popBackStack() } // Closes the popup
            )
        }
        composable("SignUpScreen") {
            SignUpScreen(
                onBack = { navController.popBackStack() },
                onNextScreen={navController.navigate("Question1")}
            )
        }
        composable("HomePage"){
            HomePage (
                onNavigateToProgress = {
                    navController.navigate("progress_tracking")
                },
                onNavigateToLevels = {
                    navController.navigate("Levelselection")
                }
            )
        }
        composable("Levelselection") {

            Levelselection(
                onNavigateHome = { navController.navigate("HomePage") },
                onNavigateToLevel = { levelNum ->
                    when (levelNum) {
                        1 -> navController.navigate("BouncyLevelScreen")
                        2 -> navController.navigate("Level2introScreen")
                        3 -> navController.navigate("TherapyBouncyLevel3")
                        4 -> navController.navigate("BouncyLevelScreen4")
                    }
                }
            )

        }
        composable("Question1") {
            // pass navigation callback or navController to screen
            Question1(onNextScreen = { navController.navigate("Question2") })
        }
        composable("Question2") {
            Question2(onNextScreen = { navController.navigate("Question3") })
        }
        composable("Question3") {
            Question3(onNextScreen = { navController.navigate("Question4") })
        }
        composable("Question4") {
            // Assuming Question4 handles navigation to the Assessment or Home next

            Question4(navController = navController)
        }
        composable("Alvl1Q1") {
            Alvl1Q1(onNextScreen = { navController.navigate("Alvl1Q2") }
            )
        }
        composable("Alvl1Q2") {
            Alvl1Q2(
                onNextScreen = { navController.navigate("Alvl1Q3") }
            )
        }
        composable("Alvl1Q3") {
            Alvl1Q3(
                onNextScreen = { navController.navigate("Alvl1Q4") }
            )
        }
        composable("Alvl1Q4") {
            Alvl1Q4(
                onNextScreen = { navController.navigate("Alvl1Q5") }
            )
        }
        composable("Alvl1Q5") {
            Alvl1Q5(onNextScreen = { navController.navigate("Alvl2Q6")})
        }

        composable("Alvl2Q6") {
            Alvl2Q6(onNextScreen = { navController.navigate("Alvl2Q7")})
        }
        composable("Alvl2Q7") {
            Alvl2Q7(onNextScreen = { navController.navigate("Alvl2Q8")})
        }
        composable("Alvl2Q8") {
            Alvl2Q8(onNextScreen = { navController.navigate("Alvl2Q9")})
        }
        composable("Alvl2Q9") {
            Alvl2Q9(onNextScreen = { navController.navigate("Alvl2Q10")})
        }
        composable("Alvl2Q10") {
            Alvl2Q10(onNextScreen = { navController.navigate("Alvl3Q11")})
        }
        composable("Alvl3Q11") {
            Alvl3Q11(onNextScreen = { navController.navigate("Alvl3Q12")})
        }
        composable("Alvl3Q12") {
            Alvl3Q12(onNextScreen = { navController.navigate("Alvl3Q13")})
        }
        composable("Alvl3Q13") {
            Alvl3Q13(onNextScreen = { navController.navigate("Alvl3Q14")})
        }
        composable("Alvl3Q14") {
            Alvl3Q14(onNextScreen = { navController.navigate("Alvl3Q15")})
        }
        composable("Alvl3Q15") {
            Alvl3Q15(onNextScreen = { navController.navigate("Alvl4Q16")})
        }
        composable("Alvl4Q16") {
            Alvl4Q16(onNextScreen = { navController.navigate("Alvl4Q17")})
        }
        composable("Alvl4Q17") {
            Alvl4Q17(onNextScreen = { navController.navigate("Alvl4Q18")})
        }
        composable("Alvl4Q18") {
            Alvl4Q18(onNextScreen = { navController.navigate("Alvl4Q19")})
        }
        composable("Alvl4Q19") {
            Alvl4Q19(onNextScreen = { navController.navigate("CartoonSelectionScreen")})
        }


        //LEVELS
        //LEVEL 1
        composable("BouncyLevelScreen") {
            BouncyLevelScreen(
                viewModel = sharedTherapyViewModel,
                onIntroFinished = { navController.navigate("TherapySessionRouter") }
            )
        }
        composable("TherapySessionRouter") {
            TherapySessionRouter(
                viewModel = sharedTherapyViewModel,
                // Triggers when getQuestionForIndex(currentIndex) returns null (all items mastered or completed)
                onSessionComplete = {
                    navController.navigate("HomePage") {
                        // Optional safety: pop the stack so pressing back doesn't reopen the session loop
                        popUpTo("HomePage") { inclusive = true }
                    }
                }
            )
        }
        //Level 2

        composable("Level2introScreen") {
            Level2introScreen(onNextScreen = { navController.navigate("Level2Therapy") })
        }
        composable("Level2Therapy") {
            Level2Therapy(onNextScreen = { navController.navigate("HomePage") })
        }

        //LEVEL 3
        composable("TherapyBouncyLevel3") {
            BouncyLevel3Screen()

            // Auto-navigate to Question 11 after 4 seconds to let the bouncy animation finish
            LaunchedEffect(Unit) {
                delay(4000)
                navController.navigate("QuestionL11") {
                    // Removes the intro screen from backstack so they don't go back to it
                    popUpTo("TherapyBouncyLevel3") { inclusive = true }
                }
            }
        }

        composable("QuestionL11") {
            QuestionL11(onNextScreen = { navController.navigate("QuestionL12") })
        }

        composable("QuestionL12") {
            QuestionL12(onNextScreen = { navController.navigate("QuestionL13") })
        }

        composable("QuestionL13") {
            QuestionL13(onNextScreen = { navController.navigate("QuestionL14") })
        }

        composable("QuestionL14") {
            QuestionL14(onNextScreen = { navController.navigate("QuestionL15") })
        }

        composable("QuestionL15") {
            QuestionL15(onNextScreen = {
                // Navigate back to HomePage or CartoonSelectionScreen when Therapy Level 3 is finished
                navController.navigate("HomePage") {
                    popUpTo("HomePage") { inclusive = true }
                }
            })
        }


        //LEVEL 4
        composable("BouncyLevelScreen4") {
            BouncyLevelScreen4(
                viewModel = sharedTherapyViewModel4,
                onIntroFinished = { navController.navigate("TherapySessionRouter4") }
            )
        }
        composable("TherapySessionRouter4") {
            TherapySessionRouter4(
                viewModel = sharedTherapyViewModel4,
                onSessionComplete = {
                    navController.navigate("HomePage") {
                        popUpTo("HomePage") { inclusive = true }
                    }
                }
            )
        }
        // ==========================================
        // QUIZZES INTEGRATION
        // ==========================================

        composable("Quiz1Router") {
            // Because Quiz1Router internally renders its BouncyIntro based on Quiz1Screen.Intro state,
            // we mount the entire container interface here directly.
            Quiz1Router(
                viewModel = sharedQuiz1ViewModel,
                onQuizComplete = { score, total ->
                    // Optionally log score/total to your database analytics before navigating back
                    Log.d("QuizCompletion", "Finished Quiz 1 with score: $score / $total")
                    navController.navigate("HomePage") {
                        popUpTo("HomePage") { inclusive = true }
                    }
                }
            )
        }
        //Quiz2
        //Quiz2
        composable("Quiz2") {
            Quiz2(onNextScreen = { navController.navigate("HomePage") })
        }
        //////////////////////////////////////////////////


        composable("CartoonSelectionScreen") {
            CartoonSelectionScreen(onNextScreen = { navController.navigate("HomePage") })
        }
        composable("progress_tracking") {
            ProgressTrackingScreen(
                onHomeClick = {
                    navController.navigate("HomePage") {
                        popUpTo("HomePage") { inclusive = true }
                    }
                },
                onNavigateToErrorList = {
                    navController.navigate("error_list") // Route to the new screen
                }
            )
        }

        composable("error_list") {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                val userId = currentUser.uid
                CommonErrorTestListScreen(
                    userId = userId,
                    onHomeClick = {
                        navController.navigate("HomePage") {
                            popUpTo("HomePage") { inclusive = true }
                        }
                    }
                )
            }


        }

    }
}
