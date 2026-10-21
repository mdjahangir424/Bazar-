package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.core.content.FileProvider
import com.example.data.model.MarketExpense
import com.example.data.model.OutstandingEntry
import com.example.data.model.PaymentRecord
import com.example.data.repository.FinancialRepository
import com.example.ui.util.AppLanguage
import com.example.ui.util.BanglaFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

data class FinancialSummary(
    val totalOutstandingBalance: Double = 0.0,
    val totalOutstandingAdded: Double = 0.0,
    val totalPaymentsMade: Double = 0.0,
    val totalMarketExpenses: Double = 0.0,
    val todaysMarketExpenses: Double = 0.0,
    val thisMonthMarketExpenses: Double = 0.0,
    val todaysOutstanding: Double = 0.0,
    val todaysPayment: Double = 0.0,
    val todaysEntriesCount: Int = 0
)

class FinancialViewModel(
    private val repository: FinancialRepository
) : ViewModel() {

    private val _language = MutableStateFlow(AppLanguage.BANGLA)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _allowNegativeBalance = MutableStateFlow(false)
    val allowNegativeBalance: StateFlow<Boolean> = _allowNegativeBalance.asStateFlow()

    val expenses: StateFlow<List<MarketExpense>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val outstanding: StateFlow<List<OutstandingEntry>> = repository.allOutstanding
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val payments: StateFlow<List<PaymentRecord>> = repository.allPayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val summary: StateFlow<FinancialSummary> = combine(
        expenses,
        outstanding,
        payments
    ) { expList, outList, payList ->
        val today = BanglaFormatter.getTodayDhakaIso()
        val startMonth = BanglaFormatter.getStartOfCurrentMonthIso()
        val endMonth = BanglaFormatter.getEndOfCurrentMonthIso()

        val totalOutAdded = outList.sumOf { it.amount }
        val totalPayMade = payList.sumOf { it.amount }
        val netBalance = totalOutAdded - totalPayMade

        val totalExp = expList.sumOf { it.amount }
        val todayExp = expList.filter { it.date == today }.sumOf { it.amount }
        val monthExp = expList.filter { it.date in startMonth..endMonth }.sumOf { it.amount }

        val todayOut = outList.filter { it.date == today }.sumOf { it.amount }
        val todayPay = payList.filter { it.date == today }.sumOf { it.amount }

        val countToday = expList.count { it.date == today } +
                outList.count { it.date == today } +
                payList.count { it.date == today }

        FinancialSummary(
            totalOutstandingBalance = netBalance,
            totalOutstandingAdded = totalOutAdded,
            totalPaymentsMade = totalPayMade,
            totalMarketExpenses = totalExp,
            todaysMarketExpenses = todayExp,
            thisMonthMarketExpenses = monthExp,
            todaysOutstanding = todayOut,
            todaysPayment = todayPay,
            todaysEntriesCount = countToday
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinancialSummary())

    fun setLanguage(lang: AppLanguage) {
        _language.value = lang
    }

    fun toggleLanguage() {
        _language.value = if (_language.value == AppLanguage.BANGLA) AppLanguage.ENGLISH else AppLanguage.BANGLA
    }

    fun setAllowNegativeBalance(allow: Boolean) {
        _allowNegativeBalance.value = allow
    }

    // Market Expense Operations
    fun addExpense(
        amount: Double,
        date: String,
        note: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (amount <= 0) {
            onError(if (_language.value == AppLanguage.BANGLA) "টাকার পরিমাণ ০ এর বেশি হতে হবে" else "Amount must be greater than 0")
            return
        }
        viewModelScope.launch {
            try {
                repository.addExpense(date, amount, note)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to save expense")
            }
        }
    }

    fun updateExpense(expense: MarketExpense, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateExpense(expense)
            onSuccess()
        }
    }

    fun deleteExpense(expense: MarketExpense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    // Outstanding Operations
    fun addOutstanding(
        amount: Double,
        date: String,
        note: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (amount <= 0) {
            onError(if (_language.value == AppLanguage.BANGLA) "টাকার পরিমাণ ০ এর বেশি হতে হবে" else "Amount must be greater than 0")
            return
        }
        viewModelScope.launch {
            try {
                repository.addOutstanding(date, amount, note)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to save outstanding entry")
            }
        }
    }

    fun updateOutstanding(entry: OutstandingEntry, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateOutstanding(entry)
            onSuccess()
        }
    }

    fun deleteOutstanding(entry: OutstandingEntry) {
        viewModelScope.launch {
            repository.deleteOutstanding(entry)
        }
    }

    // Payment Operations
    fun addPayment(
        amount: Double,
        date: String,
        note: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (amount <= 0) {
            onError(if (_language.value == AppLanguage.BANGLA) "পরিশোধের পরিমাণ ০ এর বেশি হতে হবে" else "Payment amount must be greater than 0")
            return
        }
        val currentBalance = summary.value.totalOutstandingBalance
        if (!_allowNegativeBalance.value && amount > currentBalance) {
            onError(
                if (_language.value == AppLanguage.BANGLA)
                    "পরিশোধের পরিমাণ (৳$amount) বর্তমান বাকি ব্যালেন্সের (৳$currentBalance) চেয়ে বেশি হতে পারে না!"
                else
                    "Payment amount (৳$amount) cannot exceed current outstanding balance (৳$currentBalance)!"
            )
            return
        }
        viewModelScope.launch {
            try {
                repository.addPayment(date, amount, note)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to record payment")
            }
        }
    }

    fun updatePayment(
        payment: PaymentRecord,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (payment.amount <= 0) {
            onError(if (_language.value == AppLanguage.BANGLA) "পরিশোধের পরিমাণ ০ এর বেশি হতে হবে" else "Payment amount must be greater than 0")
            return
        }
        viewModelScope.launch {
            try {
                repository.updatePayment(payment)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to update payment")
            }
        }
    }

    fun deletePayment(payment: PaymentRecord) {
        viewModelScope.launch {
            repository.deletePayment(payment)
        }
    }

    // Backup & Restore
    fun exportBackup(context: Context, onFileReady: (File) -> Unit) {
        viewModelScope.launch {
            val jsonString = repository.exportBackupJson(
                expenses.value,
                outstanding.value,
                payments.value
            )
            val fileName = "HisabKhata_Backup_${BanglaFormatter.getTodayDhakaIso()}_${System.currentTimeMillis()}.json"
            val file = File(context.cacheDir, fileName)
            FileOutputStream(file).use { fos ->
                OutputStreamWriter(fos, StandardCharsets.UTF_8).use { it.write(jsonString) }
            }
            onFileReady(file)
        }
    }

    fun importBackup(jsonString: String, replaceExisting: Boolean, onResult: (Result<Int>) -> Unit) {
        viewModelScope.launch {
            val result = repository.importBackupJson(jsonString, replaceExisting)
            onResult(result)
        }
    }

    fun clearAllData(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.clearAllData()
            onComplete()
        }
    }
}

class FinancialViewModelFactory(
    private val repository: FinancialRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FinancialViewModel::class.java)) {
            return FinancialViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
