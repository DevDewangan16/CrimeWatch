package com.example.crimewatch.data.supabase

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Url

data class ReportRequest(
    val user_id: String,
    val category: String?,
    val description: String?,
    val location: String?,
    val media_url: String? = null
)

// Supabase returns the inserted record as JSON array when Prefer: return=representation
typealias InsertResponse = List<Map<String, Any>>

interface SupabaseApi {
    @Headers(
        "Content-Type: application/json",
        // Prefer header asks Supabase to return the inserted rows
        "Prefer: return=representation"
    )
    @POST
    suspend fun insertReport(
        @Url url: String,             // full `/rest/v1/reports` URL
        @Body body: ReportRequest
    ): Response<InsertResponse>
}
