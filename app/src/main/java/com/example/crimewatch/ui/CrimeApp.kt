package com.example.crimewatch.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.crimewatch.R

enum class CrimeAppScreen(){
    SignIn,
    SignUp,
    Home
}

@Composable
fun CrimeApp(
    crimeViewModel: CrimeViewModel= viewModel(),
    navHostController: NavHostController= rememberNavController()
) {
    val webClientId = stringResource(id = R.string.default_web_client_id)

    NavHost(navController = navHostController, startDestination = CrimeAppScreen.SignIn.name) {
        composable(route = CrimeAppScreen.SignIn.name){
            SignInScreen(crimeViewModel = crimeViewModel, navHostController = navHostController,webClientId = webClientId)
        }
        composable(route = CrimeAppScreen.SignUp.name){
            SignUpScreen(crimeViewModel = crimeViewModel, navHostController = navHostController,webClientId = webClientId
            )
        }
        composable(route=CrimeAppScreen.Home.name){
            HomeScreen(crimeViewModel = crimeViewModel, navHostController = navHostController)
        }
    }
}