package com.example.crimewatch.data.supabase

import android.content.Context
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.Request
import okhttp3.OkHttpClient
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okio.buffer
import okio.source
import java.io.File
import java.io.InputStream
import java.util.*

class SupabaseRepository(
    private val api: SupabaseApi,
    private val baseUrl: String,
    private val anonKey: String,
    private val storageBucket: String
) {

    // REST endpoint for reports
    private val reportsEndpoint = "${baseUrl}rest/v1/reports"

    suspend fun fetchReports(): List<ReportDto> {
        // call API without user filter
        val resp = api.getReports(userIdFilter = null)
        if (resp.isSuccessful) return resp.body() ?: emptyList()
        else throw Exception("Failed to fetch reports: ${resp.code()} ${resp.errorBody()?.string()}")
    }

    suspend fun fetchReportsByUser(userId: String): List<ReportDto> {
        // pass "eq.<userId>" so Supabase REST filters by equality
        val filterValue = "eq.$userId"
        val resp = api.getReports(userIdFilter = filterValue)
        if (resp.isSuccessful) return resp.body() ?: emptyList()
        else throw Exception("Failed to fetch user reports: ${resp.code()} ${resp.errorBody()?.string()}")
    }

    suspend fun fetchComments(reportId: String): List<CommentDto> {
        val resp = api.getComments("eq.$reportId")
        if (resp.isSuccessful) return resp.body() ?: emptyList()
        else throw Exception("Failed to fetch comments")
    }

    suspend fun fetchVotes(reportId: String): List<VoteDto> {
        val resp = api.getVotes("eq.$reportId")
        if (resp.isSuccessful) return resp.body() ?: emptyList()
        else throw Exception("Failed to fetch votes")
    }

    // inside SupabaseRepository (Kotlin)
    suspend fun insertReport(request: InsertReportRequest): Map<String, Any> {
        val resp = api.insertReport(reportsEndpoint, request)
        if (resp.isSuccessful) {
            val body = resp.body()
            if (!body.isNullOrEmpty()) return body[0]
            else throw Exception("Empty response from Supabase insert")
        } else {
            val err = resp.errorBody()?.string()
            throw Exception("Supabase insert failed: ${resp.code()} - $err")
        }
    }


    suspend fun insertComment(reportId: String, userId: String, comment: String): CommentDto {
        val resp = api.insertComment(CommentRequest(reportId, userId, comment))
        if (resp.isSuccessful) {
            val body = resp.body()
            if (!body.isNullOrEmpty()) return body[0]
            else throw Exception("Empty response from insertComment")
        } else {
            throw Exception("Insert comment failed: ${resp.code()} ${resp.errorBody()?.string()}")
        }
    }

    suspend fun insertVote(reportId: String, userId: String, voteType: String): VoteDto {
        val resp = api.insertVote(VoteRequest(reportId, userId, voteType))
        if (resp.isSuccessful) {
            val body = resp.body()
            if (!body.isNullOrEmpty()) return body[0]
            else throw Exception("Empty response from insertVote")
        } else {
            throw Exception("Insert vote failed: ${resp.code()} ${resp.errorBody()?.string()}")
        }
    }

    /**
     * Upload media to Supabase Storage using PUT:
     * returns public URL string (using public bucket path)
     *
     * Note: baseUrl must be like "https://abcd1234.supabase.co/"
     * storage upload URL: {baseUrl}storage/v1/object/{bucket}/{path}
     *
     * This is a simple implementation that uploads the raw bytes with PUT.
     * If you want to store in a folder and avoid collisions, pass a uniqueFileName.
     */
    fun uploadMedia(context: Context, inputStream: InputStream, fileName: String): String {
        // path in the bucket
        val objectPath = "$storageBucket/$fileName" // e.g. "reports/report_...jpg"

        // upload url (upload to object path)
        val uploadUrl = "${baseUrl}storage/v1/object/$objectPath" // PUT

        // write to temp file
        val tmp = File.createTempFile("upload_", fileName, context.cacheDir)
        tmp.outputStream().use { out ->
            inputStream.copyTo(out)
        }

        val client = OkHttpClient.Builder().build()
        val mediaType = "application/octet-stream".toMediaTypeOrNull()
        val requestBody = tmp.asRequestBody(mediaType)

        val request = Request.Builder()
            .url(uploadUrl)
            .addHeader("apikey", anonKey)
            .addHeader("Authorization", "Bearer $anonKey")
            .put(requestBody)
            .build()

        client.newCall(request).execute().use { resp ->
            if (!resp.isSuccessful) {
                val body = resp.body?.string()
                throw Exception("Supabase upload failed ${resp.code}: $body")
            }
        }

        // Remove tmp file if you want
        tmp.delete()

        // Public URL format for public buckets:
        // {baseUrl}storage/v1/object/public/{bucket}/{pathRelativeToBucket}
        val publicUrl = "${baseUrl}storage/v1/object/public/$objectPath"
        return publicUrl
    }

}
