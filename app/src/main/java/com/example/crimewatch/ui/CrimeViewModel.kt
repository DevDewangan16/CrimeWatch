package com.example.crimewatch.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
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

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState

    init {
        // If user already signed in, update state
        auth.currentUser?.let { user ->
            _authState.value = AuthState(
                isLoading = false,
                userDisplayName = user.displayName,
                uid = user.uid,
                error = null
            )
        }
    }

    fun firebaseAuthWithGoogle(idToken: String, onResult: (Boolean, String?) -> Unit) {
        _authState.value = _authState.value.copy(isLoading = true, error = null)
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                viewModelScope.launch {
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        _authState.value = AuthState(
                            isLoading = false,
                            userDisplayName = user?.displayName,
                            uid = user?.uid,
                            error = null
                        )
                        onResult(true, null)
                    } else {
                        val err = task.exception?.localizedMessage ?: "Authentication failed"
                        _authState.value = AuthState(isLoading = false, error = err)
                        onResult(false, err)
                    }
                }
            }
    }

    fun signOut() {
        auth.signOut()
        _authState.value = AuthState()
    }
}