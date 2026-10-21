package com.example.ui.util

import java.text.DecimalFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

object BanglaFormatter {
    val DHAKA_ZONE: ZoneId = ZoneId.of("Asia/Dhaka")
    private val ISO_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)
    private val DISPLAY_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.US)
    private val NUMBER_FORMAT = DecimalFormat("#,##0.##")

    /**
     * Get current date in Asia/Dhaka timezone in ISO format (yyyy-MM-dd)
     */
    fun getTodayDhakaIso(): String {
        return LocalDate.now(DHAKA_ZONE).format(ISO_FORMATTER)
    }

    /**
     * Get start of current month in Asia/Dhaka timezone
     */
    fun getStartOfCurrentMonthIso(): String {
        return LocalDate.now(DHAKA_ZONE).with(TemporalAdjusters.firstDayOfMonth()).format(ISO_FORMATTER)
    }

    /**
     * Get end of current month in Asia/Dhaka timezone
     */
    fun getEndOfCurrentMonthIso(): String {
        return LocalDate.now(DHAKA_ZONE).with(TemporalAdjusters.lastDayOfMonth()).format(ISO_FORMATTER)
    }

    /**
     * Get start of current week (Monday) in Asia/Dhaka timezone
     */
    fun getStartOfCurrentWeekIso(): String {
        val today = LocalDate.now(DHAKA_ZONE)
        return today.minusDays(today.dayOfWeek.value.toLong() - 1).format(ISO_FORMATTER)
    }

    /**
     * Convert yyyy-MM-dd to dd-MM-yyyy
     */
    fun formatToDisplayDate(isoDate: String, useBanglaDigits: Boolean = true): String {
        return try {
            val date = LocalDate.parse(isoDate, ISO_FORMATTER)
            val englishDate = date.format(DISPLAY_FORMATTER)
            if (useBanglaDigits) toBanglaDigits(englishDate) else englishDate
        } catch (e: Exception) {
            isoDate
        }
    }

    /**
     * Format currency amount with BDT symbol (৳)
     */
    fun formatCurrency(amount: Double, useBanglaDigits: Boolean = true): String {
        val formatted = NUMBER_FORMAT.format(amount)
        val text = "৳$formatted"
        return if (useBanglaDigits) toBanglaDigits(text) else text
    }

    /**
     * Format number or integer with optional Bengali digits
     */
    fun formatNumber(number: Number, useBanglaDigits: Boolean = true): String {
        val text = NUMBER_FORMAT.format(number)
        return if (useBanglaDigits) toBanglaDigits(text) else text
    }

    /**
     * Convert standard English digits 0-9 to Bengali digits ০-৯
     */
    fun toBanglaDigits(input: String): String {
        val banglaDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
        val sb = StringBuilder()
        for (ch in input) {
            if (ch in '0'..'9') {
                sb.append(banglaDigits[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }
}
