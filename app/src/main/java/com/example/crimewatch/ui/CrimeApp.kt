// CrimeApp.kt
package com.example.crimewatch.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.crimewatch.R
import com.google.firebase.auth.FirebaseAuth

enum class CrimeAppScreen {
    SignIn,
    SignUp,
    Home
}

private val auth = FirebaseAuth.getInstance()

@Composable
fun CrimeApp(
    crimeViewModel: CrimeViewModel = viewModel(),
    navHostController: NavHostController = rememberNavController()
) {
    val webClientId = stringResource(id = R.string.default_web_client_id)

    // Observe FirebaseUser state from ViewModel
    val user by crimeViewModel.user.collectAsState()

    // Set initial user from FirebaseAuth once when this composable enters composition.
    LaunchedEffect(Unit) {
        crimeViewModel.setUser(auth.currentUser) // safe one-time init
    }

    // Use a single NavHost. Choose startDestination based on current user.
    val startDestination = if (user == null) CrimeAppScreen.SignIn.name else CrimeAppScreen.Home.name

    NavHost(navController = navHostController, startDestination = startDestination) {
        composable(CrimeAppScreen.SignIn.name) {
            // Pass webClientId and viewModel into SignInScreen
            SignInScreen(
                crimeViewModel = crimeViewModel,
                navHostController = navHostController,
                webClientId = webClientId
            )
        }

        composable(CrimeAppScreen.SignUp.name) {
            SignUpScreen(
                crimeViewModel = crimeViewModel,
                navHostController = navHostController,
                webClientId = webClientId
            )
        }

        composable(CrimeAppScreen.Home.name) {
            HomeScreen(
                crimeViewModel = crimeViewModel,
                navHostController = navHostController
            )
        }
    }

    // If user becomes non-null (e.g., after successful sign-in), navigate to Home and clear sign-in/up from backstack.
    LaunchedEffect(user) {
        if (user != null) {
            navHostController.navigate(CrimeAppScreen.Home.name) {
                popUpTo(CrimeAppScreen.SignIn.name) { inclusive = true }
            }
        }
    }
}
