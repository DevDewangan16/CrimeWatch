package com.example.crimewatch.data.supabase

import retrofit2.Response
import retrofit2.http.*

data class ReportDto(
    val id: String,
    val user_id: String?,
    val category: String?,
    val description: String?,
    val location: String?,
    val media_url: String?,
    val verified: Boolean? = false,
    val created_at: String? = null
)

data class CommentDto(
    val id: String,
    val report_id: String,
    val user_id: String,
    val comment: String,
    val created_at: String?
)

data class VoteDto(
    val id: String,
    val report_id: String,
    val user_id: String,
    val vote_type: String,
    val created_at: String?
)

data class InsertReportRequest(
    val user_id: String,
    val category: String?,
    val description: String?,
    val location: String?,
    val media_url: String?,
    val verified: Boolean = false
)

data class CommentRequest(val report_id: String, val user_id: String, val comment: String)
data class VoteRequest(val report_id: String, val user_id: String, val vote_type: String)

typealias InsertReportResponse = List<Map<String, Any>>
typealias InsertCommentResponse = List<CommentDto>
typealias InsertVoteResponse = List<VoteDto>

interface SupabaseApi {
    // fetch reports (order by created_at desc)
    @GET("rest/v1/reports")
    suspend fun getReports(
        @Query("select") select: String = "*",
        @Query("order") order: String = "created_at.desc"
    ): Response<List<ReportDto>>

    // fetch comments for a report (report_id=eq.<id>)
    @GET("rest/v1/comments")
    suspend fun getComments(
        @Query("report_id") reportEq: String // pass "eq.<reportId>"
    ): Response<List<CommentDto>>

    // fetch votes for a report
    @GET("rest/v1/votes")
    suspend fun getVotes(
        @Query("report_id") reportEq: String // pass "eq.<reportId>"
    ): Response<List<VoteDto>>

    // insert report
    @Headers("Content-Type: application/json", "Prefer: return=representation")
    @POST
    suspend fun insertReport(
        @Url url: String,
        @Body body: InsertReportRequest
    ): Response<InsertReportResponse>

    // insert comment
    @Headers("Content-Type: application/json", "Prefer: return=representation")
    @POST("rest/v1/comments")
    suspend fun insertComment(
        @Body body: CommentRequest
    ): Response<InsertCommentResponse>

    // insert vote
    @Headers("Content-Type: application/json", "Prefer: return=representation")
    @POST("rest/v1/votes")
    suspend fun insertVote(
        @Body body: VoteRequest
    ): Response<InsertVoteResponse>
}
