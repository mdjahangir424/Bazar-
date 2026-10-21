package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.OutstandingEntry
import com.example.data.model.PaymentRecord
import com.example.ui.components.DatePickerField
import com.example.ui.components.DateFilterType
import com.example.ui.components.DeleteConfirmationDialog
import com.example.ui.components.EditRecordDialog
import com.example.ui.components.FilterChipsRow
import com.example.ui.theme.CrimsonDanger
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate600
import com.example.ui.util.AppLanguage
import com.example.ui.util.AppStrings
import com.example.ui.util.BanglaFormatter
import com.example.ui.viewmodel.FinancialViewModel
import kotlinx.coroutines.launch

@Composable
fun OutstandingScreen(
    viewModel: FinancialViewModel,
    snackbarHostState: SnackbarHostState
) {
    val language by viewModel.language.collectAsStateWithLifecycle()
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    val allOutstanding by viewModel.outstanding.collectAsStateWithLifecycle()
    val allPayments by viewModel.payments.collectAsStateWithLifecycle()
    val isBn = language == AppLanguage.BANGLA
    val scope = rememberCoroutineScope()

    // Tab: 0 = Outstanding Entries (বাকি খাতা), 1 = Payment Register (পরিশোধের খাতা)
    var selectedTab by remember { mutableIntStateOf(0) }

    // Form inputs for new entry
    var inputAmount by remember { mutableStateOf("") }
    var inputDate by remember { mutableStateOf(BanglaFormatter.getTodayDhakaIso()) }
    var inputNote by remember { mutableStateOf("") }
    var inputError by remember { mutableStateOf<String?>(null) }

    // Filter, search & sort
    var selectedFilter by remember { mutableStateOf(DateFilterType.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var sortOrder by remember { mutableStateOf(SortOrder.DATE_DESC) }

    // Edit and Delete dialog states
    var editingOutstanding by remember { mutableStateOf<OutstandingEntry?>(null) }
    var deletingOutstanding by remember { mutableStateOf<OutstandingEntry?>(null) }
    var editingPayment by remember { mutableStateOf<PaymentRecord?>(null) }
    var deletingPayment by remember { mutableStateOf<PaymentRecord?>(null) }

    // Filtered lists
    val filteredOutstanding = remember(allOutstanding, selectedFilter, searchQuery, sortOrder) {
        val today = BanglaFormatter.getTodayDhakaIso()
        val startWeek = BanglaFormatter.getStartOfCurrentWeekIso()
        val startMonth = BanglaFormatter.getStartOfCurrentMonthIso()
        val endMonth = BanglaFormatter.getEndOfCurrentMonthIso()

        var list = when (selectedFilter) {
            DateFilterType.ALL -> allOutstanding
            DateFilterType.TODAY -> allOutstanding.filter { it.date == today }
            DateFilterType.THIS_WEEK -> allOutstanding.filter { it.date >= startWeek }
            DateFilterType.THIS_MONTH -> allOutstanding.filter { it.date in startMonth..endMonth }
            DateFilterType.CUSTOM -> allOutstanding
        }

        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            list = list.filter { it.note.lowercase().contains(q) || it.amount.toString().contains(q) }
        }

        when (sortOrder) {
            SortOrder.DATE_DESC -> list.sortedWith(compareByDescending<OutstandingEntry> { it.date }.thenByDescending { it.id })
            SortOrder.DATE_ASC -> list.sortedWith(compareBy<OutstandingEntry> { it.date }.thenBy { it.id })
            SortOrder.AMOUNT_DESC -> list.sortedByDescending { it.amount }
            SortOrder.AMOUNT_ASC -> list.sortedBy { it.amount }
        }
    }

    val filteredPayments = remember(allPayments, selectedFilter, searchQuery, sortOrder) {
        val today = BanglaFormatter.getTodayDhakaIso()
        val startWeek = BanglaFormatter.getStartOfCurrentWeekIso()
        val startMonth = BanglaFormatter.getStartOfCurrentMonthIso()
        val endMonth = BanglaFormatter.getEndOfCurrentMonthIso()

        var list = when (selectedFilter) {
            DateFilterType.ALL -> allPayments
            DateFilterType.TODAY -> allPayments.filter { it.date == today }
            DateFilterType.THIS_WEEK -> allPayments.filter { it.date >= startWeek }
            DateFilterType.THIS_MONTH -> allPayments.filter { it.date in startMonth..endMonth }
            DateFilterType.CUSTOM -> allPayments
        }

        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            list = list.filter { it.note.lowercase().contains(q) || it.amount.toString().contains(q) }
        }

        when (sortOrder) {
            SortOrder.DATE_DESC -> list.sortedWith(compareByDescending<PaymentRecord> { it.date }.thenByDescending { it.id })
            SortOrder.DATE_ASC -> list.sortedWith(compareBy<PaymentRecord> { it.date }.thenBy { it.id })
            SortOrder.AMOUNT_DESC -> list.sortedByDescending { it.amount }
            SortOrder.AMOUNT_ASC -> list.sortedBy { it.amount }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 1. Top Summary Banner: Added - Paid = Net Balance
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Navy800),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = AppStrings.totalOutstandingBalance(language),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFFCBD5E1)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = BanglaFormatter.formatCurrency(summary.totalOutstandingBalance, isBn),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isBn)
                                "মোট বাকি: ${BanglaFormatter.formatCurrency(summary.totalOutstandingAdded, true)}"
                            else
                                "Total Due: ${BanglaFormatter.formatCurrency(summary.totalOutstandingAdded, false)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFCA5A5)
                        )
                        Text(
                            text = if (isBn)
                                "মোট পরিশোধ: ${BanglaFormatter.formatCurrency(summary.totalPaymentsMade, true)}"
                            else
                                "Total Paid: ${BanglaFormatter.formatCurrency(summary.totalPaymentsMade, false)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF86EFAC)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Tab Navigation between Outstanding and Payments
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Slate100,
                contentColor = Navy800
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        inputAmount = ""
                        inputNote = ""
                        inputError = null
                    },
                    text = {
                        Text(
                            text = if (isBn) "১. বাকি খাতা (Outstanding)" else "1. Outstanding Due",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        inputAmount = ""
                        inputNote = ""
                        inputError = null
                    },
                    text = {
                        Text(
                            text = if (isBn) "২. বাকি পরিশোধ (Payments)" else "2. Payments Made",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Entry Form Card for Current Tab
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (selectedTab == 0) {
                            if (isBn) "আজকের নতুন বাকি যোগ করুন" else "Record New Outstanding Due"
                        } else {
                            if (isBn) "বাকি পরিশোধের তথ্য যোগ করুন" else "Record Payment / Repayment"
                        },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Navy800
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    DatePickerField(
                        label = AppStrings.date(language),
                        selectedIsoDate = inputDate,
                        onDateSelected = { inputDate = it },
                        language = language
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = inputAmount,
                        onValueChange = {
                            inputAmount = it
                            inputError = null
                        },
                        label = {
                            Text(
                                if (selectedTab == 0)
                                    (if (isBn) "বাকি টাকার পরিমাণ (৳)" else "Outstanding Amount (BDT)")
                                else
                                    (if (isBn) "পরিশোধের টাকার পরিমাণ (৳)" else "Payment Amount (BDT)")
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = inputError != null,
                        supportingText = inputError?.let { { Text(it, color = CrimsonDanger) } },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(if (selectedTab == 0) "outstanding_amount_input" else "payment_amount_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = inputNote,
                        onValueChange = { inputNote = it },
                        label = { Text(AppStrings.note(language)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(if (selectedTab == 0) "outstanding_note_input" else "payment_note_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val parsedAmount = inputAmount.toDoubleOrNull()
                            if (parsedAmount == null || parsedAmount <= 0) {
                                inputError = if (isBn) "সঠিক টাকার পরিমাণ লিখুন (০ এর বেশি)" else "Enter valid amount (> 0)"
                                return@Button
                            }

                            if (selectedTab == 0) {
                                viewModel.addOutstanding(
                                    amount = parsedAmount,
                                    date = inputDate,
                                    note = inputNote,
                                    onSuccess = {
                                        inputAmount = ""
                                        inputNote = ""
                                        inputError = null
                                        scope.launch {
                                            snackbarHostState.showSnackbar(AppStrings.outstandingSavedSuccess(language))
                                        }
                                    },
                                    onError = { err -> inputError = err }
                                )
                            } else {
                                viewModel.addPayment(
                                    amount = parsedAmount,
                                    date = inputDate,
                                    note = inputNote,
                                    onSuccess = {
                                        inputAmount = ""
                                        inputNote = ""
                                        inputError = null
                                        scope.launch {
                                            snackbarHostState.showSnackbar(AppStrings.paymentSavedSuccess(language))
                                        }
                                    },
                                    onError = { err -> inputError = err }
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTab == 0) CrimsonDanger else EmeraldPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag(if (selectedTab == 0) "save_outstanding_button" else "save_payment_button")
                    ) {
                        Icon(
                            imageVector = if (selectedTab == 0) Icons.Default.Receipt else Icons.Default.Payment,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = AppStrings.save(language),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Filters & Search
            FilterChipsRow(
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it },
                language = language
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(AppStrings.searchPlaceholder(language), fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Slate600) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                IconButton(
                    onClick = {
                        sortOrder = when (sortOrder) {
                            SortOrder.DATE_DESC -> SortOrder.DATE_ASC
                            SortOrder.DATE_ASC -> SortOrder.AMOUNT_DESC
                            SortOrder.AMOUNT_DESC -> SortOrder.AMOUNT_ASC
                            SortOrder.AMOUNT_ASC -> SortOrder.DATE_DESC
                        }
                    },
                    modifier = Modifier
                        .background(Slate100, RoundedCornerShape(12.dp))
                        .size(48.dp)
                ) {
                    Icon(imageVector = Icons.Default.Sort, contentDescription = "Sort", tint = Navy800)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5. Excel Table Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 0.dp, bottomEnd = 0.dp),
                colors = CardDefaults.cardColors(containerColor = Navy800)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = AppStrings.serial(language),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        modifier = Modifier.width(42.dp)
                    )
                    Text(
                        text = AppStrings.date(language),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        modifier = Modifier.width(82.dp)
                    )
                    Text(
                        text = if (selectedTab == 0)
                            (if (isBn) "বাকি টাকা" else "Due Amount")
                        else
                            (if (isBn) "পরিশোধ" else "Payment"),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = AppStrings.actions(language),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        modifier = Modifier.width(68.dp)
                    )
                }
            }
        }

        // 6. Excel Rows
        if (selectedTab == 0) {
            if (filteredOutstanding.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .border(1.dp, Slate200)
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = AppStrings.noRecordsFound(language), color = Slate600)
                    }
                }
            } else {
                itemsIndexed(filteredOutstanding, key = { _, item -> item.id }) { index, item ->
                    val isEven = index % 2 == 0
                    val rowBg = if (isEven) Color.White else Slate100

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(rowBg)
                            .border(0.5.dp, Slate200)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val sl = if (isBn) BanglaFormatter.toBanglaDigits("${index + 1}") else "${index + 1}"
                            Text(
                                text = sl,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = Slate600,
                                modifier = Modifier.width(42.dp)
                            )

                            val dt = BanglaFormatter.formatToDisplayDate(item.date, isBn)
                            Text(
                                text = dt,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.width(82.dp)
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = BanglaFormatter.formatCurrency(item.amount, isBn),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = CrimsonDanger
                                )
                                if (item.note.isNotBlank()) {
                                    Text(
                                        text = item.note,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = Slate600
                                    )
                                }
                            }

                            Row(modifier = Modifier.width(68.dp)) {
                                IconButton(
                                    onClick = { editingOutstanding = item },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        tint = Navy800,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { deletingOutstanding = item },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = CrimsonDanger,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Payment Register items
            if (filteredPayments.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .border(1.dp, Slate200)
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = AppStrings.noRecordsFound(language), color = Slate600)
                    }
                }
            } else {
                itemsIndexed(filteredPayments, key = { _, item -> item.id }) { index, item ->
                    val isEven = index % 2 == 0
                    val rowBg = if (isEven) Color.White else Slate100

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(rowBg)
                            .border(0.5.dp, Slate200)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val sl = if (isBn) BanglaFormatter.toBanglaDigits("${index + 1}") else "${index + 1}"
                            Text(
                                text = sl,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = Slate600,
                                modifier = Modifier.width(42.dp)
                            )

                            val dt = BanglaFormatter.formatToDisplayDate(item.date, isBn)
                            Text(
                                text = dt,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.width(82.dp)
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = BanglaFormatter.formatCurrency(item.amount, isBn),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = EmeraldDark
                                )
                                if (item.note.isNotBlank()) {
                                    Text(
                                        text = item.note,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = Slate600
                                    )
                                }
                            }

                            Row(modifier = Modifier.width(68.dp)) {
                                IconButton(
                                    onClick = { editingPayment = item },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        tint = Navy800,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { deletingPayment = item },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = CrimsonDanger,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 7. Table Bottom Summary
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 12.dp, bottomEnd = 12.dp),
                colors = CardDefaults.cardColors(containerColor = Slate100),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate300)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isBn) "মোট বাকি এন্ট্রি:" else "Total Outstanding Incurred:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Slate600
                        )
                        Text(
                            text = BanglaFormatter.formatCurrency(summary.totalOutstandingAdded, isBn),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = CrimsonDanger
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isBn) "মোট পরিশোধিত টাকা:" else "Total Payments Made:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Slate600
                        )
                        Text(
                            text = BanglaFormatter.formatCurrency(summary.totalPaymentsMade, isBn),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = EmeraldDark
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isBn) "বর্তমান বাকি ব্যালেন্স:" else "Current Outstanding Balance:",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Navy800
                        )
                        Text(
                            text = BanglaFormatter.formatCurrency(summary.totalOutstandingBalance, isBn),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = if (summary.totalOutstandingBalance <= 0) EmeraldDark else CrimsonDanger
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Edit Outstanding Dialog
    editingOutstanding?.let { item ->
        EditRecordDialog(
            visible = true,
            title = if (isBn) "বাকি এন্ট্রি সম্পাদনা" else "Edit Outstanding Entry",
            initialAmount = item.amount,
            initialDate = item.date,
            initialNote = item.note,
            language = language,
            onDismiss = { editingOutstanding = null },
            onSave = { amt, dt, nt ->
                viewModel.updateOutstanding(item.copy(amount = amt, date = dt, note = nt))
                editingOutstanding = null
            }
        )
    }

    // Delete Outstanding Dialog
    deletingOutstanding?.let { item ->
        DeleteConfirmationDialog(
            visible = true,
            language = language,
            onConfirm = {
                viewModel.deleteOutstanding(item)
                deletingOutstanding = null
            },
            onDismiss = { deletingOutstanding = null }
        )
    }

    // Edit Payment Dialog
    editingPayment?.let { item ->
        EditRecordDialog(
            visible = true,
            title = if (isBn) "পরিশোধ সম্পাদনা" else "Edit Payment Record",
            initialAmount = item.amount,
            initialDate = item.date,
            initialNote = item.note,
            language = language,
            onDismiss = { editingPayment = null },
            onSave = { amt, dt, nt ->
                viewModel.updatePayment(
                    payment = item.copy(amount = amt, date = dt, note = nt),
                    onSuccess = { editingPayment = null },
                    onError = { err ->
                        scope.launch { snackbarHostState.showSnackbar(err) }
                    }
                )
            }
        )
    }

    // Delete Payment Dialog
    deletingPayment?.let { item ->
        DeleteConfirmationDialog(
            visible = true,
            language = language,
            onConfirm = {
                viewModel.deletePayment(item)
                deletingPayment = null
            },
            onDismiss = { deletingPayment = null }
        )
    }
}
