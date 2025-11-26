package com.example.crimewatch.ui

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient

fun getGoogleSignInClient(context: Context, webClientId: String): GoogleSignInClient {
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(webClientId) // Web client ID from Firebase console
        .requestEmail()
        .build()
    return GoogleSignIn.getClient(context, gso)
}