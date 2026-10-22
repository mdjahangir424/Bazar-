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

    /**
     * Get full date in Bengali format e.g. "বৃহস্পতিবার, ২২ অক্টোবর ২০২৬"
     */
    fun getFullBanglaDate(isoDate: String = getTodayDhakaIso()): String {
        return try {
            val date = LocalDate.parse(isoDate, ISO_FORMATTER)
            val dayOfWeekBangla = when (date.dayOfWeek) {
                java.time.DayOfWeek.SATURDAY -> "শনিবার"
                java.time.DayOfWeek.SUNDAY -> "রবিবার"
                java.time.DayOfWeek.MONDAY -> "সোমবার"
                java.time.DayOfWeek.TUESDAY -> "মঙ্গলবার"
                java.time.DayOfWeek.WEDNESDAY -> "বুধবার"
                java.time.DayOfWeek.THURSDAY -> "বৃহস্পতিবার"
                java.time.DayOfWeek.FRIDAY -> "শুক্রবার"
            }
            val monthBangla = when (date.monthValue) {
                1 -> "জানুয়ারি"
                2 -> "ফেব্রুয়ারি"
                3 -> "মার্চ"
                4 -> "এপ্রিল"
                5 -> "মে"
                6 -> "জুন"
                7 -> "জুলাই"
                8 -> "আগস্ট"
                9 -> "সেপ্টেম্বর"
                10 -> "অক্টোবর"
                11 -> "নভেম্বর"
                12 -> "ডিসেম্বর"
                else -> ""
            }
            val dayDigits = toBanglaDigits(date.dayOfMonth.toString())
            val yearDigits = toBanglaDigits(date.year.toString())
            "$dayOfWeekBangla, $dayDigits $monthBangla $yearDigits"
        } catch (e: Exception) {
            isoDate
        }
    }
}
