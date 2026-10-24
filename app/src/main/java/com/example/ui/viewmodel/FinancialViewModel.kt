package com.example.ui.viewmodel

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.auth.AuthManager
import com.example.data.cloud.FirestoreSyncManager
import com.example.data.model.MarketExpense
import com.example.data.model.OutstandingEntry
import com.example.data.model.PaymentRecord
import com.example.data.repository.FinancialRepository
import com.example.ui.util.AppLanguage
import com.example.ui.util.BanglaFormatter
import com.google.firebase.auth.FirebaseUser
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
    val todaysEntriesCount: Int = 0,
    val totalOutstandingCount: Int = 0
)

class FinancialViewModel(
    private val repository: FinancialRepository,
    private val authManager: AuthManager? = null,
    private val syncManager: FirestoreSyncManager? = null
) : ViewModel() {

    private val _language = MutableStateFlow(AppLanguage.BANGLA)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _allowNegativeBalance = MutableStateFlow(false)
    val allowNegativeBalance: StateFlow<Boolean> = _allowNegativeBalance.asStateFlow()

    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _reminderEnabled = MutableStateFlow(true)
    val reminderEnabled: StateFlow<Boolean> = _reminderEnabled.asStateFlow()

    private val _reminderTime = MutableStateFlow("09:00 PM")
    val reminderTime: StateFlow<String> = _reminderTime.asStateFlow()

    val currentUser: StateFlow<FirebaseUser?> = authManager?.currentUser
        ?: MutableStateFlow(null).asStateFlow()

    init {
        // Attempt silent auto sign-in and cloud sync on startup
        authManager?.attemptAutoSignIn(viewModelScope) { success ->
            if (success) {
                authManager.currentUser.value?.uid?.let { uid ->
                    viewModelScope.launch {
                        syncManager?.syncDownFromCloud(uid)
                    }
                }
            }
        }
    }

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
            todaysEntriesCount = countToday,
            totalOutstandingCount = outList.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinancialSummary())

    fun setLanguage(lang: AppLanguage) {
        _language.value = lang
    }

    fun toggleLanguage() {
        _language.value = if (_language.value == AppLanguage.BANGLA) AppLanguage.ENGLISH else AppLanguage.BANGLA
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun setDarkMode(dark: Boolean) {
        _isDarkMode.value = dark
    }

    fun setReminderEnabled(enabled: Boolean) {
        _reminderEnabled.value = enabled
    }

    fun setReminderTime(time: String) {
        _reminderTime.value = time
    }

    fun setAllowNegativeBalance(allow: Boolean) {
        _allowNegativeBalance.value = allow
    }

    // Auth & Cloud Sync
    fun signInWithGoogle(
        activity: Activity,
        onSuccess: (FirebaseUser) -> Unit,
        onError: (String) -> Unit,
        onCancelled: () -> Unit = {}
    ) {
        authManager?.signInWithGoogle(
            activity = activity,
            scope = viewModelScope,
            onSuccess = { user ->
                viewModelScope.launch {
                    syncManager?.syncDownFromCloud(user.uid)
                }
                onSuccess(user)
            },
            onError = onError,
            onCancelled = onCancelled
        ) ?: onError("Authentication service not initialized")
    }

    fun signOut(onComplete: () -> Unit = {}) {
        authManager?.signOut(viewModelScope, onComplete) ?: onComplete()
    }

    fun syncFromCloud(onResult: (Result<Int>) -> Unit) {
        val uid = authManager?.currentUser?.value?.uid
        if (uid != null && syncManager != null) {
            viewModelScope.launch {
                val res = syncManager.syncDownFromCloud(uid)
                onResult(res)
            }
        } else {
            onResult(Result.failure(IllegalStateException("User is not signed in")))
        }
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
                val id = repository.addExpense(date, amount, note)
                val uid = authManager?.currentUser?.value?.uid
                if (uid != null && syncManager != null) {
                    val exp = MarketExpense(id = id, date = date, amount = amount, note = note)
                    syncManager.uploadExpense(uid, exp)
                }
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to save expense")
            }
        }
    }

    fun updateExpense(expense: MarketExpense, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateExpense(expense)
            val uid = authManager?.currentUser?.value?.uid
            if (uid != null && syncManager != null) {
                syncManager.uploadExpense(uid, expense)
            }
            onSuccess()
        }
    }

    fun deleteExpense(expense: MarketExpense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
            val uid = authManager?.currentUser?.value?.uid
            if (uid != null && syncManager != null) {
                syncManager.deleteExpense(uid, expense.id)
            }
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
                val id = repository.addOutstanding(date, amount, note)
                val uid = authManager?.currentUser?.value?.uid
                if (uid != null && syncManager != null) {
                    val entry = OutstandingEntry(id = id, date = date, amount = amount, note = note)
                    syncManager.uploadOutstanding(uid, entry)
                }
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to save outstanding entry")
            }
        }
    }

    fun updateOutstanding(entry: OutstandingEntry, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateOutstanding(entry)
            val uid = authManager?.currentUser?.value?.uid
            if (uid != null && syncManager != null) {
                syncManager.uploadOutstanding(uid, entry)
            }
            onSuccess()
        }
    }

    fun deleteOutstanding(entry: OutstandingEntry) {
        viewModelScope.launch {
            repository.deleteOutstanding(entry)
            val uid = authManager?.currentUser?.value?.uid
            if (uid != null && syncManager != null) {
                syncManager.deleteOutstanding(uid, entry.id)
            }
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
                val id = repository.addPayment(date, amount, note)
                val uid = authManager?.currentUser?.value?.uid
                if (uid != null && syncManager != null) {
                    val pay = PaymentRecord(id = id, date = date, amount = amount, note = note)
                    syncManager.uploadPayment(uid, pay)
                }
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
                val uid = authManager?.currentUser?.value?.uid
                if (uid != null && syncManager != null) {
                    syncManager.uploadPayment(uid, payment)
                }
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to update payment")
            }
        }
    }

    fun deletePayment(payment: PaymentRecord) {
        viewModelScope.launch {
            repository.deletePayment(payment)
            val uid = authManager?.currentUser?.value?.uid
            if (uid != null && syncManager != null) {
                syncManager.deletePayment(uid, payment.id)
            }
        }
    }

    // Backup & Restore (JSON)
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
    private val repository: FinancialRepository,
    private val authManager: AuthManager? = null,
    private val syncManager: FirestoreSyncManager? = null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FinancialViewModel::class.java)) {
            return FinancialViewModel(repository, authManager, syncManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
