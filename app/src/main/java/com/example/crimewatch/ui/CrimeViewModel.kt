package com.example.crimewatch.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthState(
    val isLoading: Boolean = false,
    val userDisplayName: String? = null,
    val uid: String? = null,
    val error: String? = null
)
class CrimeViewModel(application: Application):AndroidViewModel(application){

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // internal mutable flow holding the FirebaseUser (or null)
    private val _user = MutableStateFlow<FirebaseUser?>(null)

    // expose as read-only StateFlow
    val user: StateFlow<FirebaseUser?> = _user

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState

    init {
        // initialize from FirebaseAuth current user (may be null)
        _user.value = auth.currentUser
    }

    // helper to update user (call this after sign-in / sign-out)
    fun setUser(user: FirebaseUser?) {
        _user.value = user
    }


    fun firebaseAuthWithGoogle(idToken: String, onResult: (Boolean, String?) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        _user.value = null // optional: show loading via separate flow if you have one
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // update the user flow with the current user
                _user.value = auth.currentUser
                onResult(true, null)
            } else {
                onResult(false, task.exception?.localizedMessage)
            }
        }
    }

    fun signOut() {
        auth.signOut()
        _user.value = null
    }
}