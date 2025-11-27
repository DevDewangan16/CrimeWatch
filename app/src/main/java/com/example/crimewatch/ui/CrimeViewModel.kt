// CrimeViewModel.kt
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
import java.util.UUID

data class AuthState(
    val isLoading: Boolean = false,
    val userDisplayName: String? = null,
    val uid: String? = null,
    val error: String? = null
)

class CrimeViewModel(application: Application) : AndroidViewModel(application) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val context = application.applicationContext

    // Supabase client + repo (keep your values)
    private val supabaseApi: SupabaseApi = SupabaseClient.create(
        "https://ljwkisfjnukggmeldetk.supabase.co",
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imxqd2tpc2ZqbnVrZ2dtZWxkZXRrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjQxNzkwMjksImV4cCI6MjA3OTc1NTAyOX0.fQ3MbCecydRTT24z3qCGwNYmMqq0OXZvlvmx_KeWNhY"
    )

    private val repository = SupabaseRepository(
        supabaseApi,
        "https://ljwkisfjnukggmeldetk.supabase.co/",
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imxqd2tpc2ZqbnVrZ2dtZWxkZXRrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjQxNzkwMjksImV4cCI6MjA3OTc1NTAyOX0.fQ3MbCecydRTT24z3qCGwNYmMqq0OXZvlvmx_KeWNhY",
        "reports"
    )

    // user state
    private val _user = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val user: StateFlow<FirebaseUser?> = _user

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState

    private val _logoutClicked = MutableStateFlow(false)
    val logoutClicked: MutableStateFlow<Boolean> get() = _logoutClicked

    // Report form state
    val selectedCategory = MutableStateFlow<String?>(null)
    val description = MutableStateFlow("")
    val locationText = MutableStateFlow("Detected: (not set)")
    val pickedMediaUrl = MutableStateFlow<String?>(null) // public URL after upload

    // Upload / submit state
    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting

    private val _submitResult = MutableStateFlow<String?>(null) // "success" or error message
    val submitResult: StateFlow<String?> = _submitResult

    private val _myReports = MutableStateFlow<List<com.example.crimewatch.data.supabase.ReportDto>>(emptyList())
    val myReports: StateFlow<List<com.example.crimewatch.data.supabase.ReportDto>> = _myReports

    private val _isLoadingMyReports = MutableStateFlow(false)
    val isLoadingMyReports: StateFlow<Boolean> = _isLoadingMyReports


    init {
        // initial user
        _user.value = auth.currentUser
    }

    fun setUser(user: FirebaseUser?) {
        _user.value = user
    }

    fun setLogoutStatus(logoutStatus: Boolean) {
        _logoutClicked.value = logoutStatus
    }

    fun signOut() {
        auth.signOut()
        _user.value = null
    }

    fun firebaseAuthWithGoogle(idToken: String, onResult: (Boolean, String?) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        _user.value = null
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                _user.value = auth.currentUser
                onResult(true, null)
            } else {
                onResult(false, task.exception?.localizedMessage)
            }
        }
    }

    private fun getFileExtension(uriString: String): String {
        return uriString.substringAfterLast('.', "jpg")
    }

    private fun getFileExtension(uri: android.net.Uri): String? {
        val cr = context.contentResolver
        val type = cr.getType(uri) ?: return null
        return when {
            type.contains("jpeg", true) -> "jpg"
            type.contains("png", true) -> "png"
            type.contains("webp", true) -> "webp"
            type.contains("gif", true) -> "gif"
            type.contains("mp4", true) -> "mp4"
            else -> type.substringAfterLast('/', "bin")
        }
    }

    /**
     * Upload media only. Does NOT submit the report.
     * Sets pickedMediaUrl on success and updates isUploading state.
     */
    fun uploadMediaOnly(uri: Uri, onComplete: ((Boolean, String?) -> Unit)? = null) {
        viewModelScope.launch {
            try {
                _isUploading.value = true
                _submitResult.value = null

                val publicUrl = withContext(Dispatchers.IO) {
                    val cr = context.contentResolver
                    cr.openInputStream(uri)?.use { input ->
                        val ext = getFileExtension(uri) ?: "jpg"
                        val fileName = "report_${System.currentTimeMillis()}_${UUID.randomUUID()}.$ext"
                        repository.uploadMedia(context, input, fileName)
                    } ?: throw Exception("Cannot open input stream")
                }

                pickedMediaUrl.value = publicUrl
                onComplete?.invoke(true, publicUrl)
            } catch (e: Exception) {
                Log.e("CrimeVM", "uploadMediaOnly error", e)
                _submitResult.value = "Media upload error: ${e.message}"
                onComplete?.invoke(false, e.message)
            } finally {
                _isUploading.value = false
            }
        }
    }

    /**
     * Submit report. Expects pickedMediaUrl to be set if media was uploaded.
     */
    fun submitReport() {
        viewModelScope.launch {
            // basic validation
            if (selectedCategory.value.isNullOrBlank()) {
                _submitResult.value = "Please choose a category"
                return@launch
            }
            if (description.value.isBlank()) {
                _submitResult.value = "Please add a description"
                return@launch
            }
            if (_isUploading.value) {
                _submitResult.value = "Image is still uploading — please wait"
                return@launch
            }
            val uid = auth.currentUser?.uid
            if (uid == null) {
                _submitResult.value = "User not signed in"
                return@launch
            }

            _isSubmitting.value = true
            _submitResult.value = null

            try {
                val req = InsertReportRequest(
                    user_id = uid,
                    category = selectedCategory.value,
                    description = description.value.trim(),
                    location = locationText.value,
                    media_url = pickedMediaUrl.value,
                    verified = false
                )

                withContext(Dispatchers.IO) {
                    repository.insertReport(req) // may throw
                }

                _submitResult.value = "success"
            } catch (e: Exception) {
                Log.e("CrimeVM", "submitReport error", e)
                _submitResult.value = e.localizedMessage ?: "Submit failed"
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    /** Reset submitResult so dialogs don't reappear */
    fun resetSubmitResult() {
        _submitResult.value = null
    }

    fun loadMyReports() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                _isLoadingMyReports.value = true
                val list = withContext(Dispatchers.IO) {
                    repository.fetchReportsByUser(uid)
                }
                _myReports.value = list
            } catch (e: Exception) {
                // handle or log error
                _myReports.value = emptyList()
            } finally {
                _isLoadingMyReports.value = false
            }
        }
    }
}
