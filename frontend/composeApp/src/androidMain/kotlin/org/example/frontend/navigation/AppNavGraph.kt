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
import org.example.frontend.HomeScreen.HomeScreen
import org.example.frontend.Question1.Question1
import org.example.frontend.Question2.Question2
import org.example.frontend.Question3.Question3
import org.example.frontend.Question4.Question4

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
import org.example.frontend.AssesmentTest.Level4.Question16 as Alvl4Q16
import org.example.frontend.AssesmentTest.Level4.Question17 as Alvl4Q17
import org.example.frontend.AssesmentTest.Level4.Question18 as Alvl4Q18
import org.example.frontend.AssesmentTest.Level4.Question19 as Alvl4Q19
@Composable
fun AppNavGraph(startDestination: String = "Alvl1Q3") {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {
        composable("LoginScreen") {
            // pass navigation callback or navController to screen
            LoginScreen(
                navController = navController,onSignUpScreen = { navController.navigate("SignUpScreen") }
                )


        }
        composable("SignUpScreen") {
            SignUpScreen(
                onBack = { navController.popBackStack() },
                onNextScreen={navController.navigate("Question1")}
            )
        }
        composable("HomeScreen") {
            HomeScreen()
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
            Alvl1Q5()
        }
        composable("Alvl2Q6") {
            Alvl2Q6()
        }
        composable("Alvl2Q7") {
            Alvl2Q7()
        }
        composable("Alvl2Q8") {
            Alvl2Q8()
        }
        composable("Alvl2Q9") {
            Alvl2Q9()
        }
        composable("Alvl2Q10") {
            Alvl2Q10()
        }
        composable("Alvl4Q16") {
            Alvl4Q16()
        }
        composable("Alvl4Q17") {
            Alvl4Q17()
        }
        composable("Alvl4Q18") {
            Alvl4Q18()
        }
        composable("Alvl4Q19") {
            Alvl4Q19()
        }

    }
}
