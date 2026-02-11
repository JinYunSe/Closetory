package com.ssafy.closetory.util

object DateTimeFormat {
    fun formatCreatedAt(raw: String?): String {
        if (raw.isNullOrBlank()) return ""
        val s = raw.trim()

        // 1) Prefer full date + time match (supports 1-2 digit hour, optional seconds)
        val m = Regex("""(\d{4}-\d{2}-\d{2})[ T](\d{1,2}):(\d{2})(?::\d{2})?""").find(s)
        if (m != null) {
            val date = m.groupValues[1]
            val hour = m.groupValues[2].padStart(2, '0')
            val minute = m.groupValues[3]
            return "$date $hour:$minute"
        }

        // 2) Fallback: separate date and HH:mm anywhere in the string
        val dateMatch = Regex("""\d{4}-\d{2}-\d{2}""").find(s)?.value
        val timeMatch = Regex("""\b\d{1,2}:\d{2}\b""").find(s)?.value
        if (!dateMatch.isNullOrBlank()) {
            return if (!timeMatch.isNullOrBlank()) {
                val parts = timeMatch.split(":")
                val hour = parts[0].padStart(2, '0')
                val minute = parts[1]
                "$dateMatch $hour:$minute"
            } else {
                dateMatch
            }
        }

        // 3) Last resort: normalize T -> space and take date + HH:mm
        val normalized = s.replace("T", " ")
        val spaceIdx = normalized.indexOf(' ')
        if (spaceIdx == -1) return normalized
        val datePart = normalized.substring(0, spaceIdx)
        val timePart = normalized.substring(spaceIdx + 1)
        val hhmm = timePart.take(5)
        return if (hhmm.length == 5) "$datePart $hhmm" else normalized
    }
}