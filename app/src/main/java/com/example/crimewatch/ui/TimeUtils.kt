package com.example.crimewatch.ui

import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*

fun formatTimeAgo(timestamp: String?): String {
    if (timestamp.isNullOrBlank()) return "Unknown"

    return try {
        val parsed = Instant.parse(timestamp)
        val zoneTime = parsed.atZone(ZoneId.systemDefault())
        val now = ZonedDateTime.now()

        val diff = Duration.between(zoneTime, now).toMinutes()

        return when {
            diff < 1 -> "Just now"
            diff < 60 -> "${diff} min ago"
            diff < 1440 -> "${diff / 60} hrs ago"
            diff < 2880 -> "Yesterday"
            else -> zoneTime.format(DateTimeFormatter.ofPattern("dd MMM yyyy • hh:mm a"))
        }
    } catch (e: Exception) {
        timestamp // fallback
    }
}
