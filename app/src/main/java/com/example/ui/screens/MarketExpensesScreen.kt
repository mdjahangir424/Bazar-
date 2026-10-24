package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.MarketExpense
import com.example.ui.components.DatePickerField
import com.example.ui.components.DateFilterType
import com.example.ui.components.DeleteConfirmationDialog
import com.example.ui.components.EditRecordDialog
import com.example.ui.components.FilterChipsRow
import com.example.ui.theme.CrimsonDanger
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.Navy800
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.util.AppLanguage
import com.example.ui.util.AppStrings
import com.example.ui.util.BanglaFormatter
import com.example.ui.viewmodel.FinancialViewModel
import kotlinx.coroutines.launch

enum class SortOrder {
    DATE_DESC,
    DATE_ASC,
    AMOUNT_DESC,
    AMOUNT_ASC
}

@Composable
fun MarketExpensesScreen(
    viewModel: FinancialViewModel,
    snackbarHostState: SnackbarHostState
) {
    val language by viewModel.language.collectAsStateWithLifecycle()
    val allExpenses by viewModel.expenses.collectAsStateWithLifecycle()
    val isBn = language == AppLanguage.BANGLA
    val scope = rememberCoroutineScope()

    // Form states
    var inputAmount by remember { mutableStateOf("") }
    var inputDate by remember { mutableStateOf(BanglaFormatter.getTodayDhakaIso()) }
    var inputNote by remember { mutableStateOf("") }
    var inputError by remember { mutableStateOf<String?>(null) }

    // Filter, search & sort states
    var selectedFilter by remember { mutableStateOf(DateFilterType.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var sortOrder by remember { mutableStateOf(SortOrder.DATE_DESC) }

    // Edit and Delete dialog states
    var editingExpense by remember { mutableStateOf<MarketExpense?>(null) }
    var deletingExpense by remember { mutableStateOf<MarketExpense?>(null) }

    // Filter calculations
    val filteredExpenses = remember(allExpenses, selectedFilter, searchQuery, sortOrder) {
        val today = BanglaFormatter.getTodayDhakaIso()
        val startWeek = BanglaFormatter.getStartOfCurrentWeekIso()
        val startMonth = BanglaFormatter.getStartOfCurrentMonthIso()
        val endMonth = BanglaFormatter.getEndOfCurrentMonthIso()

        var list = when (selectedFilter) {
            DateFilterType.ALL -> allExpenses
            DateFilterType.TODAY -> allExpenses.filter { it.date == today }
            DateFilterType.THIS_WEEK -> allExpenses.filter { it.date >= startWeek }
            DateFilterType.THIS_MONTH -> allExpenses.filter { it.date in startMonth..endMonth }
            DateFilterType.CUSTOM -> allExpenses
        }

        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            list = list.filter {
                it.note.lowercase().contains(q) || it.amount.toString().contains(q)
            }
        }

        when (sortOrder) {
            SortOrder.DATE_DESC -> list.sortedWith(compareByDescending<MarketExpense> { it.date }.thenByDescending { it.id })
            SortOrder.DATE_ASC -> list.sortedWith(compareBy<MarketExpense> { it.date }.thenBy { it.id })
            SortOrder.AMOUNT_DESC -> list.sortedByDescending { it.amount }
            SortOrder.AMOUNT_ASC -> list.sortedBy { it.amount }
        }
    }

    val grandTotal = remember(allExpenses) { allExpenses.sumOf { it.amount } }
    val filteredTotal = remember(filteredExpenses) { filteredExpenses.sumOf { it.amount } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 1. Entry Form Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isBn) "দৈনিক বাজার খরচ এন্ট্রি" else "Daily Market Expense Entry",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Navy800
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Date Picker (Defaults to today in Asia/Dhaka)
                    DatePickerField(
                        label = AppStrings.date(language),
                        selectedIsoDate = inputDate,
                        onDateSelected = { inputDate = it },
                        language = language
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Amount in BDT
                    OutlinedTextField(
                        value = inputAmount,
                        onValueChange = {
                            inputAmount = it
                            inputError = null
                        },
                        label = { Text(AppStrings.amount(language)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = inputError != null,
                        supportingText = inputError?.let { { Text(it, color = CrimsonDanger) } },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("expense_amount_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Note
                    OutlinedTextField(
                        value = inputNote,
                        onValueChange = { inputNote = it },
                        label = { Text(AppStrings.note(language)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("expense_note_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Category Chips (খাবার, বিল, যাতায়াত, অন্যান্য)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("খাবার", "বিল", "যাতায়াত", "অন্যান্য").forEach { cat ->
                            val isSelected = inputNote.contains(cat)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) EmeraldContainer else Slate100,
                                border = BorderStroke(1.dp, if (isSelected) EmeraldPrimary else Slate300),
                                onClick = {
                                    inputNote = if (inputNote.isBlank()) {
                                        cat
                                    } else if (inputNote.contains(cat)) {
                                        inputNote.replace(", $cat", "").replace(cat, "").trim()
                                    } else {
                                        "$inputNote, $cat"
                                    }
                                }
                            ) {
                                Text(
                                    text = cat,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isSelected) EmeraldDark else Slate700
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Save Button
                    Button(
                        onClick = {
                            val parsedAmount = inputAmount.toDoubleOrNull()
                            if (parsedAmount == null || parsedAmount <= 0) {
                                inputError = if (isBn) "সঠিক টাকার পরিমাণ লিখুন (০ এর বেশি)" else "Enter valid amount (> 0)"
                                return@Button
                            }
                            viewModel.addExpense(
                                amount = parsedAmount,
                                date = inputDate,
                                note = inputNote,
                                onSuccess = {
                                    inputAmount = ""
                                    inputNote = ""
                                    inputError = null
                                    scope.launch {
                                        snackbarHostState.showSnackbar(AppStrings.expenseSavedSuccess(language))
                                    }
                                },
                                onError = { err ->
                                    inputError = err
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("save_expense_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = AppStrings.save(language),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Filters & Search Bar
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

                // Sort toggle button
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
                    Icon(
                        imageVector = Icons.Default.Sort,
                        contentDescription = "Sort",
                        tint = Navy800
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Excel-Style Table Header
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
                        text = AppStrings.marketExpense(language),
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

        // 4. Excel-Style Table Rows
        if (filteredExpenses.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .border(1.dp, Slate200)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = AppStrings.noRecordsFound(language),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate600
                    )
                }
            }
        } else {
            itemsIndexed(filteredExpenses, key = { _, item -> item.id }) { index, item ->
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
                        // Serial
                        val sl = if (isBn) BanglaFormatter.toBanglaDigits("${index + 1}") else "${index + 1}"
                        Text(
                            text = sl,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = Slate600,
                            modifier = Modifier.width(42.dp)
                        )

                        // Date
                        val dt = BanglaFormatter.formatToDisplayDate(item.date, isBn)
                        Text(
                            text = dt,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.width(82.dp)
                        )

                        // Amount & Note
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

                        // Actions: Edit & Delete
                        Row(modifier = Modifier.width(68.dp)) {
                            IconButton(
                                onClick = { editingExpense = item },
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
                                onClick = { deletingExpense = item },
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

        // 5. Excel Table Bottom Totals
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 12.dp, bottomEnd = 12.dp),
                colors = CardDefaults.cardColors(containerColor = Slate100),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate300)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    if (selectedFilter != DateFilterType.ALL || searchQuery.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${AppStrings.filteredTotal(language)}:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Slate600
                            )
                            Text(
                                text = BanglaFormatter.formatCurrency(filteredTotal, isBn),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Navy800
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isBn) "সর্বমোট বাজার খরচ:" else "Grand Total Market Expenses:",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Navy800
                        )
                        Text(
                            text = BanglaFormatter.formatCurrency(grandTotal, isBn),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = EmeraldDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Edit Dialog
    editingExpense?.let { exp ->
        EditRecordDialog(
            visible = true,
            title = if (isBn) "বাজার খরচ সম্পাদনা" else "Edit Market Expense",
            initialAmount = exp.amount,
            initialDate = exp.date,
            initialNote = exp.note,
            language = language,
            onDismiss = { editingExpense = null },
            onSave = { amount, date, note ->
                viewModel.updateExpense(
                    exp.copy(amount = amount, date = date, note = note)
                )
                editingExpense = null
            }
        )
    }

    // Delete Confirmation Dialog
    deletingExpense?.let { exp ->
        DeleteConfirmationDialog(
            visible = true,
            language = language,
            onConfirm = {
                viewModel.deleteExpense(exp)
                deletingExpense = null
            },
            onDismiss = { deletingExpense = null }
        )
    }
}
