package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.EditRecordDialog
import com.example.ui.components.ExpenseTrendCard
import com.example.ui.components.StatCard
import com.example.ui.theme.AmberLight
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CrimsonDanger
import com.example.ui.theme.CrimsonLight
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.Navy700
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate800
import com.example.ui.util.AppLanguage
import com.example.ui.util.AppStrings
import com.example.ui.util.BanglaFormatter
import com.example.ui.viewmodel.FinancialViewModel
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    viewModel: FinancialViewModel,
    snackbarHostState: SnackbarHostState,
    onNavigateToExpenses: () -> Unit,
    onNavigateToOutstanding: () -> Unit
) {
    val language by viewModel.language.collectAsStateWithLifecycle()
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isBn = language == AppLanguage.BANGLA
    val scope = rememberCoroutineScope()

    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var showAddOutstandingDialog by remember { mutableStateOf(false) }
    var showAddPaymentDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Top Header Banner
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isBn) "হিসাব খাতা" else "Hisab Khata",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Navy900
                    )
                )
                Text(
                    text = BanglaFormatter.getFullBanglaDate(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate600
                )
            }

            // Cloud Status Badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (currentUser != null) EmeraldContainer else Slate100
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (currentUser != null) Icons.Default.CloudDone else Icons.Default.CloudSync,
                        contentDescription = null,
                        tint = if (currentUser != null) EmeraldDark else Slate600,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (currentUser != null) (if (isBn) "সিঙ্ক হয়েছে" else "Synced") else (if (isBn) "অফলাইন" else "Offline"),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (currentUser != null) EmeraldDark else Slate600
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 1. PRIMARY HERO CARD: Total Outstanding Balance (মোট বাকি ব্যালেন্স)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("outstanding_balance_card"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E36)),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Top Row: Label and Due Count Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isBn) "মোট বাকি ব্যালেন্স" else "Total Due Balance",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF94A3B8)
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0x33DC2626)
                    ) {
                        Text(
                            text = if (isBn)
                                "${BanglaFormatter.toBanglaDigits(summary.totalOutstandingCount.toString())}টি বকেয়া এন্ট্রি"
                            else
                                "${summary.totalOutstandingCount} Due Entries",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFFCA5A5)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Big Currency Amount
                Text(
                    text = BanglaFormatter.formatCurrency(summary.totalOutstandingBalance, isBn),
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 38.sp
                    ),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Progress Bar showing paid ratio
                val totalDueAndPaid = summary.totalOutstandingAdded + summary.totalPaymentsMade
                val paidRatio = if (totalDueAndPaid > 0) (summary.totalPaymentsMade / totalDueAndPaid).toFloat().coerceIn(0f, 1f) else 0f
                val paidPercent = (paidRatio * 100).toInt()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(paidRatio)
                            .height(5.dp)
                            .background(EmeraldLight)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Row: মোট বাকি ৳X   পরিশোধ ৳Y (Z%)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isBn)
                            "মোট বাকি ${BanglaFormatter.formatCurrency(summary.totalOutstandingAdded, true)}"
                        else
                            "Total Due: ${BanglaFormatter.formatCurrency(summary.totalOutstandingAdded, false)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )
                    Text(
                        text = if (isBn)
                            "পরিশোধ ${BanglaFormatter.formatCurrency(summary.totalPaymentsMade, true)} (${BanglaFormatter.toBanglaDigits(paidPercent.toString())}%)"
                        else
                            "Paid: ${BanglaFormatter.formatCurrency(summary.totalPaymentsMade, false)} ($paidPercent%)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. TWO-COLUMN STAT ROW (এই মাসের বাজার খরচ | আজকের এন্ট্রি)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Left Card: এই মাসের বাজার খরচ
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigateToExpenses() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (isBn) "এই মাসের বাজার খরচ" else "This Month's Exp.",
                        style = MaterialTheme.typography.labelMedium,
                        color = Slate600
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = BanglaFormatter.formatCurrency(summary.thisMonthMarketExpenses, isBn),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = EmeraldDark
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isBn)
                            "আজ ${BanglaFormatter.formatCurrency(summary.todaysMarketExpenses, true)}"
                        else
                            "Today ${BanglaFormatter.formatCurrency(summary.todaysMarketExpenses, false)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate600
                    )
                }
            }

            // Right Card: আজকের এন্ট্রি
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (isBn) "আজকের এন্ট্রি" else "Today's Entries",
                        style = MaterialTheme.typography.labelMedium,
                        color = Slate600
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isBn)
                            "${BanglaFormatter.toBanglaDigits(summary.todaysEntriesCount.toString())}টি"
                        else
                            "${summary.todaysEntriesCount}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Navy900
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isBn)
                            "নতুন বাকি ${BanglaFormatter.formatCurrency(summary.todaysOutstanding, true)}"
                        else
                            "New Due ${BanglaFormatter.formatCurrency(summary.todaysOutstanding, false)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate600
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Quick Action Buttons
        Text(
            text = if (isBn) "দ্রুত হিসাব এন্ট্রি" else "Quick Entry Actions",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Navy800
        )
        Spacer(modifier = Modifier.height(8.dp))

        // 3. Quick Action Cards (High-Quality Executive Design - No Text Clipping)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Action 1: বাজার খরচ (Market Expense)
            Card(
                onClick = { showAddExpenseDialog = true },
                modifier = Modifier
                    .weight(1f)
                    .testTag("quick_add_expense_btn"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldPrimary),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp, pressedElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isBn) "বাজার খরচ" else "Market Exp.",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        ),
                        color = Color.White,
                        maxLines = 1,
                        softWrap = false,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Action 2: বাকি যোগ (Add Outstanding Due)
            Card(
                onClick = { showAddOutstandingDialog = true },
                modifier = Modifier
                    .weight(1f)
                    .testTag("quick_add_outstanding_btn"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CrimsonDanger),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp, pressedElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isBn) "বাকি যোগ" else "Add Due",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        ),
                        color = Color.White,
                        maxLines = 1,
                        softWrap = false,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Action 3: পরিশোধ (Payment / Repayment) - NEVER CUTS OFF
            Card(
                onClick = { showAddPaymentDialog = true },
                modifier = Modifier
                    .weight(1f)
                    .testTag("quick_add_payment_btn"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E36)),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp, pressedElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Payment,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isBn) "পরিশোধ" else "Payment",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        ),
                        color = Color.White,
                        maxLines = 1,
                        softWrap = false,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Expense Trend Chart (গত ৭ দিনের বাজার খরচ)
        ExpenseTrendCard(expenses = expenses, language = language)

        Spacer(modifier = Modifier.height(16.dp))

        // 5. Recent Expenses Card (সাম্প্রতিক খরচ - সব দেখুন)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isBn) "সাম্প্রতিক খরচ" else "Recent Expenses",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Navy900
                    )
                    Text(
                        text = if (isBn) "সব দেখুন" else "View All",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = EmeraldDark
                        ),
                        modifier = Modifier.clickable { onNavigateToExpenses() }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                val recentList = expenses.take(3)
                if (recentList.isEmpty()) {
                    Text(
                        text = if (isBn) "এখনো কোনো খরচ রেকর্ড করা হয়নি" else "No recent expense records",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate600,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    recentList.forEachIndexed { index, exp ->
                        if (index > 0) {
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = Slate100, thickness = 1.dp)
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        val isBill = exp.note.contains("বিল") || exp.note.contains("বিদ্যুৎ") || exp.note.contains("গ্যাস") || exp.note.contains("পানি")
                        val isTravel = exp.note.contains("যাতায়াত") || exp.note.contains("ভাড়া") || exp.note.contains("গাড়ি")
                        val iconColor = if (isBill) Color(0xFF3B82F6) else if (isTravel) Color(0xFF10B981) else Color(0xFFF59E0B)
                        val iconBg = if (isBill) Color(0xFFEFF6FF) else if (isTravel) Color(0xFFECFDF5) else Color(0xFFFEF3C7)
                        val categoryName = if (isBill) "বিল" else if (isTravel) "যাতায়াত" else "খাবার"

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(iconBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isBill) Icons.Default.FlashOn else if (isTravel) Icons.Default.DirectionsCar else Icons.Default.Restaurant,
                                        contentDescription = null,
                                        tint = iconColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = if (exp.note.isNotBlank()) exp.note else (if (isBn) "বাজার খরচ" else "Expense"),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Navy900
                                    )
                                    Text(
                                        text = "${BanglaFormatter.formatToDisplayDate(exp.date, isBn)} · $categoryName",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Slate600
                                    )
                                }
                            }
                            Text(
                                text = BanglaFormatter.formatCurrency(exp.amount, isBn),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldDark
                                )
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Modal Dialog: Add Market Expense
    if (showAddExpenseDialog) {
        EditRecordDialog(
            visible = true,
            title = AppStrings.addMarketExpenseBtn(language),
            initialAmount = 0.0,
            initialDate = BanglaFormatter.getTodayDhakaIso(),
            initialNote = "",
            language = language,
            onDismiss = { showAddExpenseDialog = false },
            onSave = { amount, date, note ->
                viewModel.addExpense(
                    amount = amount,
                    date = date,
                    note = note,
                    onSuccess = {
                        scope.launch {
                            snackbarHostState.showSnackbar(AppStrings.expenseSavedSuccess(language))
                        }
                    },
                    onError = { err ->
                        scope.launch {
                            snackbarHostState.showSnackbar(err)
                        }
                    }
                )
            }
        )
    }

    // Modal Dialog: Add Outstanding Entry
    if (showAddOutstandingDialog) {
        EditRecordDialog(
            visible = true,
            title = AppStrings.addOutstandingBtn(language),
            initialAmount = 0.0,
            initialDate = BanglaFormatter.getTodayDhakaIso(),
            initialNote = "",
            language = language,
            onDismiss = { showAddOutstandingDialog = false },
            onSave = { amount, date, note ->
                viewModel.addOutstanding(
                    amount = amount,
                    date = date,
                    note = note,
                    onSuccess = {
                        scope.launch {
                            snackbarHostState.showSnackbar(AppStrings.outstandingSavedSuccess(language))
                        }
                    },
                    onError = { err ->
                        scope.launch {
                            snackbarHostState.showSnackbar(err)
                        }
                    }
                )
            }
        )
    }

    // Modal Dialog: Add Payment Record
    if (showAddPaymentDialog) {
        EditRecordDialog(
            visible = true,
            title = AppStrings.addPaymentBtn(language),
            initialAmount = 0.0,
            initialDate = BanglaFormatter.getTodayDhakaIso(),
            initialNote = "",
            language = language,
            onDismiss = { showAddPaymentDialog = false },
            onSave = { amount, date, note ->
                viewModel.addPayment(
                    amount = amount,
                    date = date,
                    note = note,
                    onSuccess = {
                        scope.launch {
                            snackbarHostState.showSnackbar(AppStrings.paymentSavedSuccess(language))
                        }
                    },
                    onError = { err ->
                        scope.launch {
                            snackbarHostState.showSnackbar(err)
                        }
                    }
                )
            }
        )
    }
}
