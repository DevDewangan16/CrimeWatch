package com.example.crimewatch.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.crimewatch.data.supabase.ReportRequest
import com.example.crimewatch.data.supabase.SupabaseClient
import com.example.crimewatch.data.supabase.SupabaseRepository
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

    private val _logoutClicked=MutableStateFlow(false)
    val logoutClicked:MutableStateFlow<Boolean>get() = _logoutClicked

    // report form fields (exposed as state flows)
    val selectedCategory = MutableStateFlow<String?>(null)
    val description = MutableStateFlow("")
    val locationText = MutableStateFlow("Detected: (not set)")
    val pickedMediaUrl = MutableStateFlow<String?>(null) // store media URL (after upload)

    // submission status
    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting

    private val _submitResult = MutableStateFlow<String?>(null) // success message or error
    val submitResult: StateFlow<String?> = _submitResult

    // Supabase repo (create with BuildConfig values)
    private val supabaseApi = SupabaseClient.create(
        "https://ljwkisfjnukggmeldetk.supabase.co/","eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imxqd2tpc2ZqbnVrZ2dtZWxkZXRrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjQxNzkwMjksImV4cCI6MjA3OTc1NTAyOX0.fQ3MbCecydRTT24z3qCGwNYmMqq0OXZvlvmx_KeWNhY"
    )
    private val supabaseRepo = SupabaseRepository(supabaseApi, "https://ljwkisfjnukggmeldetk.supabase.co/rest/v1/reports")


    init {
        // initialize from FirebaseAuth current user (may be null)
        _user.value = auth.currentUser
    }

    // helper to update user (call this after sign-in / sign-out)
    fun setUser(user: FirebaseUser?) {
        _user.value = user
    }

    fun setLogoutStatus(
        logoutStatus:Boolean
    ){
        _logoutClicked.value=logoutStatus
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
        // optionally clear other data:
        selectedCategory.value = null
        description.value = ""
        locationText.value = "Detected: (not set)"
        pickedMediaUrl.value = null
    }

    /**
     * Submit report to Supabase
     */
    fun submitReport() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            _submitResult.value = "User not signed in"
            return
        }

        val category = selectedCategory.value
        val desc = description.value
        val loc = locationText.value
        val media = pickedMediaUrl.value

        // simple validation
        if (category.isNullOrBlank() || desc.isBlank()) {
            _submitResult.value = "Please fill category and description"
            return
        }

        viewModelScope.launch {
            try {
                _isSubmitting.value = true
                _submitResult.value = null

                val request = ReportRequest(
                    user_id = uid,
                    category = category,
                    description = desc,
                    location = loc,
                    media_url = media
                )

                val inserted = supabaseRepo.insertReport(request)
                _submitResult.value = "success" // or parse inserted["id"]
            } catch (e: Exception) {
                _submitResult.value = e.localizedMessage ?: "Submit failed"
            } finally {
                _isSubmitting.value = false
            }
        }
    }
}