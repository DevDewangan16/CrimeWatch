// CrimeApp.kt
package com.example.crimewatch.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.List
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.crimewatch.R
import com.google.firebase.auth.FirebaseAuth

enum class CrimeAppScreen(val title:String) {
    SignIn("SignIn"),
    SignUp("SignUp"),
    Home("CrimeWatch"),
    Report("Report an Incident"),
    MyReport("My Report")
}

var canNavigateBack=false
private val auth = FirebaseAuth.getInstance()

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrimeApp(
    crimeViewModel: CrimeViewModel = viewModel(),
    navHostController: NavHostController = rememberNavController()
) {
    val webClientId = stringResource(id = R.string.default_web_client_id)

    // Observe FirebaseUser state from ViewModel
    val user by crimeViewModel.user.collectAsState()

    val logoutClicked by crimeViewModel.logoutClicked.collectAsState()


    // Set initial user from FirebaseAuth once when this composable enters composition.
    LaunchedEffect(Unit) {
        crimeViewModel.setUser(auth.currentUser) // safe one-time init
    }

    val backStackEntry by navHostController.currentBackStackEntryAsState()
    val currentScreen =CrimeAppScreen.valueOf(
        backStackEntry?.destination?.route?:CrimeAppScreen.Home.name
    )
    canNavigateBack=navHostController.previousBackStackEntry != null


    // Use a single NavHost. Choose startDestination based on current user.
    val startDestination = if (user == null) CrimeAppScreen.SignIn.name else CrimeAppScreen.Home.name

    Scaffold(
        topBar ={
            if (user !=null){
                TopAppBar(title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = currentScreen.title,
                                fontSize = 26.sp,
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                                )
                        }
                        Row(modifier = Modifier.clickable {
                            crimeViewModel.setLogoutStatus(true)
                        }) {
                            Icon(painter = painterResource(id = R.drawable.logout), contentDescription ="Logout",
                                modifier = Modifier.size(24.dp))
                            Text(text = "Logout",
                                fontSize = 18.sp,
                                modifier = Modifier.padding(
                                    end = 14.dp,
                                    start = 4.dp
                                ))
                        }
                    }
                })
            }
        },
        bottomBar = {
            if (user != null){
                BottomAppBar(navHostController = navHostController, currentScreen = currentScreen)
            }
        }
    ) {
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

        if (logoutClicked){
            AlertCheck(onYesButtonPressed = {
                crimeViewModel.signOut()
            },
                onNoButtonPressed = {
                    crimeViewModel.setLogoutStatus(false)
                }
            )
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
}

@Composable
fun BottomAppBar(
    navHostController: NavHostController,
    currentScreen:CrimeAppScreen
){
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 70.dp, vertical = 10.dp)    ) {

        Column(horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
            ,    modifier = Modifier.clickable {
                navHostController.navigate(CrimeAppScreen.Home.name){
                    popUpTo(0)
                }
            }) {
            Icon(imageVector = Icons.Outlined.Home, contentDescription ="Home" )
            Text(text = "Home", fontSize = 10.sp)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
            ,    modifier = Modifier.clickable {
                navHostController.navigate(CrimeAppScreen.Report.name){
                    popUpTo(0)
                }
            }) {
            Icon(imageVector = Icons.Outlined.Add, contentDescription ="Add" )
            Text(text = "Report", fontSize = 10.sp)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
            ,    modifier = Modifier.clickable {
                navHostController.navigate(CrimeAppScreen.MyReport.name){
                    popUpTo(0)
                }
            }) {
            Icon(imageVector = Icons.Outlined.List, contentDescription ="My Report" )
            Text(text = "My Report", fontSize = 10.sp)
        }
    }
}

@Composable
fun AlertCheck(
    onYesButtonPressed:()->Unit,
    onNoButtonPressed:()->Unit

){
    AlertDialog(
        title = {
            Text(text = "Logout?", fontWeight = FontWeight.Bold)
        },
        containerColor = Color.White,
        text = {
            Text(text = "Are you sure you want to Logout")
        },
        confirmButton = {
            TextButton(onClick = {
                onYesButtonPressed()
            }) {
                Text(text = "Yes")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onNoButtonPressed()
            }) {
                Text(text = "No")
            }
        },
        onDismissRequest = {
            onNoButtonPressed()
        }
    )
}