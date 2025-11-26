package com.example.crimewatch.ui

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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.crimewatch.R

@Composable
fun SignUpScreen(
    crimeViewModel: CrimeViewModel,
    navHostController: NavHostController
) {

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

                // Title
                Text(
                    text = "Create Account",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xffE53131),
                    textAlign = TextAlign.Center
                )


                // Subtitle
                Text(
                    text = "Join CrimeWatch and stay informed.",
                    fontSize = 16.sp,
                    color = Color(0xFF4A4A4A),
                    textAlign = TextAlign.Center
                )

                // Google Sign Up Button
                OutlinedButton(
                    onClick = { /* TODO: Google Signup */ },
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

                // Already have account?
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
