package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.DatePickerField
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate600
import com.example.ui.util.AppLanguage
import com.example.ui.util.BanglaFormatter
import com.example.ui.util.CsvExporter
import com.example.ui.util.PdfReportGenerator
import com.example.ui.util.ReportType
import com.example.ui.viewmodel.FinancialViewModel
import kotlinx.coroutines.launch

enum class ReportPeriod {
    TODAY,
    THIS_MONTH,
    ALL_TIME,
    CUSTOM
}

@Composable
fun ReportsScreen(
    viewModel: FinancialViewModel,
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    val language by viewModel.language.collectAsStateWithLifecycle()
    val allExpenses by viewModel.expenses.collectAsStateWithLifecycle()
    val allOutstanding by viewModel.outstanding.collectAsStateWithLifecycle()
    val allPayments by viewModel.payments.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isBn = language == AppLanguage.BANGLA
    val scope = rememberCoroutineScope()

    // Profile user information for report & receipt signature
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val signerName = userProfile.name.takeIf { it.isNotBlank() }
        ?: currentUser?.displayName?.takeIf { it.isNotBlank() }
        ?: (if (isBn) "মো: জাহাঙ্গীর আলম" else "Md Jahangir Alam")
    val signerEmail = userProfile.email.ifBlank { currentUser?.email ?: "mdjahangiralam2528@gmail.com" }

    var selectedReportType by remember { mutableStateOf(ReportType.ALL) }
    var selectedPeriod by remember { mutableStateOf(ReportPeriod.THIS_MONTH) }

    var customStartDate by remember { mutableStateOf(BanglaFormatter.getStartOfCurrentMonthIso()) }
    var customEndDate by remember { mutableStateOf(BanglaFormatter.getEndOfCurrentMonthIso()) }

    // Filter data for report
    val filteredData = remember(allExpenses, allOutstanding, allPayments, selectedPeriod, customStartDate, customEndDate) {
        val today = BanglaFormatter.getTodayDhakaIso()
        val startMonth = BanglaFormatter.getStartOfCurrentMonthIso()
        val endMonth = BanglaFormatter.getEndOfCurrentMonthIso()

        when (selectedPeriod) {
            ReportPeriod.TODAY -> Triple(
                allExpenses.filter { it.date == today },
                allOutstanding.filter { it.date == today },
                allPayments.filter { it.date == today }
            )
            ReportPeriod.THIS_MONTH -> Triple(
                allExpenses.filter { it.date in startMonth..endMonth },
                allOutstanding.filter { it.date in startMonth..endMonth },
                allPayments.filter { it.date in startMonth..endMonth }
            )
            ReportPeriod.ALL_TIME -> Triple(
                allExpenses,
                allOutstanding,
                allPayments
            )
            ReportPeriod.CUSTOM -> Triple(
                allExpenses.filter { it.date in customStartDate..customEndDate },
                allOutstanding.filter { it.date in customStartDate..customEndDate },
                allPayments.filter { it.date in customStartDate..customEndDate }
            )
        }
    }

    val reportExpenses = filteredData.first
    val reportOutstanding = filteredData.second
    val reportPayments = filteredData.third

    val totalExp = remember(reportExpenses) { reportExpenses.sumOf { it.amount } }
    val totalOut = remember(reportOutstanding) { reportOutstanding.sumOf { it.amount } }
    val totalPay = remember(reportPayments) { reportPayments.sumOf { it.amount } }
    val netBalance = totalOut - totalPay

    val periodLabel = when (selectedPeriod) {
        ReportPeriod.TODAY -> if (isBn) "আজকের হিসাব (${BanglaFormatter.formatToDisplayDate(BanglaFormatter.getTodayDhakaIso(), true)})" else "Today (${BanglaFormatter.getTodayDhakaIso()})"
        ReportPeriod.THIS_MONTH -> if (isBn) "চলতি মাস" else "Current Month"
        ReportPeriod.ALL_TIME -> if (isBn) "সকল রেকর্ড" else "All Time"
        ReportPeriod.CUSTOM -> if (isBn)
            "${BanglaFormatter.formatToDisplayDate(customStartDate, true)} হতে ${BanglaFormatter.formatToDisplayDate(customEndDate, true)}"
        else
            "$customStartDate to $customEndDate"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Section Header
        Text(
            text = if (isBn) "আর্থিক রিপোর্ট ও প্রিন্ট" else "Financial Reports & Printing",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = Navy800
        )
        Text(
            text = if (isBn) "আদর্শ A4 সাইজ আর্থিক বিবরণী তৈরি, ডাউনলোড ও প্রিন্ট করুন" else "Generate, download and print formal A4 financial statements",
            style = MaterialTheme.typography.bodySmall,
            color = Slate600
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 1. Report Type Selection Cards
        Text(
            text = if (isBn) "রিপোর্টের ধরন নির্বাচন করুন:" else "Select Report Type:",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = Navy800
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val types = listOf(
                ReportType.ALL to if (isBn) "সম্পূর্ণ রিপোর্ট" else "Full Statement",
                ReportType.EXPENSES_ONLY to if (isBn) "বাজার খরচ" else "Expenses Only",
                ReportType.OUTSTANDING_ONLY to if (isBn) "বাকি হিসাব" else "Outstanding"
            )

            for ((t, label) in types) {
                val isSelected = selectedReportType == t
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedReportType = t },
                    label = {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Navy800,
                        selectedLabelColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Period Selection Chips
        Text(
            text = if (isBn) "সময়কাল নির্বাচন করুন:" else "Select Period:",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = Navy800
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val periods = listOf(
                ReportPeriod.TODAY to if (isBn) "আজ" else "Today",
                ReportPeriod.THIS_MONTH to if (isBn) "এই মাস" else "This Month",
                ReportPeriod.ALL_TIME to if (isBn) "সব সময়" else "All Time",
                ReportPeriod.CUSTOM to if (isBn) "কাস্টম" else "Custom"
            )

            for ((p, label) in periods) {
                val isSelected = selectedPeriod == p
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedPeriod = p },
                    label = {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EmeraldPrimary,
                        selectedLabelColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // Custom Date Range pickers
        if (selectedPeriod == ReportPeriod.CUSTOM) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    DatePickerField(
                        label = if (isBn) "শুরুর তারিখ" else "Start Date",
                        selectedIsoDate = customStartDate,
                        onDateSelected = { customStartDate = it },
                        language = language
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    DatePickerField(
                        label = if (isBn) "শেষ তারিখ" else "End Date",
                        selectedIsoDate = customEndDate,
                        onDateSelected = { customEndDate = it },
                        language = language
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 3. Action Buttons: PDF Download, Print, CSV Export
        Text(
            text = if (isBn) "ডাউনলোড ও প্রিন্ট অ্যাকশন:" else "Export & Print Actions:",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = Navy800
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // PDF Download Button
            Button(
                onClick = {
                    try {
                        val pdfFile = PdfReportGenerator.generatePdfFile(
                            context = context,
                            reportType = selectedReportType,
                            periodLabel = periodLabel,
                            expenses = reportExpenses,
                            outstanding = reportOutstanding,
                            payments = reportPayments,
                            language = language,
                            signerName = signerName,
                            signerEmail = signerEmail
                        )
                        PdfReportGenerator.openOrSharePdf(context, pdfFile)
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                if (isBn) "পিডিএফ রিপোর্ট সফলভাবে তৈরি হয়েছে!" else "PDF generated successfully!"
                            )
                        }
                    } catch (e: Exception) {
                        scope.launch {
                            snackbarHostState.showSnackbar("PDF generation error: ${e.message}")
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Navy800),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("pdf_download_button")
            ) {
                Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isBn) "PDF ডাউনলোড" else "PDF Download",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            // Print Button
            Button(
                onClick = {
                    try {
                        val pdfFile = PdfReportGenerator.generatePdfFile(
                            context = context,
                            reportType = selectedReportType,
                            periodLabel = periodLabel,
                            expenses = reportExpenses,
                            outstanding = reportOutstanding,
                            payments = reportPayments,
                            language = language,
                            signerName = signerName,
                            signerEmail = signerEmail
                        )
                        PdfReportGenerator.printPdfFile(context, pdfFile)
                    } catch (e: Exception) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Print error: ${e.message}")
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("print_report_button")
            ) {
                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isBn) "প্রিন্ট করুন" else "Print Statement",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // CSV Export Button
        OutlinedButton(
            onClick = {
                try {
                    val csvFile = if (selectedReportType == ReportType.EXPENSES_ONLY) {
                        CsvExporter.exportMarketExpensesCsv(context, reportExpenses, language)
                    } else {
                        CsvExporter.exportOutstandingCsv(context, reportOutstanding, reportPayments, language)
                    }
                    CsvExporter.shareCsvFile(context, csvFile)
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            if (isBn) "CSV ফাইল প্রস্তুত হয়েছে (Excel-এ খুলুন)" else "CSV exported successfully"
                        )
                    }
                } catch (e: Exception) {
                    scope.launch { snackbarHostState.showSnackbar("CSV error: ${e.message}") }
                }
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("csv_download_button")
        ) {
            Icon(Icons.Default.TableChart, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isBn) "Excel-Compatible CSV ডাউনলোড" else "Download Excel-Compatible CSV",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 4. Live Report Preview (A4 Card Format)
        Text(
            text = if (isBn) "রিপোর্ট প্রিভিউ (A4 ফরম্যাট)" else "Live Report Preview (A4 Paper)",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Navy800
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Slate300, RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Header
                Text(
                    text = if (isBn) "হিসাব খাতা – দৈনিক হিসাব" else "Hisab Khata – Daily Hisab",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Navy900
                )
                Text(
                    text = periodLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate600
                )
                Text(
                    text = if (isBn)
                        "তারিখ: ${BanglaFormatter.formatToDisplayDate(BanglaFormatter.getTodayDhakaIso(), true)}"
                    else
                        "Date: ${BanglaFormatter.formatToDisplayDate(BanglaFormatter.getTodayDhakaIso(), false)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate600
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Summary Numbers
                if (selectedReportType == ReportType.ALL || selectedReportType == ReportType.EXPENSES_ONLY) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Slate100, RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isBn) "সর্বমোট বাজার খরচ:" else "Total Market Expenses:",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = BanglaFormatter.formatCurrency(totalExp, isBn),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = EmeraldDark
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (selectedReportType == ReportType.ALL || selectedReportType == ReportType.OUTSTANDING_ONLY) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Slate100, RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isBn) "মোট বাকি টাকা:" else "Total Outstanding Added:",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = BanglaFormatter.formatCurrency(totalOut, isBn),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFDC2626)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Slate100, RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isBn) "মোট পরিশোধিত টাকা:" else "Total Payments Made:",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = BanglaFormatter.formatCurrency(totalPay, isBn),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = EmeraldDark
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFEEF2FF), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isBn) "চূড়ান্ত বাকি ব্যালেন্স:" else "Current Outstanding Balance:",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = Navy900
                        )
                        Text(
                            text = BanglaFormatter.formatCurrency(netBalance, isBn),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = if (netBalance <= 0) EmeraldDark else Color(0xFFDC2626)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Signature lines preview
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Box(
                            modifier = Modifier
                                .width(130.dp)
                                .height(1.dp)
                                .background(Color.Black)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isBn) "স্বাক্ষর: $signerName" else "Signature: $signerName",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Navy900
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .width(110.dp)
                                .height(1.dp)
                                .background(Color.Black)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isBn) "অনুমোদনকারীর স্বাক্ষর" else "Authorized Signature",
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate600
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
                androidx.compose.material3.HorizontalDivider(color = Slate200, thickness = 0.8.dp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isBn) "হিসাব খাতা v2.0.0 • সর্বস্বত্ব সংরক্ষিত © 2026 মো: জাহাঙ্গীর আলম (+8801778600028)" else "Hisab Khata v2.0.0 • All Rights Reserved © 2026 Md Jahangir Alam (+8801778600028)",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = Slate600,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
