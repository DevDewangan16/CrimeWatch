// SignUpScreen.kt
package com.example.crimewatch.ui

import android.content.Intent
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.crimewatch.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException

@Composable
fun SignUpScreen(
    crimeViewModel: CrimeViewModel,
    navHostController: NavHostController,
    webClientId: String // pass from stringResource(R.string.default_web_client_id)
) {
    val context = LocalContext.current
    val authState by crimeViewModel.authState.collectAsState()

    // Use the helper from GoogleSignInUtils.kt
    val googleClient = remember { getGoogleSignInClient(context, webClientId) }

    // Launcher for the Google Sign-In activity result
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { activityResult ->
        val data: Intent? = activityResult.data
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (idToken != null) {
                crimeViewModel.firebaseAuthWithGoogle(idToken) { success, _ ->
                    if (success) {
                        navHostController.navigate(CrimeAppScreen.Home.name) {
                            popUpTo(CrimeAppScreen.SignUp.name) { inclusive = true }
                        }
                    }
                }
            } else {
                // handle missing token if desired
            }
        } catch (e: ApiException) {
            // handle error (log or set viewModel error)
        }
    }

    LaunchedEffect(authState.uid) {
        if (!authState.uid.isNullOrEmpty()) {
            navHostController.navigate(CrimeAppScreen.Home.name) {
                popUpTo(CrimeAppScreen.SignUp.name) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xffE53131))
    ) {

        // Header Background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .size(320.dp)
                .background(
                    color = Color(0xffBF1F1F),
                    shape = RoundedCornerShape(bottomStart = 90.dp, bottomEnd = 90.dp)
                )
        )

        // Main Card
        Card(
            modifier = Modifier
                .padding(horizontal = 28.dp)
                .padding(top = 150.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {

            Column(
                modifier = Modifier
                    .padding(22.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {

                // Logo
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo",
                    modifier = Modifier.size(180.dp),
                    contentScale = ContentScale.Fit
                )

                Text(
                    text = "Create Account",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xffE53131),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Join CrimeWatch and stay informed.",
                    fontSize = 16.sp,
                    color = Color(0xFF4A4A4A),
                    textAlign = TextAlign.Center
                )

                OutlinedButton(
                    onClick = {
                        // Launch the intent from the GoogleSignInClient instance
                        val signInIntent = googleClient.signInIntent
                        launcher.launch(signInIntent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, Color(0xffE53131))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.google_icon),
                        contentDescription = "Google Icon",
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Continue with Google",
                        fontSize = 18.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                if (authState.isLoading) {
                    CircularProgressIndicator()
                }

                authState.error?.let { err ->
                    Text(
                        text = err,
                        fontSize = 14.sp,
                        color = Color.Red,
                        textAlign = TextAlign.Center
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Already have an account?",
                        fontSize = 16.sp,
                        color = Color(0xFF6C6C6C)
                    )

                    Text(
                        text = "Sign In",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xffE53131),
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clickable {
                                navHostController.navigate(CrimeAppScreen.SignIn.name)
                            }
                    )
                }
            }
        }
    }
}