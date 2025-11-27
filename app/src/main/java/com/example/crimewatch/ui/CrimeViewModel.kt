package com.example.crimewatch.ui

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.crimewatch.data.supabase.InsertReportRequest
import com.example.crimewatch.data.supabase.SupabaseApi
import com.example.crimewatch.data.supabase.SupabaseClient
import com.example.crimewatch.data.supabase.SupabaseRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withContext
import java.util.UUID



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

    private val context = application.applicationContext

    // Supabase client + repo
    private val supabaseApi: SupabaseApi = SupabaseClient.create("https://ljwkisfjnukggmeldetk.supabase.co", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imxqd2tpc2ZqbnVrZ2dtZWxkZXRrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjQxNzkwMjksImV4cCI6MjA3OTc1NTAyOX0.fQ3MbCecydRTT24z3qCGwNYmMqq0OXZvlvmx_KeWNhY")
    private val repository = SupabaseRepository(
        supabaseApi,
        "https://ljwkisfjnukggmeldetk.supabase.co/",
         "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imxqd2tpc2ZqbnVrZ2dtZWxkZXRrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjQxNzkwMjksImV4cCI6MjA3OTc1NTAyOX0.fQ3MbCecydRTT24z3qCGwNYmMqq0OXZvlvmx_KeWNhY",
        "reports"
    )

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
    }

    /**
     * Upload media from a Uri. This runs in IO dispatcher and sets pickedMediaUrl to public URL.
     * Returns true on success, false on failure.
     */

    fun uploadThenSubmit(uri: Uri) {
        viewModelScope.launch {
            try {
                _isSubmitting.value = true
                _submitResult.value = null

                // 1) upload and get public URL (IO)
                val publicUrl = withContext(Dispatchers.IO) {
                    // open stream and call repository.uploadMedia
                    val cr = context.contentResolver
                    cr.openInputStream(uri)?.use { input ->
                        val ext = getFileExtension(uri) ?: "bin"
                        val fileName = "report_${System.currentTimeMillis()}_${UUID.randomUUID()}.$ext"
                        repository.uploadMedia(context, input, fileName) // blocks, returns public url
                    } ?: throw Exception("Cannot open input stream")
                }

                // set it in VM state
                pickedMediaUrl.value = publicUrl

                // 2) validate and insert report
                val uid = auth.currentUser?.uid ?: throw Exception("User not signed in")
                val category = selectedCategory.value ?: throw Exception("Choose category")
                val desc = description.value.trim()
                if (desc.isBlank()) throw Exception("Add description")

                val req = InsertReportRequest(
                    user_id = uid,
                    category = category,
                    description = desc,
                    location = locationText.value,
                    media_url = publicUrl,
                    verified = false
                )

                val inserted = withContext(Dispatchers.IO) {
                    repository.insertReport(req) // returns map
                }

                _submitResult.value = "success"
            } catch (e: Exception) {
                _submitResult.value = e.localizedMessage ?: "Submit failed"
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    private fun getFileExtension(uri: Uri): String? {
        val cr = context.contentResolver
        val type = cr.getType(uri) ?: return null
        return when {
            type.contains("jpeg") -> "jpg"
            type.contains("png") -> "png"
            type.contains("webp") -> "webp"
            type.contains("gif") -> "gif"
            type.contains("mp4") -> "mp4"
            else -> type.substringAfterLast('/', "bin")
        }
    }

    /**
     * Submit report to Supabase (expects pickedMediaUrl to already be set if media included)
     */
    fun submitReport(useExistingMediaUrl: Boolean = true) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            _submitResult.value = "User not signed in"
            return
        }

        val cat = selectedCategory.value
        val desc = description.value.trim()
        val loc = locationText.value
        val media = pickedMediaUrl.value // IMPORTANT: set this (manual test) or by upload logic

        if (cat.isNullOrBlank()) {
            _submitResult.value = "Please choose a category"
            return
        }
        if (desc.isBlank()) {
            _submitResult.value = "Please add a description"
            return
        }
        if (!useExistingMediaUrl && media.isNullOrBlank()) {
            _submitResult.value = "Please upload image first"
            return
        }

        viewModelScope.launch {
            try {
                _isSubmitting.value = true
                _submitResult.value = null

                val req = InsertReportRequest(
                    user_id = uid,
                    category = cat,
                    description = desc,
                    location = loc,
                    media_url = media,
                    verified = false
                )

                val inserted = withContext(Dispatchers.IO) {
                    repository.insertReport(req)
                }
                // success
                _submitResult.value = "success"
            } catch (e: Exception) {
                _submitResult.value = e.localizedMessage ?: "Submit failed"
                Log.e("CrimeVM", "submitReport error", e)
            } finally {
                _isSubmitting.value = false
            }
        }
    }

}