package com.example.crimewatch.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crimewatch.R

@Composable
fun SignInScreen(){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription ="Logo",
            modifier = Modifier.size(282.dp),
            contentScale = ContentScale.Fit,
        )
        Text(
            text = "Welcome to CrimeWatch",
            modifier = Modifier.fillMaxWidth(),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Join the community and make your surroundings safer.",
            modifier = Modifier.fillMaxWidth(),
            fontSize = 20.sp,
        )
        OutlinedButton(onClick = { /*TODO*/ } ,
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            shape = RoundedCornerShape(5.dp),
            border = BorderStroke(2.dp,Color(0xffE53131))
        ) {
            Text(
                text = "Continue with Google",
                fontSize = 20.sp,
                color = Color.Black
            )
        }
        Text(
            text = "By continuing, you agree to CrimeWatch’s Terms & Privacy Policy.",
            modifier = Modifier.fillMaxWidth(),
            fontSize = 18.sp,
            color = Color(0xff7C7272),
            textAlign = TextAlign.Center
        )
    }

}