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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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
                    text = AppStrings.appTitle(language),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Navy800
                    )
                )
                Text(
                    text = if (isBn) "আজকের তারিখ: ${BanglaFormatter.formatToDisplayDate(BanglaFormatter.getTodayDhakaIso(), true)} (ঢাকা)"
                    else "Today: ${BanglaFormatter.formatToDisplayDate(BanglaFormatter.getTodayDhakaIso(), false)} (Asia/Dhaka)",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate600
                )
            }

            // Cloud Status Badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (currentUser != null) EmeraldContainer else Slate100
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
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
                        text = if (currentUser != null) (if (isBn) "ক্লাউড সিঙ্কড" else "Synced") else (if (isBn) "অফলাইন" else "Offline"),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (currentUser != null) EmeraldDark else Slate600
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 1. PRIMARY HERO CARD: Total Outstanding Balance (মোট বাকি)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("outstanding_balance_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Navy900),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Navy900, Navy800, Navy700)
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x33DC2626)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Receipt,
                                    contentDescription = null,
                                    tint = Color(0xFFFCA5A5),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = AppStrings.totalOutstandingBalance(language),
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFFCBD5E1)
                            )
                        }

                        // Badge: Paid off vs Outstanding
                        val isCleared = summary.totalOutstandingBalance <= 0.0
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isCleared) Color(0x3310B981) else Color(0x33DC2626)
                        ) {
                            Text(
                                text = if (isCleared) {
                                    if (isBn) "পরিশোধিত" else "Cleared"
                                } else {
                                    if (isBn) "বকেয়া আছে" else "Due"
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isCleared) EmeraldLight else Color(0xFFFCA5A5)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = BanglaFormatter.formatCurrency(summary.totalOutstandingBalance, isBn),
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 34.sp
                        ),
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isBn)
                            "মোট বাকি: ${BanglaFormatter.formatCurrency(summary.totalOutstandingAdded, true)} − পরিশোধ: ${BanglaFormatter.formatCurrency(summary.totalPaymentsMade, true)}"
                        else
                            "Added: ${BanglaFormatter.formatCurrency(summary.totalOutstandingAdded, false)} − Paid: ${BanglaFormatter.formatCurrency(summary.totalPaymentsMade, false)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Total Market Expenses Card (Prominent secondary figure)
        StatCard(
            title = AppStrings.totalMarketExpenses(language),
            value = BanglaFormatter.formatCurrency(summary.totalMarketExpenses, isBn),
            subtitle = if (isBn)
                "আজ: ${BanglaFormatter.formatCurrency(summary.todaysMarketExpenses, true)} | চলতি মাস: ${BanglaFormatter.formatCurrency(summary.thisMonthMarketExpenses, true)}"
            else
                "Today: ${BanglaFormatter.formatCurrency(summary.todaysMarketExpenses, false)} | This Month: ${BanglaFormatter.formatCurrency(summary.thisMonthMarketExpenses, false)}",
            icon = Icons.Default.ShoppingCart,
            iconBgColor = EmeraldContainer,
            iconTint = EmeraldDark,
            isPrimary = false
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Quick Action Buttons
        Text(
            text = if (isBn) "দ্রুত হিসাব এন্ট্রি" else "Quick Entry Actions",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Navy800
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { showAddExpenseDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("quick_add_expense_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isBn) "বাজার খরচ" else "Market Exp.",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Button(
                onClick = { showAddOutstandingDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonDanger),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("quick_add_outstanding_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isBn) "বাকি যোগ" else "Add Due",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Button(
                onClick = { showAddPaymentDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Navy800),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("quick_add_payment_btn")
            ) {
                Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isBn) "পরিশোধ" else "Payment",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Today's Summary Card
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
                        text = AppStrings.todaysSummary(language),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Navy800
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Slate100
                    ) {
                        Text(
                            text = if (isBn)
                                "${AppStrings.todaysEntriesCount(language)}: ${BanglaFormatter.toBanglaDigits(summary.todaysEntriesCount.toString())}"
                            else
                                "${AppStrings.todaysEntriesCount(language)}: ${summary.todaysEntriesCount}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate600
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Today's Expense
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = AppStrings.todaysExpense(language),
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate600
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = BanglaFormatter.formatCurrency(summary.todaysMarketExpenses, isBn),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = EmeraldDark
                        )
                    }

                    // Today's Outstanding
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = AppStrings.todaysOutstanding(language),
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate600
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = BanglaFormatter.formatCurrency(summary.todaysOutstanding, isBn),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = CrimsonDanger
                        )
                    }

                    // Today's Payment
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = AppStrings.todaysPayment(language),
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate600
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = BanglaFormatter.formatCurrency(summary.todaysPayment, isBn),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Navy800
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. Expense Trend Chart
        ExpenseTrendCard(expenses = expenses, language = language)

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
