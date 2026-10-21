package com.example.ui.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.model.MarketExpense
import com.example.data.model.OutstandingEntry
import com.example.data.model.PaymentRecord
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

object CsvExporter {

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }

    /**
     * Export Market Expenses to CSV
     */
    fun exportMarketExpensesCsv(context: Context, list: List<MarketExpense>, language: AppLanguage): File {
        val isBn = language == AppLanguage.BANGLA
        val fileName = "Market_Expenses_${BanglaFormatter.getTodayDhakaIso()}.csv"
        val file = File(context.cacheDir, fileName)

        FileOutputStream(file).use { fos ->
            OutputStreamWriter(fos, StandardCharsets.UTF_8).use { writer ->
                // Write UTF-8 BOM so Microsoft Excel correctly displays Bengali characters
                writer.write("\uFEFF")

                // Headers
                val hSl = if (isBn) "ক্রমিক নং" else "Sl. No."
                val hDate = if (isBn) "তারিখ (DD-MM-YYYY)" else "Date (DD-MM-YYYY)"
                val hAmt = if (isBn) "বাজার খরচ (টাকা)" else "Market Expense (BDT)"
                val hNote = if (isBn) "বিবরণ" else "Note"

                writer.write("$hSl,$hDate,$hAmt,$hNote\n")

                var total = 0.0
                for ((idx, item) in list.withIndex()) {
                    total += item.amount
                    val sl = if (isBn) BanglaFormatter.toBanglaDigits("${idx + 1}") else "${idx + 1}"
                    val dt = BanglaFormatter.formatToDisplayDate(item.date, isBn)
                    val amt = item.amount.toString()
                    val note = escapeCsv(item.note)
                    writer.write("$sl,$dt,$amt,$note\n")
                }

                // Summary Total Row
                val totalLabel = if (isBn) "সর্বমোট বাজার খরচ:" else "Total Market Expenses:"
                writer.write("$totalLabel,,$total,\n")
            }
        }
        return file
    }

    /**
     * Export Outstanding & Payment registers to CSV
     */
    fun exportOutstandingCsv(
        context: Context,
        outstanding: List<OutstandingEntry>,
        payments: List<PaymentRecord>,
        language: AppLanguage
    ): File {
        val isBn = language == AppLanguage.BANGLA
        val fileName = "Outstanding_Register_${BanglaFormatter.getTodayDhakaIso()}.csv"
        val file = File(context.cacheDir, fileName)

        FileOutputStream(file).use { fos ->
            OutputStreamWriter(fos, StandardCharsets.UTF_8).use { writer ->
                writer.write("\uFEFF")

                // Section 1: Outstanding Entries
                writer.write(if (isBn) "১. নতুন বাকি তালিকা\n" else "1. Outstanding Incurred Register\n")
                writer.write(if (isBn) "ক্রমিক নং,তারিখ,বাকি টাকা (৳),বিবরণ\n" else "Sl. No.,Date,Outstanding (BDT),Note\n")

                var totalOut = 0.0
                for ((idx, item) in outstanding.withIndex()) {
                    totalOut += item.amount
                    val sl = if (isBn) BanglaFormatter.toBanglaDigits("${idx + 1}") else "${idx + 1}"
                    val dt = BanglaFormatter.formatToDisplayDate(item.date, isBn)
                    val amt = item.amount.toString()
                    val note = escapeCsv(item.note)
                    writer.write("$sl,$dt,$amt,$note\n")
                }
                writer.write(if (isBn) "মোট বাকি টাকা,,$totalOut,\n\n" else "Total Outstanding Added,,$totalOut,\n\n")

                // Section 2: Payments
                writer.write(if (isBn) "২. বাকি পরিশোধের তালিকা\n" else "2. Payments Made Register\n")
                writer.write(if (isBn) "ক্রমিক নং,তারিখ,পরিশোধিত টাকা (৳),বিবরণ\n" else "Sl. No.,Date,Payment (BDT),Note\n")

                var totalPay = 0.0
                for ((idx, item) in payments.withIndex()) {
                    totalPay += item.amount
                    val sl = if (isBn) BanglaFormatter.toBanglaDigits("${idx + 1}") else "${idx + 1}"
                    val dt = BanglaFormatter.formatToDisplayDate(item.date, isBn)
                    val amt = item.amount.toString()
                    val note = escapeCsv(item.note)
                    writer.write("$sl,$dt,$amt,$note\n")
                }
                writer.write(if (isBn) "মোট পরিশোধিত টাকা,,$totalPay,\n\n" else "Total Payments Made,,$totalPay,\n\n")

                // Final Balance
                val netBalance = totalOut - totalPay
                writer.write(if (isBn) "বর্তমান বাকি ব্যালেন্স,,$netBalance,\n" else "Current Outstanding Balance,,$netBalance,\n")
            }
        }
        return file
    }

    fun shareCsvFile(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "Open or Share CSV")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
