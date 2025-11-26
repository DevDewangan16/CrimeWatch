package com.example.crimewatch.ui

import android.content.Intent
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
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.crimewatch.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException

@Composable
fun SignInScreen(
    crimeViewModel: CrimeViewModel,
    navHostController: NavHostController,
    webClientId: String
) {
    val context = LocalContext.current
    val authState by crimeViewModel.authState.collectAsState()
    val googleClient = getGoogleSignInClient(context, webClientId)

    // launcher for Google Sign In Activity
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { activityResult ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(activityResult.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (idToken != null) {
                // send ID token to ViewModel to sign in with Firebase
                crimeViewModel.firebaseAuthWithGoogle(idToken) { success, error ->
                    if (success) {
                        // navigate to home on successful sign in
                        navHostController.navigate(CrimeAppScreen.Home.name) {
                            popUpTo(CrimeAppScreen.SignIn.name) { inclusive = true }
                        }
                    } else {
                        // Optionally show error (authState will also hold it)
                    }
                }
            } else {
                // handle missing idToken
            }
        } catch (e: ApiException) {
            // handle error (e.statusCode)
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xffE53131))
    ) {

        // Curved top decorative background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .size(350.dp)
                .background(
                    color = Color(0xffBF1F1F),
                    shape = RoundedCornerShape(bottomStart = 90.dp, bottomEnd = 90.dp)
                )
        )

        // Main Card Content
        Card(
            modifier = Modifier
                .padding(horizontal = 28.dp)
                .padding(top = 180.dp)
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
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {

                // Logo
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo",
                    modifier = Modifier.size(180.dp),
                    contentScale = ContentScale.Fit
                )

                // Title
                Text(
                    text = "Welcome to CrimeWatch",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xffE53131),
                    textAlign = TextAlign.Center
                )

                // Subtitle
                Text(
                    text = "Stay informed.\nKeep your community safe.",
                    fontSize = 18.sp,
                    color = Color(0xFF4A4A4A),
                    textAlign = TextAlign.Center
                )

                // Google Sign In Button
                OutlinedButton(
                    onClick = {
                        val signInIntent: Intent = googleClient.signInIntent
                        launcher.launch(signInIntent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
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
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                // Show error if needed
                authState.error?.let { err ->
                    // very simple inline error UI:
                    Text(text = err, color = Color.Red)
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Don't have an account?",
                        fontSize = 16.sp,
                        color = Color(0xFF6C6C6C)
                    )

                    Text(
                        text = "Sign Up",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xffE53131),
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clickable {
                                navHostController.navigate(CrimeAppScreen.SignUp.name)
                            }
                    )
            }
        }
    }
}
}
