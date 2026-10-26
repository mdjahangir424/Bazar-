package com.example.ui.viewmodel

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.auth.AuthManager
import com.example.data.cloud.FirestoreSyncManager
import com.example.data.cloud.SyncWorker
import com.example.data.local.SyncQueueDao
import com.example.data.model.BackupSnapshotInfo
import com.example.data.model.MarketExpense
import com.example.data.model.OutstandingEntry
import com.example.data.model.PaymentRecord
import com.example.data.network.NetworkMonitor
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

data class UserProfile(
    val name: String = "মো: জাহাঙ্গীর আলম (Md Jahangir Alam)",
    val phone: String = "+8801778600028",
    val email: String = "mdjahangiralam2528@gmail.com",
    val gender: String = "ছেলে" // "ছেলে" or "মেয়ে"
)

class FinancialViewModel(
    private val repository: FinancialRepository,
    private val authManager: AuthManager? = null,
    private val syncManager: FirestoreSyncManager? = null,
    private val syncQueueDao: SyncQueueDao? = null,
    private val networkMonitor: NetworkMonitor? = null,
    private val appContext: Context? = null
) : ViewModel() {

    private val prefs by lazy {
        appContext?.getSharedPreferences("hisab_khata_user_profile", Context.MODE_PRIVATE)
    }

    private val _userProfile = MutableStateFlow(loadInitialProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private fun loadInitialProfile(): UserProfile {
        val p = prefs ?: return UserProfile()
        val name = p.getString("profile_name", null)
        val phone = p.getString("profile_phone", null)
        val email = p.getString("profile_email", null)
        val gender = p.getString("profile_gender", null)
        return if (!name.isNullOrBlank() || !phone.isNullOrBlank() || !email.isNullOrBlank() || !gender.isNullOrBlank()) {
            UserProfile(
                name = name ?: "মো: জাহাঙ্গীর আলম",
                phone = phone ?: "+8801778600028",
                email = email ?: "mdjahangiralam2528@gmail.com",
                gender = gender ?: "ছেলে"
            )
        } else {
            UserProfile()
        }
    }

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

    val isOnline: StateFlow<Boolean> = networkMonitor?.isOnline
        ?: MutableStateFlow(true).asStateFlow()

    val isLiveSyncActive: StateFlow<Boolean> = syncManager?.isLiveSyncActive
        ?: MutableStateFlow(false).asStateFlow()

    val syncState: StateFlow<String> = syncManager?.syncState
        ?: MutableStateFlow("IDLE").asStateFlow()

    val lastSyncError: StateFlow<String?> = syncManager?.lastSyncError
        ?: MutableStateFlow(null).asStateFlow()

    val pendingSyncCount: StateFlow<Int> = syncQueueDao?.getPendingCountFlow()
        ?.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
        ?: MutableStateFlow(0).asStateFlow()

    private val _availableBackups = MutableStateFlow<List<BackupSnapshotInfo>>(emptyList())
    val availableBackups: StateFlow<List<BackupSnapshotInfo>> = _availableBackups.asStateFlow()

    private val _lastCloudBackupTime = MutableStateFlow<String?>(null)
    val lastCloudBackupTime: StateFlow<String?> = _lastCloudBackupTime.asStateFlow()

    init {
        // Observe auth state changes to start/stop live sync, flush offline queue, and sync profile
        viewModelScope.launch {
            currentUser.collect { user ->
                if (user != null) {
                    syncManager?.startLiveSync(user.uid, viewModelScope)
                    // Trigger queue flush and initial sync
                    syncManager?.flushPendingSyncQueue(user.uid)
                    loadAvailableBackups()

                    // Sync user profile with cloud
                    try {
                        val cloudProfileRes = syncManager?.getUserProfileFromCloud(user.uid)
                        val cloudProfile = cloudProfileRes?.getOrNull()
                        if (cloudProfile != null && cloudProfile["name"]?.isNotBlank() == true) {
                            val loaded = UserProfile(
                                name = cloudProfile["name"] ?: _userProfile.value.name,
                                phone = cloudProfile["phone"] ?: _userProfile.value.phone,
                                email = cloudProfile["email"] ?: _userProfile.value.email,
                                gender = cloudProfile["gender"] ?: _userProfile.value.gender
                            )
                            _userProfile.value = loaded
                            prefs?.edit()?.apply {
                                putString("profile_name", loaded.name)
                                putString("profile_phone", loaded.phone)
                                putString("profile_email", loaded.email)
                                putString("profile_gender", loaded.gender)
                                apply()
                            }
                        } else {
                            // If cloud profile empty, upload local profile
                            val cur = _userProfile.value
                            syncManager?.saveUserProfileToCloud(
                                user.uid,
                                cur.name,
                                cur.phone,
                                cur.email,
                                cur.gender
                            )
                        }
                    } catch (e: Exception) {
                        // Profile cloud sync non-blocking
                    }
                } else {
                    syncManager?.stopLiveSync()
                    _availableBackups.value = emptyList()
                }
            }
        }

        // Silent auto sign-in via Google Credential Manager on app startup
        authManager?.attemptAutoSignIn(viewModelScope) { success ->
            if (success) {
                authManager.currentUser.value?.uid?.let { uid ->
                    viewModelScope.launch {
                        syncManager?.syncDownFromCloud(uid)
                        syncManager?.flushPendingSyncQueue(uid)
                        loadAvailableBackups()
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

    fun updateProfile(name: String, phone: String, email: String, gender: String) {
        val updated = UserProfile(
            name = name.trim().ifBlank { "মো: জাহাঙ্গীর আলম" },
            phone = phone.trim().ifBlank { "+8801778600028" },
            email = email.trim().ifBlank { "mdjahangiralam2528@gmail.com" },
            gender = gender
        )
        _userProfile.value = updated
        prefs?.edit()?.apply {
            putString("profile_name", updated.name)
            putString("profile_phone", updated.phone)
            putString("profile_email", updated.email)
            putString("profile_gender", updated.gender)
            apply()
        }
        val uid = authManager?.currentUser?.value?.uid
        if (uid != null && syncManager != null) {
            viewModelScope.launch {
                syncManager.saveUserProfileToCloud(
                    uid,
                    updated.name,
                    updated.phone,
                    updated.email,
                    updated.gender
                )
            }
        }
    }

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

    // Google Sign-In
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
                    syncManager?.startLiveSync(user.uid, viewModelScope)
                    syncManager?.flushPendingSyncQueue(user.uid)
                    loadAvailableBackups()
                }
                onSuccess(user)
            },
            onError = onError,
            onCancelled = onCancelled
        ) ?: onError("Authentication service not initialized")
    }

    // Email / Password Authentication
    fun signInWithEmail(
        email: String,
        pass: String,
        onSuccess: (FirebaseUser) -> Unit,
        onError: (String) -> Unit
    ) {
        authManager?.signInWithEmail(
            email = email,
            pass = pass,
            scope = viewModelScope,
            onSuccess = { user ->
                viewModelScope.launch {
                    syncManager?.startLiveSync(user.uid, viewModelScope)
                    syncManager?.flushPendingSyncQueue(user.uid)
                    loadAvailableBackups()
                }
                onSuccess(user)
            },
            onError = onError
        ) ?: onError("Authentication service not initialized")
    }

    fun signUpWithEmail(
        email: String,
        pass: String,
        onSuccess: (FirebaseUser) -> Unit,
        onError: (String) -> Unit
    ) {
        authManager?.signUpWithEmail(
            email = email,
            pass = pass,
            scope = viewModelScope,
            onSuccess = { user ->
                viewModelScope.launch {
                    syncManager?.startLiveSync(user.uid, viewModelScope)
                    syncManager?.flushPendingSyncQueue(user.uid)
                    loadAvailableBackups()
                }
                onSuccess(user)
            },
            onError = onError
        ) ?: onError("Authentication service not initialized")
    }

    fun sendPasswordReset(
        email: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        authManager?.sendPasswordResetEmail(email, viewModelScope, onSuccess, onError)
            ?: onError("Authentication service not initialized")
    }

    fun sendEmailVerification(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        authManager?.sendEmailVerification(viewModelScope, onSuccess, onError)
            ?: onError("Authentication service not initialized")
    }

    fun signOut(onComplete: () -> Unit = {}) {
        syncManager?.stopLiveSync()
        _availableBackups.value = emptyList()
        authManager?.signOut(viewModelScope, onComplete) ?: onComplete()
    }

    // Cloud Live Sync & Offline Queue
    fun flushPendingSync(onResult: (Result<Int>) -> Unit = {}) {
        val uid = authManager?.currentUser?.value?.uid
        if (uid != null && syncManager != null) {
            viewModelScope.launch {
                val res = syncManager.flushPendingSyncQueue(uid)
                onResult(res)
            }
        } else {
            appContext?.let { SyncWorker.enqueueSync(it) }
            onResult(Result.failure(IllegalStateException("দয়া করে আগে সাইন ইন করুন")))
        }
    }

    // Cloud Backup & Restore
    fun createCloudBackup(onResult: (Result<BackupSnapshotInfo>) -> Unit) {
        val uid = authManager?.currentUser?.value?.uid
        if (uid != null && syncManager != null) {
            viewModelScope.launch {
                val res = syncManager.createCloudBackup(
                    userId = uid,
                    expenses = expenses.value,
                    outstanding = outstanding.value,
                    payments = payments.value
                )
                if (res.isSuccess) {
                    val info = res.getOrNull()
                    _lastCloudBackupTime.value = info?.backupTime
                    loadAvailableBackups()
                }
                onResult(res)
            }
        } else {
            onResult(Result.failure(IllegalStateException("দয়া করে আগে সাইন ইন করুন (Please sign in first)")))
        }
    }

    fun loadAvailableBackups(onResult: (Result<List<BackupSnapshotInfo>>) -> Unit = {}) {
        val uid = authManager?.currentUser?.value?.uid
        if (uid != null && syncManager != null) {
            viewModelScope.launch {
                val res = syncManager.getAvailableCloudBackups(uid)
                if (res.isSuccess) {
                    val list = res.getOrNull().orEmpty()
                    _availableBackups.value = list
                    if (list.isNotEmpty()) {
                        _lastCloudBackupTime.value = list.first().backupTime
                    }
                }
                onResult(res)
            }
        }
    }

    fun restoreCloudBackup(
        backupId: String,
        isReplaceMode: Boolean,
        onResult: (Result<Int>) -> Unit
    ) {
        val uid = authManager?.currentUser?.value?.uid
        if (uid != null && syncManager != null) {
            viewModelScope.launch {
                val res = syncManager.restoreCloudBackup(uid, backupId, isReplaceMode)
                onResult(res)
            }
        } else {
            onResult(Result.failure(IllegalStateException("User is not signed in")))
        }
    }

    fun triggerImmediateSync(onResult: (Result<Int>) -> Unit = {}) {
        val uid = authManager?.currentUser?.value?.uid
        if (uid != null && syncManager != null) {
            viewModelScope.launch {
                try {
                    // 1. Ensure live sync listener is active
                    syncManager.startLiveSync(uid, viewModelScope)
                    // 2. Flush pending local offline queue to Cloud
                    val flushRes = syncManager.flushPendingSyncQueue(uid)
                    // 3. Pull latest records down from Cloud
                    val pullRes = syncManager.syncDownFromCloud(uid)
                    // 4. Reload cloud backup list
                    loadAvailableBackups()
                    val totalProcessed = (flushRes.getOrNull() ?: 0) + (pullRes.getOrNull() ?: 0)
                    onResult(Result.success(totalProcessed))
                } catch (e: Exception) {
                    onResult(Result.failure(e))
                }
            }
        } else {
            appContext?.let { SyncWorker.enqueueSync(it) }
            onResult(Result.failure(IllegalStateException(if (_language.value == AppLanguage.BANGLA) "দয়া করে প্রথমে গুগল বা ইমেইল দিয়ে সাইন ইন করুন" else "Please sign in first")))
        }
    }

    fun backupAllToCloud(onResult: (Result<String>) -> Unit) {
        createCloudBackup { res ->
            res.onSuccess { info ->
                onResult(Result.success(info.backupTime))
            }.onFailure { ex ->
                onResult(Result.failure(ex))
            }
        }
    }

    fun syncFromCloud(onResult: (Result<Int>) -> Unit) {
        val uid = authManager?.currentUser?.value?.uid
        if (uid != null && syncManager != null) {
            viewModelScope.launch {
                val res = syncManager.syncDownFromCloud(uid)
                val metaRes = syncManager.getLatestBackupMetadata(uid)
                if (metaRes.isSuccess) {
                    _lastCloudBackupTime.value = metaRes.getOrNull()?.get("backupTime") as? String
                }
                onResult(res)
            }
        } else {
            onResult(Result.failure(IllegalStateException("User is not signed in")))
        }
    }

    fun restoreFromCloud(onResult: (Result<Int>) -> Unit) {
        syncFromCloud(onResult)
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
                triggerBackgroundOrDirectSync()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to save expense")
            }
        }
    }

    fun updateExpense(expense: MarketExpense, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateExpense(expense)
            triggerBackgroundOrDirectSync()
            onSuccess()
        }
    }

    fun deleteExpense(expense: MarketExpense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
            triggerBackgroundOrDirectSync()
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
                triggerBackgroundOrDirectSync()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to save outstanding entry")
            }
        }
    }

    fun updateOutstanding(entry: OutstandingEntry, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateOutstanding(entry)
            triggerBackgroundOrDirectSync()
            onSuccess()
        }
    }

    fun deleteOutstanding(entry: OutstandingEntry) {
        viewModelScope.launch {
            repository.deleteOutstanding(entry)
            triggerBackgroundOrDirectSync()
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
        if (!_allowNegativeBalance.value && currentBalance > 0 && amount > (currentBalance + 0.001)) {
            onError(
                if (_language.value == AppLanguage.BANGLA)
                    "পরিশোধের পরিমাণ (${BanglaFormatter.formatCurrency(amount, true)}) বর্তমান বাকি ব্যালেন্সের (${BanglaFormatter.formatCurrency(currentBalance, true)}) চেয়ে বেশি হতে পারে না! (প্রয়োজনে সেটিংস থেকে 'নেগেটিভ ব্যালেন্স' চালু করতে পারেন)"
                else
                    "Payment amount (${BanglaFormatter.formatCurrency(amount, false)}) cannot exceed current balance (${BanglaFormatter.formatCurrency(currentBalance, false)})! (Enable negative balance in Settings if needed)"
            )
            return
        }
        viewModelScope.launch {
            try {
                repository.addPayment(date, amount, note)
                triggerBackgroundOrDirectSync()
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
                triggerBackgroundOrDirectSync()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to update payment")
            }
        }
    }

    fun deletePayment(payment: PaymentRecord) {
        viewModelScope.launch {
            repository.deletePayment(payment)
            triggerBackgroundOrDirectSync()
        }
    }

    private fun triggerBackgroundOrDirectSync() {
        val uid = authManager?.currentUser?.value?.uid
        if (uid != null && syncManager != null && isOnline.value) {
            viewModelScope.launch {
                syncManager.flushPendingSyncQueue(uid)
            }
        } else {
            appContext?.let { SyncWorker.enqueueSync(it) }
        }
    }

    // Local JSON Backup & Restore
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
    private val syncManager: FirestoreSyncManager? = null,
    private val syncQueueDao: SyncQueueDao? = null,
    private val networkMonitor: NetworkMonitor? = null,
    private val context: Context? = null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FinancialViewModel::class.java)) {
            return FinancialViewModel(
                repository = repository,
                authManager = authManager,
                syncManager = syncManager,
                syncQueueDao = syncQueueDao,
                networkMonitor = networkMonitor,
                appContext = context
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
