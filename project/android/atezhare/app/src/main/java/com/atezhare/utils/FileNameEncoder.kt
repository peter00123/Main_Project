package com.atezhare.utils

import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * FileNameEncoder.kt
 * 
 * Handles encoding and decoding of the Atezhare file naming format.
 *
 * FORMAT:
 *   [first 6 chars of original name][6-digit random code][timer HH.MM.SS].[extension]
 *
 * EXAMPLES:
 *   Original: "invoice.pdf", timer: 1h 30m → "invoic4829290001.30.00.pdf"
 *   Original: "photo.jpg",   timer: 0 0 0  → "photo_489201_00.00.00.jpg"
 *   Original: "a.txt",       timer: 2 days → "a_____48292948.00.00.txt"
 *
 * TIMER FORMAT: HH.MM.SS where HH can exceed 24 (e.g. 48 hours = "48.00.00")
 * If timer is 00.00.00 → no expiry, Librarian does NOT track this file.
 */
object FileNameEncoder {

    /**
     * Generates a new encoded filename before upload.
     *
     * @param originalName  e.g. "invoice.pdf"
     * @param expiresAtMillis  0L = no timer (LIVE mode). Future timestamp = countdown.
     * @return encoded name e.g. "invoic482929_01.30.00.pdf"
     */
    fun encode(originalName: String, expiresAtMillis: Long): String {
        val dotIndex = originalName.lastIndexOf('.')
        val baseName = if (dotIndex > 0) originalName.substring(0, dotIndex) else originalName
        val extension = if (dotIndex > 0) originalName.substring(dotIndex) else ""

        // First 6 chars of base name, padded with underscores if shorter
        val prefix = baseName.take(6).padEnd(6, '_')

        // 6-digit random code generated on Android
        val code = (100000..999999).random().toString()

        // Timer in HH.MM.SS derived from expiresAt
        val timer = buildTimerString(expiresAtMillis)

        return "${prefix}${code}_${timer}${extension}"
    }

    /**
     * Decodes the timer from an encoded filename.
     * Returns 0L if filename is not in encoded format or timer is 00.00.00.
     *
     * @param encodedName  e.g. "invoic482929_01.30.00.pdf"
     * @return duration in milliseconds from NOW, or 0L if no timer
     */
    fun decodeTimerMillis(encodedName: String): Long {
        return try {
            // Timer is always the segment between last underscore and the dot-extension
            // Pattern: [6chars][6digits]_[HH.MM.SS][.ext]
            val dotIndex = encodedName.lastIndexOf('.')
            val nameWithoutExt = if (dotIndex > 0) encodedName.substring(0, dotIndex) else encodedName
            val underscoreIndex = nameWithoutExt.lastIndexOf('_')
            if (underscoreIndex < 0) return 0L

            val timerPart = nameWithoutExt.substring(underscoreIndex + 1) // e.g. "01.30.00"
            val parts = timerPart.split(".")
            if (parts.size != 3) return 0L

            val hours   = parts[0].toLongOrNull() ?: return 0L
            val minutes = parts[1].toLongOrNull() ?: return 0L
            val seconds = parts[2].toLongOrNull() ?: return 0L

            // 00.00.00 means no timer
            if (hours == 0L && minutes == 0L && seconds == 0L) return 0L

            TimeUnit.HOURS.toMillis(hours) +
            TimeUnit.MINUTES.toMillis(minutes) +
            TimeUnit.SECONDS.toMillis(seconds)
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Converts an expiresAt timestamp to a HH.MM.SS duration string
     * by computing the difference from the current time.
     *
     * @param expiresAtMillis  0L = no timer → returns "00.00.00"
     */
    private fun buildTimerString(expiresAtMillis: Long): String {
        if (expiresAtMillis == 0L) return "00.00.00"

        val durationMillis = expiresAtMillis - System.currentTimeMillis()
        if (durationMillis <= 0L) return "00.00.00"

        val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(durationMillis)
        val hours   = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return String.format(Locale.US, "%02d.%02d.%02d", hours, minutes, seconds)
    }

    /**
     * Returns true if the filename appears to be in Atezhare encoded format.
     * Useful for deciding whether to pass a filename to the Librarian.
     */
    fun isEncodedFileName(fileName: String): Boolean {
        return try {
            val dotIndex = fileName.lastIndexOf('.')
            val nameWithoutExt = if (dotIndex > 0) fileName.substring(0, dotIndex) else fileName
            val underscoreIndex = nameWithoutExt.lastIndexOf('_')
            if (underscoreIndex < 0) return false
            val timerPart = nameWithoutExt.substring(underscoreIndex + 1)
            val parts = timerPart.split(".")
            parts.size == 3 && parts.all { it.all(Char::isDigit) }
        } catch (e: Exception) {
            false
        }
    }
}
