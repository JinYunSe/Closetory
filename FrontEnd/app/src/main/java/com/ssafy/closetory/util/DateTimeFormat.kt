package com.ssafy.closetory.util

object DateTimeFormat {
    fun formatCreatedAt(raw: String?): String {
        if (raw.isNullOrBlank()) return ""
        val s = raw.trim()

        // 1) Prefer explicit yyyy-MM-dd and HH:mm patterns anywhere in the string
        val dateMatch = Regex("""\d{4}-\d{2}-\d{2}""").find(s)?.value
        val timeMatch = Regex("""\b\d{2}:\d{2}\b""").find(s)?.value
        if (!dateMatch.isNullOrBlank()) {
            return if (!timeMatch.isNullOrBlank()) "$dateMatch $timeMatch" else dateMatch
        }

        // 2) Fallback: replace T with space and take date + HH:mm
        val normalized = s.replace("T", " ")
        val spaceIdx = normalized.indexOf(' ')
        if (spaceIdx == -1) return normalized
        val datePart = normalized.substring(0, spaceIdx)
        val timePart = normalized.substring(spaceIdx + 1)
        val hhmm = timePart.take(5)
        return if (hhmm.length == 5) "$datePart $hhmm" else normalized
    }
}
