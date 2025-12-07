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

@Composable
fun AppNavGraph(startDestination: String = "HomeScreen") {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {
        composable("LoginScreen") {
            // pass navigation callback or navController to screen
            LoginScreen(
                onSignUpScreen = { navController.navigate("SignUpScreen") },
                onHomeScreen={navController.navigate("HomeScreen")},

            )
        }
        composable("SignUpScreen") {
            SignUpScreen(
                onBack = { navController.popBackStack() }
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
            Question2(onBack = { navController.popBackStack() })
        }
    }
}
