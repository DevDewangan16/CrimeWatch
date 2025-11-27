package com.example.crimewatch.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.crimewatch.data.supabase.SupabaseApi
import com.example.crimewatch.data.supabase.SupabaseClient
import com.example.crimewatch.data.supabase.SupabaseRepository
import com.example.crimewatch.data.supabase.ReportDto
import com.example.crimewatch.data.supabase.CommentDto
import com.example.crimewatch.data.supabase.VoteDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ReportsViewModel(application: Application) : AndroidViewModel(application) {

    private val supabaseApi: SupabaseApi = SupabaseClient.create("https://ljwkisfjnukggmeldetk.supabase.co", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imxqd2tpc2ZqbnVrZ2dtZWxkZXRrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjQxNzkwMjksImV4cCI6MjA3OTc1NTAyOX0.fQ3MbCecydRTT24z3qCGwNYmMqq0OXZvlvmx_KeWNhY")
    private val repo = SupabaseRepository(
        supabaseApi,
        "https://ljwkisfjnukggmeldetk.supabase.co/",
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imxqd2tpc2ZqbnVrZ2dtZWxkZXRrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjQxNzkwMjksImV4cCI6MjA3OTc1NTAyOX0.fQ3MbCecydRTT24z3qCGwNYmMqq0OXZvlvmx_KeWNhY",
        "reports"
    )

    private val _reports = MutableStateFlow<List<ReportDto>>(emptyList())
    val reports: StateFlow<List<ReportDto>> = _reports

    private val _comments = MutableStateFlow<Map<String, List<CommentDto>>>(emptyMap())
    val comments: StateFlow<Map<String, List<CommentDto>>> = _comments

    private val _votes = MutableStateFlow<Map<String, List<VoteDto>>>(emptyMap())
    val votes: StateFlow<Map<String, List<VoteDto>>> = _votes

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        refreshReports()
    }

    fun refreshReports() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val list = repo.fetchReports()
                _reports.value = list
            } catch (e: Exception) {
                // log to Logcat and keep empty list
                Log.e("ReportsVM", "refreshReports failed", e)
                _reports.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun loadComments(reportId: String) {
        viewModelScope.launch {
            try {
                val list = repo.fetchComments(reportId)
                _comments.value = _comments.value.toMutableMap().also { it[reportId] = list }
            } catch (_: Exception) {}
        }
    }

    fun loadVotes(reportId: String) {
        viewModelScope.launch {
            try {
                val list = repo.fetchVotes(reportId)
                _votes.value = _votes.value.toMutableMap().also { it[reportId] = list }
            } catch (_: Exception) {}
        }
    }

    fun postComment(reportId: String, userId: String, text: String, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                repo.insertComment(reportId, userId, text)
                val updated = repo.fetchComments(reportId)
                _comments.value = _comments.value.toMutableMap().also { it[reportId] = updated }
                onDone(true)
            } catch (e: Exception) {
                onDone(false)
            }
        }
    }

    fun postVote(reportId: String, userId: String, voteType: String, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                repo.insertVote(reportId, userId, voteType)
                val updated = repo.fetchVotes(reportId)
                _votes.value = _votes.value.toMutableMap().also { it[reportId] = updated }
                onDone(true)
            } catch (e: Exception) {
                onDone(false)
            }
        }
    }
}
