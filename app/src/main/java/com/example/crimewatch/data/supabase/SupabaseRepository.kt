package com.example.crimewatch.data.supabase

class SupabaseRepository(private val api: SupabaseApi, private val restEndpoint: String) {

    /**
     * Inserts a report.
     * Returns the inserted record (as Map) on success or throws exception.
     */
    suspend fun insertReport(request: ReportRequest): Map<String, Any> {
        val resp = api.insertReport(restEndpoint, request)
        if (resp.isSuccessful) {
            val body = resp.body()
            if (body != null && body.isNotEmpty()) {
                return body[0]
            } else {
                throw Exception("Empty response")
            }
        } else {
            val err = resp.errorBody()?.string()
            throw Exception("Supabase insert failed: ${resp.code()} - $err")
        }
    }
}
