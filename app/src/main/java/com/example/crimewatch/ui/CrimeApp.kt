// CrimeApp.kt - App Bars Section
package com.example.crimewatch.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
    reportsViewModel: ReportsViewModel= viewModel(),
    crimeViewModel: CrimeViewModel = viewModel(),
    navHostController: NavHostController = rememberNavController()
) {
    val webClientId = stringResource(id = R.string.default_web_client_id)

    val user by crimeViewModel.user.collectAsState()
    val logoutClicked by crimeViewModel.logoutClicked.collectAsState()

    LaunchedEffect(Unit) {
        crimeViewModel.setUser(auth.currentUser)
    }

    val backStackEntry by navHostController.currentBackStackEntryAsState()
    val rawRoute = backStackEntry?.destination?.route ?: CrimeAppScreen.Home.name
    val baseRoute = rawRoute.substringBefore("/")

    val currentScreen = try {
        when (baseRoute) {
            "ReportDetails" -> CrimeAppScreen.Report
            else -> CrimeAppScreen.valueOf(baseRoute)
        }
    } catch (e: Exception) {
        CrimeAppScreen.Home
    }

    canNavigateBack=navHostController.previousBackStackEntry != null

    val startDestination = if (user == null) CrimeAppScreen.SignIn.name else CrimeAppScreen.Home.name

    Scaffold(
        topBar = {
            if (user != null) {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // App Title
                            Text(
                                text = currentScreen.title,
                                fontSize = 22.sp,
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1A1A)
                            )

                            // Logout Button
                            Surface(
                                modifier = Modifier.clickable {
                                    crimeViewModel.setLogoutStatus(true)
                                },
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFFFFF3E0)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ExitToApp,
                                        contentDescription = "Logout",
                                        modifier = Modifier.size(18.dp),
                                        tint = Color(0xFFE65100)
                                    )
                                    Text(
                                        text = "Logout",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFE65100)
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White
                    )
                )
            }
        },
        bottomBar = {
            if (user != null) {
                BottomAppBar(
                    navHostController = navHostController,
                    currentScreen = currentScreen
                )
            }
        },
        containerColor = Color(0xFFF5F5F5)
    ) {
        NavHost(navController = navHostController, startDestination = startDestination) {
            composable(CrimeAppScreen.SignIn.name) {
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
                    reportsViewModel=reportsViewModel,navHostController
                )
            }
            composable(route = CrimeAppScreen.Report.name){
                ReportScreen(crimeViewModel = crimeViewModel, navHostController = navHostController)
            }
            composable(
                route = "ReportDetails/{reportId}",
                arguments = listOf(
                    navArgument("reportId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("reportId") ?: return@composable
                IncidentDetailScreen(
                    reportId = id,
                    reportsViewModel,
                    navHostController = navHostController
                )
            }

            composable(route = CrimeAppScreen.MyReport.name) {
                MyReportScreen(crimeViewModel = crimeViewModel, navHostController = navHostController)
            }
        }

        if (logoutClicked) {
            AlertCheck(
                onYesButtonPressed = {
                    crimeViewModel.setLogoutStatus(false)
                    crimeViewModel.signOut()
                },
                onNoButtonPressed = {
                    crimeViewModel.setLogoutStatus(false)
                }
            )
        }

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
    currentScreen: CrimeAppScreen
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            // Home
            BottomNavItem(
                icon = Icons.Outlined.Home,
                label = "Home",
                isSelected = currentScreen == CrimeAppScreen.Home,
                onClick = {
                    navHostController.navigate(CrimeAppScreen.Home.name) {
                        popUpTo(0)
                    }
                }
            )

            // Report (Center - Emphasized)
            Surface(
                modifier = Modifier
                    .size(60.dp)
                    .clickable {
                        navHostController.navigate(CrimeAppScreen.Report.name) {
                            popUpTo(0)
                        }
                    },
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF2196F3),
                shadowElevation = 4.dp
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = "Report",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // My Reports
            BottomNavItem(
                icon = Icons.Outlined.List,
                label = "My Reports",
                isSelected = currentScreen == CrimeAppScreen.MyReport,
                onClick = {
                    navHostController.navigate(CrimeAppScreen.MyReport.name) {
                        popUpTo(0)
                    }
                }
            )
        }
    }
}

@Composable
fun BottomNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) Color(0xFF2196F3) else Color(0xFF999999),
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) Color(0xFF2196F3) else Color(0xFF999999)
        )
    }
}

@Composable
fun AlertCheck(
    onYesButtonPressed: () -> Unit,
    onNoButtonPressed: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onNoButtonPressed() },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.ExitToApp,
                    contentDescription = null,
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "Logout",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFF1A1A1A)
                )
            }
        },
        text = {
            Text(
                text = "Are you sure you want to logout from your account?",
                fontSize = 16.sp,
                color = Color(0xFF666666),
                lineHeight = 22.sp
            )
        },
        confirmButton = {
            Button(
                onClick = { onYesButtonPressed() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE53935)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Yes, Logout",
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onNoButtonPressed() },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFF666666)
                )
            ) {
                Text(
                    text = "Cancel",
                    fontWeight = FontWeight.Medium
                )
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}