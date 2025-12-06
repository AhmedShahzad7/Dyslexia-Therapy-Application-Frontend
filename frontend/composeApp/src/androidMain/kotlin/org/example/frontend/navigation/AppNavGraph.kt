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
@Composable
fun AppNavGraph(startDestination: String = "LoginScreen") {
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
        // add more composable routes as needed
    }
}
