package com.example.ui.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import androidx.core.content.FileProvider
import com.example.data.model.MarketExpense
import com.example.data.model.OutstandingEntry
import com.example.data.model.PaymentRecord
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

enum class ReportType {
    ALL,
    EXPENSES_ONLY,
    OUTSTANDING_ONLY
}

object PdfReportGenerator {

    /**
     * Generate an A4 PDF document containing financial tables and summary.
     */
    fun generatePdfFile(
        context: Context,
        reportType: ReportType,
        periodLabel: String,
        expenses: List<MarketExpense>,
        outstanding: List<OutstandingEntry>,
        payments: List<PaymentRecord>,
        language: AppLanguage,
        signerName: String = "মো: জাহাঙ্গীর আলম",
        signerEmail: String = "mdjahangiralam2528@gmail.com"
    ): File {
        val document = PdfDocument()
        val pageWidth = 595 // A4 standard width in points (72 dpi)
        val pageHeight = 842 // A4 standard height in points (72 dpi)
        val margin = 36f

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas

        val textPaint = Paint().apply {
            color = Color.BLACK
            isAntiAlias = true
            textSize = 10f
        }
        val boldPaint = Paint().apply {
            color = Color.BLACK
            isAntiAlias = true
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val headerPaint = Paint().apply {
            color = Color.rgb(15, 39, 68) // Deep Navy
            isAntiAlias = true
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val subHeaderPaint = Paint().apply {
            color = Color.DKGRAY
            isAntiAlias = true
            textSize = 10f
        }
        val borderPaint = Paint().apply {
            color = Color.LTGRAY
            style = Paint.Style.STROKE
            strokeWidth = 0.8f
        }
        val tableHeaderBgPaint = Paint().apply {
            color = Color.rgb(241, 245, 249) // Slate 100
            style = Paint.Style.FILL
        }

        var y = margin + 15f

        // Helper to check page bounds and create new page if needed
        fun checkNewPage(neededHeight: Float) {
            if (y + neededHeight > pageHeight - margin - 40f) {
                // Draw footer on current page
                drawPageFooter(canvas, pageNumber, pageWidth, pageHeight, margin, textPaint, signerName)
                document.finishPage(page)

                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                y = margin + 20f
            }
        }

        // Title Header
        val isBn = language == AppLanguage.BANGLA
        val title = if (isBn) "হিসাব খাতা – দৈনিক হিসাব" else "Hisab Khata – Daily Hisab"
        val subtitle = when (reportType) {
            ReportType.ALL -> if (isBn) "সম্পূর্ণ আর্থিক বিবরণী ও রিপোর্ট" else "Complete Financial Statement"
            ReportType.EXPENSES_ONLY -> if (isBn) "দৈনিক বাজার খরচের রিপোর্ট" else "Daily Market Expenses Statement"
            ReportType.OUTSTANDING_ONLY -> if (isBn) "বাকি ও পরিশোধ হিসাব বিবরণী" else "Outstanding & Payment Statement"
        }
        val dateLabel = (if (isBn) "রিপোর্ট প্রস্তুতের তারিখ: " else "Generated on: ") +
                BanglaFormatter.formatToDisplayDate(BanglaFormatter.getTodayDhakaIso(), isBn)
        val periodText = (if (isBn) "সময়কাল: " else "Period: ") + periodLabel

        canvas.drawText(title, margin, y, headerPaint)
        y += 18f
        canvas.drawText(subtitle, margin, y, boldPaint)
        y += 14f
        canvas.drawText(periodText, margin, y, subHeaderPaint)
        y += 14f
        canvas.drawText(dateLabel, margin, y, subHeaderPaint)
        y += 18f

        // Thin separator
        canvas.drawLine(margin, y, pageWidth - margin, y, borderPaint)
        y += 15f

        val tableWidth = pageWidth - (2 * margin)

        // 1. Market Expenses Table (if requested)
        if (reportType == ReportType.ALL || reportType == ReportType.EXPENSES_ONLY) {
            checkNewPage(60f)
            val sectionTitle = if (isBn) "১. বাজার খরচের হিসাব (Market Expenses)" else "1. Market Expenses Register"
            canvas.drawText(sectionTitle, margin, y, boldPaint)
            y += 14f

            // Table Header: Sl (40), Date (80), Note (tableWidth - 210), Amount (90)
            val colSl = 35f
            val colDate = 75f
            val colAmount = 85f
            val colNote = tableWidth - colSl - colDate - colAmount

            // Draw Header Row
            canvas.drawRect(margin, y - 10f, margin + tableWidth, y + 8f, tableHeaderBgPaint)
            canvas.drawRect(margin, y - 10f, margin + tableWidth, y + 8f, borderPaint)

            var x = margin + 5f
            canvas.drawText(if (isBn) "ক্রমিক" else "Sl.", x, y + 4f, boldPaint)
            x += colSl
            canvas.drawText(if (isBn) "তারিখ" else "Date", x, y + 4f, boldPaint)
            x += colDate
            canvas.drawText(if (isBn) "বিবরণ" else "Note", x, y + 4f, boldPaint)
            x += colNote
            canvas.drawText(if (isBn) "টাকা (BDT)" else "Amount", x, y + 4f, boldPaint)
            y += 18f

            var totalExp = 0.0
            if (expenses.isEmpty()) {
                checkNewPage(20f)
                canvas.drawRect(margin, y - 10f, margin + tableWidth, y + 8f, borderPaint)
                canvas.drawText(if (isBn) "কোনো বাজার খরচ নেই" else "No market expenses recorded", margin + 10f, y + 3f, textPaint)
                y += 18f
            } else {
                for ((idx, item) in expenses.withIndex()) {
                    checkNewPage(20f)
                    totalExp += item.amount
                    val rowBg = if (idx % 2 == 1) tableHeaderBgPaint else Paint().apply { color = Color.WHITE }
                    canvas.drawRect(margin, y - 10f, margin + tableWidth, y + 8f, rowBg)
                    canvas.drawRect(margin, y - 10f, margin + tableWidth, y + 8f, borderPaint)

                    var rx = margin + 5f
                    val slText = if (isBn) BanglaFormatter.toBanglaDigits("${idx + 1}") else "${idx + 1}"
                    canvas.drawText(slText, rx, y + 3f, textPaint)
                    rx += colSl
                    val dtText = BanglaFormatter.formatToDisplayDate(item.date, isBn)
                    canvas.drawText(dtText, rx, y + 3f, textPaint)
                    rx += colDate
                    val noteText = if (item.note.isNotBlank()) item.note.take(28) else "-"
                    canvas.drawText(noteText, rx, y + 3f, textPaint)
                    rx += colNote
                    val amtText = BanglaFormatter.formatCurrency(item.amount, isBn)
                    canvas.drawText(amtText, rx, y + 3f, boldPaint)
                    y += 18f
                }
            }

            // Total row
            checkNewPage(24f)
            canvas.drawRect(margin, y - 10f, margin + tableWidth, y + 10f, tableHeaderBgPaint)
            canvas.drawRect(margin, y - 10f, margin + tableWidth, y + 10f, borderPaint)
            canvas.drawText(if (isBn) "সর্বমোট বাজার খরচ:" else "Total Market Expenses:", margin + 15f, y + 4f, boldPaint)
            val grandExpText = BanglaFormatter.formatCurrency(totalExp, isBn)
            canvas.drawText(grandExpText, margin + tableWidth - colAmount, y + 4f, boldPaint)
            y += 28f
        }

        // 2. Outstanding Entries Table
        if (reportType == ReportType.ALL || reportType == ReportType.OUTSTANDING_ONLY) {
            checkNewPage(60f)
            val sectionTitle = if (isBn) "২. নতুন বাকি হিসাব (Outstanding Entries)" else "2. Outstanding Incurred Register"
            canvas.drawText(sectionTitle, margin, y, boldPaint)
            y += 14f

            val colSl = 35f
            val colDate = 75f
            val colAmount = 85f
            val colNote = tableWidth - colSl - colDate - colAmount

            canvas.drawRect(margin, y - 10f, margin + tableWidth, y + 8f, tableHeaderBgPaint)
            canvas.drawRect(margin, y - 10f, margin + tableWidth, y + 8f, borderPaint)

            var x = margin + 5f
            canvas.drawText(if (isBn) "ক্রমিক" else "Sl.", x, y + 4f, boldPaint)
            x += colSl
            canvas.drawText(if (isBn) "তারিখ" else "Date", x, y + 4f, boldPaint)
            x += colDate
            canvas.drawText(if (isBn) "বিবরণ" else "Note", x, y + 4f, boldPaint)
            x += colNote
            canvas.drawText(if (isBn) "বাকি টাকা" else "Amount", x, y + 4f, boldPaint)
            y += 18f

            var totalOut = 0.0
            if (outstanding.isEmpty()) {
                checkNewPage(20f)
                canvas.drawRect(margin, y - 10f, margin + tableWidth, y + 8f, borderPaint)
                canvas.drawText(if (isBn) "কোনো বাকি এন্ট্রি নেই" else "No outstanding records", margin + 10f, y + 3f, textPaint)
                y += 18f
            } else {
                for ((idx, item) in outstanding.withIndex()) {
                    checkNewPage(20f)
                    totalOut += item.amount
                    val rowBg = if (idx % 2 == 1) tableHeaderBgPaint else Paint().apply { color = Color.WHITE }
                    canvas.drawRect(margin, y - 10f, margin + tableWidth, y + 8f, rowBg)
                    canvas.drawRect(margin, y - 10f, margin + tableWidth, y + 8f, borderPaint)

                    var rx = margin + 5f
                    val slText = if (isBn) BanglaFormatter.toBanglaDigits("${idx + 1}") else "${idx + 1}"
                    canvas.drawText(slText, rx, y + 3f, textPaint)
                    rx += colSl
                    val dtText = BanglaFormatter.formatToDisplayDate(item.date, isBn)
                    canvas.drawText(dtText, rx, y + 3f, textPaint)
                    rx += colDate
                    val noteText = if (item.note.isNotBlank()) item.note.take(28) else "-"
                    canvas.drawText(noteText, rx, y + 3f, textPaint)
                    rx += colNote
                    val amtText = BanglaFormatter.formatCurrency(item.amount, isBn)
                    canvas.drawText(amtText, rx, y + 3f, boldPaint)
                    y += 18f
                }
            }

            checkNewPage(24f)
            canvas.drawRect(margin, y - 10f, margin + tableWidth, y + 10f, tableHeaderBgPaint)
            canvas.drawRect(margin, y - 10f, margin + tableWidth, y + 10f, borderPaint)
            canvas.drawText(if (isBn) "মোট বাকি টাকা:" else "Total Outstanding Added:", margin + 15f, y + 4f, boldPaint)
            val grandOutText = BanglaFormatter.formatCurrency(totalOut, isBn)
            canvas.drawText(grandOutText, margin + tableWidth - colAmount, y + 4f, boldPaint)
            y += 28f

            // 3. Payment Register Table
            checkNewPage(60f)
            val payTitle = if (isBn) "৩. বাকি পরিশোধের হিসাব (Payment / Repayments)" else "3. Payments & Repayments Register"
            canvas.drawText(payTitle, margin, y, boldPaint)
            y += 14f

            canvas.drawRect(margin, y - 10f, margin + tableWidth, y + 8f, tableHeaderBgPaint)
            canvas.drawRect(margin, y - 10f, margin + tableWidth, y + 8f, borderPaint)

            x = margin + 5f
            canvas.drawText(if (isBn) "ক্রমিক" else "Sl.", x, y + 4f, boldPaint)
            x += colSl
            canvas.drawText(if (isBn) "তারিখ" else "Date", x, y + 4f, boldPaint)
            x += colDate
            canvas.drawText(if (isBn) "বিবরণ" else "Note", x, y + 4f, boldPaint)
            x += colNote
            canvas.drawText(if (isBn) "পরিশোধ" else "Payment", x, y + 4f, boldPaint)
            y += 18f

            var totalPay = 0.0
            if (payments.isEmpty()) {
                checkNewPage(20f)
                canvas.drawRect(margin, y - 10f, margin + tableWidth, y + 8f, borderPaint)
                canvas.drawText(if (isBn) "কোনো পরিশোধের রেকর্ড নেই" else "No payment records", margin + 10f, y + 3f, textPaint)
                y += 18f
            } else {
                for ((idx, item) in payments.withIndex()) {
                    checkNewPage(20f)
                    totalPay += item.amount
                    val rowBg = if (idx % 2 == 1) tableHeaderBgPaint else Paint().apply { color = Color.WHITE }
                    canvas.drawRect(margin, y - 10f, margin + tableWidth, y + 8f, rowBg)
                    canvas.drawRect(margin, y - 10f, margin + tableWidth, y + 8f, borderPaint)

                    var rx = margin + 5f
                    val slText = if (isBn) BanglaFormatter.toBanglaDigits("${idx + 1}") else "${idx + 1}"
                    canvas.drawText(slText, rx, y + 3f, textPaint)
                    rx += colSl
                    val dtText = BanglaFormatter.formatToDisplayDate(item.date, isBn)
                    canvas.drawText(dtText, rx, y + 3f, textPaint)
                    rx += colDate
                    val noteText = if (item.note.isNotBlank()) item.note.take(28) else "-"
                    canvas.drawText(noteText, rx, y + 3f, textPaint)
                    rx += colNote
                    val amtText = BanglaFormatter.formatCurrency(item.amount, isBn)
                    canvas.drawText(amtText, rx, y + 3f, boldPaint)
                    y += 18f
                }
            }

            checkNewPage(24f)
            canvas.drawRect(margin, y - 10f, margin + tableWidth, y + 10f, tableHeaderBgPaint)
            canvas.drawRect(margin, y - 10f, margin + tableWidth, y + 10f, borderPaint)
            canvas.drawText(if (isBn) "মোট পরিশোধিত টাকা:" else "Total Payments Made:", margin + 15f, y + 4f, boldPaint)
            val grandPayText = BanglaFormatter.formatCurrency(totalPay, isBn)
            canvas.drawText(grandPayText, margin + tableWidth - colAmount, y + 4f, boldPaint)
            y += 28f

            // 4. Outstanding Summary Box
            checkNewPage(50f)
            val netBalance = totalOut - totalPay
            val summaryBg = Paint().apply {
                color = Color.rgb(238, 242, 255) // Indigo tint
                style = Paint.Style.FILL
            }
            canvas.drawRect(margin, y - 10f, margin + tableWidth, y + 32f, summaryBg)
            canvas.drawRect(margin, y - 10f, margin + tableWidth, y + 32f, borderPaint)

            val balTitle = if (isBn) "চূড়ান্ত বাকি ব্যালেন্স (Current Outstanding Balance):" else "Current Outstanding Balance (Added - Paid):"
            val balValue = BanglaFormatter.formatCurrency(netBalance, isBn)
            canvas.drawText(balTitle, margin + 15f, y + 10f, boldPaint)
            canvas.drawText(balValue, margin + tableWidth - 110f, y + 10f, headerPaint)
            y += 48f
        }

        // Signature Line
        checkNewPage(95f)
        y += 25f
        val sigLinePaint = Paint().apply {
            color = Color.BLACK
            strokeWidth = 1f
        }
        val sigLeft = margin + 20f
        val sigRight = pageWidth - margin - 150f

        canvas.drawLine(sigLeft, y, sigLeft + 160f, y, sigLinePaint)
        canvas.drawText(if (isBn) "স্বাক্ষর: $signerName" else "Signature: $signerName", sigLeft + 5f, y + 14f, subHeaderPaint)
        if (signerEmail.isNotBlank()) {
            canvas.drawText(if (isBn) "গুগল ক্লাউড আইডি: $signerEmail" else "Google Cloud ID: $signerEmail", sigLeft + 5f, y + 26f, subHeaderPaint)
            canvas.drawText(if (isBn) "প্রস্তুতকারক (Google Cloud Login)" else "Prepared by (Google Cloud)", sigLeft + 5f, y + 38f, subHeaderPaint)
        } else {
            canvas.drawText(if (isBn) "প্রস্তুতকারক (হিসাব খাতা)" else "Prepared by", sigLeft + 5f, y + 26f, subHeaderPaint)
        }

        canvas.drawLine(sigRight, y, sigRight + 130f, y, sigLinePaint)
        canvas.drawText(if (isBn) "অনুমোদনকারীর স্বাক্ষর" else "Authorized Signature", sigRight + 10f, y + 14f, subHeaderPaint)

        // Draw footer on last page
        drawPageFooter(canvas, pageNumber, pageWidth, pageHeight, margin, textPaint, signerName)
        document.finishPage(page)

        // Save PDF to cache
        val timestamp = System.currentTimeMillis()
        val filePrefix = when (reportType) {
            ReportType.ALL -> "Full_Financial_Report"
            ReportType.EXPENSES_ONLY -> "Market_Expense_Report"
            ReportType.OUTSTANDING_ONLY -> "Outstanding_Report"
        }
        val fileName = "${filePrefix}_${BanglaFormatter.getTodayDhakaIso()}_$timestamp.pdf"
        val cacheDir = context.cacheDir
        val pdfFile = File(cacheDir, fileName)

        val outputStream = FileOutputStream(pdfFile)
        document.writeTo(outputStream)
        outputStream.flush()
        outputStream.close()
        document.close()

        return pdfFile
    }

    private fun drawPageFooter(
        canvas: Canvas,
        pageNumber: Int,
        pageWidth: Int,
        pageHeight: Int,
        margin: Float,
        textPaint: Paint,
        signerName: String = ""
    ) {
        val footerText = if (signerName.isNotBlank()) {
            "হিসাব খাতা | প্রস্তুতকারী (Google Cloud): $signerName | পৃষ্ঠা $pageNumber"
        } else {
            "হিসাব খাতা | পৃষ্ঠা $pageNumber"
        }
        val textBounds = Rect()
        textPaint.getTextBounds(footerText, 0, footerText.length, textBounds)
        val fx = (pageWidth - textBounds.width()) / 2f
        val fy = pageHeight - margin + 10f
        canvas.drawText(footerText, fx, fy, textPaint)
    }

    /**
     * Generate an official Payment Receipt / রশিদ (Money Voucher) PDF with Google Cloud user attribution
     */
    fun generateReceiptPdf(
        context: Context,
        receiptId: String,
        date: String,
        title: String,
        amount: Double,
        note: String,
        signerName: String,
        signerEmail: String,
        language: AppLanguage
    ): File {
        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 420 // A5 landscape format for receipt
        val margin = 32f

        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val isBn = language == AppLanguage.BANGLA

        // Border
        val borderPaint = Paint().apply {
            color = Color.rgb(15, 39, 68)
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
        }
        canvas.drawRect(margin, margin, pageWidth - margin, pageHeight - margin, borderPaint)

        // Header Background
        val headerBg = Paint().apply {
            color = Color.rgb(241, 245, 249)
            style = Paint.Style.FILL
        }
        canvas.drawRect(margin + 2f, margin + 2f, pageWidth - margin - 2f, margin + 65f, headerBg)

        val titlePaint = Paint().apply {
            color = Color.rgb(15, 39, 68)
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val subPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 10f
            isAntiAlias = true
        }
        val boldPaint = Paint().apply {
            color = Color.BLACK
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val bodyPaint = Paint().apply {
            color = Color.BLACK
            textSize = 11f
            isAntiAlias = true
        }

        // Header Text
        val receiptHeading = if (isBn) "হিসাব খাতা – টাকা প্রাপ্তি / পরিশোধের রশিদ" else "Hisab Khata – Payment Receipt"
        canvas.drawText(receiptHeading, margin + 15f, margin + 28f, titlePaint)

        val recNoText = (if (isBn) "রশিদ নং: #" else "Receipt No: #") + receiptId.takeLast(6).uppercase()
        val dateText = (if (isBn) "তারিখ: " else "Date: ") + BanglaFormatter.formatToDisplayDate(date, isBn)
        canvas.drawText("$recNoText   |   $dateText", margin + 15f, margin + 48f, subPaint)

        // Content
        var cy = margin + 95f
        val lineSpacing = 26f

        canvas.drawText(if (isBn) "হিসাবের খাত / বিবরণ:" else "Description:", margin + 25f, cy, subPaint)
        canvas.drawText(title, margin + 170f, cy, boldPaint)
        cy += lineSpacing

        if (note.isNotBlank()) {
            canvas.drawText(if (isBn) "মন্তব্য / নোট:" else "Note / Reference:", margin + 25f, cy, subPaint)
            canvas.drawText(note, margin + 170f, cy, bodyPaint)
            cy += lineSpacing
        }

        // Amount Box
        val amountBg = Paint().apply {
            color = Color.rgb(236, 253, 245)
            style = Paint.Style.FILL
        }
        val amountBorder = Paint().apply {
            color = Color.rgb(16, 185, 129)
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        canvas.drawRect(margin + 25f, cy - 5f, pageWidth - margin - 25f, cy + 35f, amountBg)
        canvas.drawRect(margin + 25f, cy - 5f, pageWidth - margin - 25f, cy + 35f, amountBorder)

        val amountPaint = Paint().apply {
            color = Color.rgb(6, 95, 70)
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val amountText = (if (isBn) "পরিশোধকৃত টাকা: " else "Amount: ") + BanglaFormatter.formatCurrency(amount, isBn)
        canvas.drawText(amountText, margin + 38f, cy + 20f, amountPaint)

        // Signature section
        val sigY = pageHeight - margin - 45f
        val sigLinePaint = Paint().apply {
            color = Color.BLACK
            strokeWidth = 1f
        }

        val sigLeft = margin + 25f
        val sigRight = pageWidth - margin - 170f

        // Left Signature: Logged-in Google Cloud user
        canvas.drawLine(sigLeft, sigY, sigLeft + 160f, sigY, sigLinePaint)
        canvas.drawText(if (isBn) "স্বাক্ষর: $signerName" else "Signature: $signerName", sigLeft + 5f, sigY + 14f, subPaint)
        if (signerEmail.isNotBlank()) {
            canvas.drawText(if (isBn) "গুগল ক্লাউড আইডি: $signerEmail" else "Google Cloud ID: $signerEmail", sigLeft + 5f, sigY + 26f, subPaint)
            canvas.drawText(if (isBn) "আদায়কারী / প্রস্তুতকারক" else "Prepared by (Google Cloud)", sigLeft + 5f, sigY + 38f, subPaint)
        } else {
            canvas.drawText(if (isBn) "আদায়কারী / প্রস্তুতকারক" else "Prepared by", sigLeft + 5f, sigY + 26f, subPaint)
        }

        // Right Signature: Authorized
        canvas.drawLine(sigRight, sigY, sigRight + 140f, sigY, sigLinePaint)
        canvas.drawText(if (isBn) "অনুমোদনকারীর স্বাক্ষর" else "Authorized Signature", sigRight + 10f, sigY + 14f, subPaint)

        document.finishPage(page)

        val timestamp = System.currentTimeMillis()
        val fileName = "Receipt_${receiptId.takeLast(6)}_$timestamp.pdf"
        val pdfFile = File(context.cacheDir, fileName)
        val outputStream = FileOutputStream(pdfFile)
        document.writeTo(outputStream)
        outputStream.flush()
        outputStream.close()
        document.close()

        return pdfFile
    }

    /**
     * Share or View generated PDF via Android system chooser
     */
    fun openOrSharePdf(context: Context, pdfFile: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, "Share or Save PDF")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    /**
     * Print the PDF document using Android PrintManager
     */
    fun printPdfFile(context: Context, pdfFile: File, jobName: String = "Hisab Khata Statement") {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
        val printAdapter = object : PrintDocumentAdapter() {
            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes?,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback?,
                extras: Bundle?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback?.onLayoutCancelled()
                    return
                }
                val info = PrintDocumentInfo.Builder(jobName)
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .build()
                callback?.onLayoutFinished(info, true)
            }

            override fun onWrite(
                pages: Array<out PageRange>?,
                destination: ParcelFileDescriptor?,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback?
            ) {
                var input: FileInputStream? = null
                var output: FileOutputStream? = null
                try {
                    input = FileInputStream(pdfFile)
                    output = FileOutputStream(destination?.fileDescriptor)
                    val buffer = ByteArray(1024)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } >= 0) {
                        output.write(buffer, 0, bytesRead)
                    }
                    callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                } catch (e: Exception) {
                    callback?.onWriteFailed(e.message)
                } finally {
                    try {
                        input?.close()
                        output?.close()
                    } catch (e: IOException) {
                        // ignore
                    }
                }
            }
        }
        printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
    }
}
