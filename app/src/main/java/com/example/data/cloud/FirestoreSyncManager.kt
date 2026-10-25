package com.example.data.cloud

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.local.ExpenseDao
import com.example.data.local.OutstandingDao
import com.example.data.local.PaymentDao
import com.example.data.local.SyncQueueDao
import com.example.data.model.BackupSnapshotInfo
import com.example.data.model.MarketExpense
import com.example.data.model.OutstandingEntry
import com.example.data.model.PaymentRecord
import com.example.data.model.SyncQueueItem
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FirestoreSyncManager(
    private val context: Context,
    private val expenseDao: ExpenseDao,
    private val outstandingDao: OutstandingDao,
    private val paymentDao: PaymentDao,
    private val syncQueueDao: SyncQueueDao? = null
) {
    private val TAG = "FirestoreSyncManager"

    private val firestore: FirebaseFirestore? by lazy {
        FirebaseAppInitializer.ensureInitialized(context)
        try {
            val dbId = context.getString(R.string.firestore_database_id)
            if (dbId.isNotBlank()) {
                FirebaseFirestore.getInstance(dbId)
            } else {
                FirebaseFirestore.getInstance()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Could not initialize Firestore: ${e.message}", e)
            null
        }
    }

    private val _isLiveSyncActive = MutableStateFlow(false)
    val isLiveSyncActive: StateFlow<Boolean> = _isLiveSyncActive.asStateFlow()

    private val _syncState = MutableStateFlow("IDLE") // "IDLE", "SYNCING", "SUCCESS", "ERROR"
    val syncState: StateFlow<String> = _syncState.asStateFlow()

    private val _lastSyncError = MutableStateFlow<String?>(null)
    val lastSyncError: StateFlow<String?> = _lastSyncError.asStateFlow()

    private var expenseListener: ListenerRegistration? = null
    private var outstandingListener: ListenerRegistration? = null
    private var paymentListener: ListenerRegistration? = null
    private var currentActiveUserId: String? = null

    /**
     * Start Real-Time Live Sync listeners on Cloud Firestore for the authenticated user.
     * When any changes occur on other devices (insert, edit, delete), Room is immediately updated.
     */
    fun startLiveSync(userId: String, scope: CoroutineScope) {
        if (userId.isBlank()) return
        if (_isLiveSyncActive.value && currentActiveUserId == userId) return

        stopLiveSync()
        currentActiveUserId = userId

        val db = firestore ?: return
        val userDoc = db.collection("users").document(userId)

        try {
            // 1. Live listener for Market Expenses
            expenseListener = userDoc.collection("market_expenses")
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        Log.e(TAG, "Market expenses listener error: ${error.message}", error)
                        _lastSyncError.value = error.localizedMessage
                        return@addSnapshotListener
                    }
                    if (snapshots == null) return@addSnapshotListener

                    scope.launch(Dispatchers.IO) {
                        for (change in snapshots.documentChanges) {
                            val doc = change.document
                            val isDeleted = doc.getBoolean("deleted") == true || doc.get("deletedAt") != null
                            val docId = doc.id.toLongOrNull() ?: continue

                            if (isDeleted || change.type == DocumentChange.Type.REMOVED) {
                                expenseDao.deleteById(docId)
                            } else {
                                val serial = doc.getLong("serialNo")?.toInt() ?: 1
                                val date = doc.getString("date") ?: ""
                                val amount = doc.getDouble("amount") ?: 0.0
                                val note = doc.getString("note") ?: ""
                                val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                                if (date.isNotBlank() && amount > 0) {
                                    val item = MarketExpense(
                                        id = docId,
                                        serialNo = serial,
                                        date = date,
                                        amount = amount,
                                        note = note,
                                        createdAt = createdAt
                                    )
                                    expenseDao.insertExpense(item)
                                }
                            }
                        }
                    }
                }

            // 2. Live listener for Outstanding Entries
            outstandingListener = userDoc.collection("outstanding_entries")
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        Log.e(TAG, "Outstanding listener error: ${error.message}", error)
                        return@addSnapshotListener
                    }
                    if (snapshots == null) return@addSnapshotListener

                    scope.launch(Dispatchers.IO) {
                        for (change in snapshots.documentChanges) {
                            val doc = change.document
                            val isDeleted = doc.getBoolean("deleted") == true || doc.get("deletedAt") != null
                            val docId = doc.id.toLongOrNull() ?: continue

                            if (isDeleted || change.type == DocumentChange.Type.REMOVED) {
                                outstandingDao.deleteById(docId)
                            } else {
                                val serial = doc.getLong("serialNo")?.toInt() ?: 1
                                val date = doc.getString("date") ?: ""
                                val amount = doc.getDouble("amount") ?: 0.0
                                val note = doc.getString("note") ?: ""
                                val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                                if (date.isNotBlank() && amount > 0) {
                                    val item = OutstandingEntry(
                                        id = docId,
                                        serialNo = serial,
                                        date = date,
                                        amount = amount,
                                        note = note,
                                        createdAt = createdAt
                                    )
                                    outstandingDao.insertOutstanding(item)
                                }
                            }
                        }
                    }
                }

            // 3. Live listener for Payment Records
            paymentListener = userDoc.collection("payment_records")
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        Log.e(TAG, "Payment listener error: ${error.message}", error)
                        return@addSnapshotListener
                    }
                    if (snapshots == null) return@addSnapshotListener

                    scope.launch(Dispatchers.IO) {
                        for (change in snapshots.documentChanges) {
                            val doc = change.document
                            val isDeleted = doc.getBoolean("deleted") == true || doc.get("deletedAt") != null
                            val docId = doc.id.toLongOrNull() ?: continue

                            if (isDeleted || change.type == DocumentChange.Type.REMOVED) {
                                paymentDao.deleteById(docId)
                            } else {
                                val serial = doc.getLong("serialNo")?.toInt() ?: 1
                                val date = doc.getString("date") ?: ""
                                val amount = doc.getDouble("amount") ?: 0.0
                                val note = doc.getString("note") ?: ""
                                val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                                if (date.isNotBlank() && amount > 0) {
                                    val item = PaymentRecord(
                                        id = docId,
                                        serialNo = serial,
                                        date = date,
                                        amount = amount,
                                        note = note,
                                        createdAt = createdAt
                                    )
                                    paymentDao.insertPayment(item)
                                }
                            }
                        }
                    }
                }

            _isLiveSyncActive.value = true
            Log.d(TAG, "Live sync successfully attached for user: $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start live sync", e)
            _isLiveSyncActive.value = false
        }
    }

    /**
     * Stop and detach all active Firestore live listeners
     */
    fun stopLiveSync() {
        expenseListener?.remove()
        outstandingListener?.remove()
        paymentListener?.remove()
        expenseListener = null
        outstandingListener = null
        paymentListener = null
        currentActiveUserId = null
        _isLiveSyncActive.value = false
        Log.d(TAG, "Live sync detached")
    }

    /**
     * Flushes persistent offline queue items to Cloud Firestore
     */
    suspend fun flushPendingSyncQueue(userId: String): Result<Int> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(IllegalStateException("Firestore is not available"))
        val queueDao = syncQueueDao ?: return@withContext Result.success(0)

        _syncState.value = "SYNCING"
        var syncedCount = 0

        try {
            val pendingItems = queueDao.getPendingItemsList()
            val userDoc = db.collection("users").document(userId)

            for (item in pendingItems) {
                try {
                    val collectionName = when (item.entityType) {
                        "EXPENSE" -> "market_expenses"
                        "OUTSTANDING" -> "outstanding_entries"
                        "PAYMENT" -> "payment_records"
                        else -> continue
                    }
                    val docRef = userDoc.collection(collectionName).document(item.entityId.toString())

                    if (item.operation == "DELETE") {
                        val deletePayload = hashMapOf(
                            "deleted" to true,
                            "deletedAt" to System.currentTimeMillis(),
                            "ownerUid" to userId,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                        docRef.set(deletePayload, SetOptions.merge()).await()
                        docRef.delete().await()
                    } else {
                        // INSERT or UPDATE
                        val payloadMap = parseJsonToMap(item.payloadJson)
                        payloadMap["ownerUid"] = userId
                        payloadMap["updatedAt"] = FieldValue.serverTimestamp()
                        docRef.set(payloadMap, SetOptions.merge()).await()
                    }

                    // Successful write, remove from offline queue
                    queueDao.deleteById(item.queueId)
                    syncedCount++
                } catch (e: Exception) {
                    Log.e(TAG, "Failed syncing item ${item.queueId}: ${e.message}", e)
                    val updated = item.copy(
                        retryCount = item.retryCount + 1,
                        status = "FAILED",
                        errorMessage = e.localizedMessage
                    )
                    queueDao.update(updated)
                }
            }

            _syncState.value = "SUCCESS"
            _lastSyncError.value = null
            Result.success(syncedCount)
        } catch (e: Exception) {
            Log.e(TAG, "Queue flush failed", e)
            _syncState.value = "ERROR"
            _lastSyncError.value = e.localizedMessage
            Result.failure(e)
        }
    }

    /**
     * Pull all records from Firestore for this user and store into local Room SQLite.
     */
    suspend fun syncDownFromCloud(userId: String): Result<Int> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(IllegalStateException("Firestore is unavailable"))
        try {
            var count = 0
            val userDoc = db.collection("users").document(userId)

            // 1. Sync Market Expenses
            val expensesSnapshot = userDoc.collection("market_expenses").get().await()
            val cloudExpenses = mutableListOf<MarketExpense>()
            for (doc in expensesSnapshot.documents) {
                if (doc.getBoolean("deleted") == true || doc.get("deletedAt") != null) continue
                val serial = doc.getLong("serialNo")?.toInt() ?: 1
                val date = doc.getString("date") ?: ""
                val amount = doc.getDouble("amount") ?: 0.0
                val note = doc.getString("note") ?: ""
                val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                if (date.isNotBlank() && amount > 0) {
                    cloudExpenses.add(
                        MarketExpense(
                            id = doc.id.toLongOrNull() ?: 0L,
                            serialNo = serial,
                            date = date,
                            amount = amount,
                            note = note,
                            createdAt = createdAt
                        )
                    )
                }
            }
            if (cloudExpenses.isNotEmpty()) {
                expenseDao.insertAll(cloudExpenses)
                count += cloudExpenses.size
            }

            // 2. Sync Outstanding Entries
            val outSnapshot = userDoc.collection("outstanding_entries").get().await()
            val cloudOutstanding = mutableListOf<OutstandingEntry>()
            for (doc in outSnapshot.documents) {
                if (doc.getBoolean("deleted") == true || doc.get("deletedAt") != null) continue
                val serial = doc.getLong("serialNo")?.toInt() ?: 1
                val date = doc.getString("date") ?: ""
                val amount = doc.getDouble("amount") ?: 0.0
                val note = doc.getString("note") ?: ""
                val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                if (date.isNotBlank() && amount > 0) {
                    cloudOutstanding.add(
                        OutstandingEntry(
                            id = doc.id.toLongOrNull() ?: 0L,
                            serialNo = serial,
                            date = date,
                            amount = amount,
                            note = note,
                            createdAt = createdAt
                        )
                    )
                }
            }
            if (cloudOutstanding.isNotEmpty()) {
                outstandingDao.insertAll(cloudOutstanding)
                count += cloudOutstanding.size
            }

            // 3. Sync Payments
            val paySnapshot = userDoc.collection("payment_records").get().await()
            val cloudPayments = mutableListOf<PaymentRecord>()
            for (doc in paySnapshot.documents) {
                if (doc.getBoolean("deleted") == true || doc.get("deletedAt") != null) continue
                val serial = doc.getLong("serialNo")?.toInt() ?: 1
                val date = doc.getString("date") ?: ""
                val amount = doc.getDouble("amount") ?: 0.0
                val note = doc.getString("note") ?: ""
                val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                if (date.isNotBlank() && amount > 0) {
                    cloudPayments.add(
                        PaymentRecord(
                            id = doc.id.toLongOrNull() ?: 0L,
                            serialNo = serial,
                            date = date,
                            amount = amount,
                            note = note,
                            createdAt = createdAt
                        )
                    )
                }
            }
            if (cloudPayments.isNotEmpty()) {
                paymentDao.insertAll(cloudPayments)
                count += cloudPayments.size
            }

            Result.success(count)
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing down from cloud", e)
            Result.failure(e)
        }
    }

    /**
     * Create a complete versioned snapshot backup on Firestore
     */
    suspend fun createCloudBackup(
        userId: String,
        expenses: List<MarketExpense>,
        outstanding: List<OutstandingEntry>,
        payments: List<PaymentRecord>
    ): Result<BackupSnapshotInfo> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(IllegalStateException("Firestore is unavailable"))
        try {
            val userDoc = db.collection("users").document(userId)
            val timestamp = System.currentTimeMillis()
            val backupId = "backup_$timestamp"
            val backupTime = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(timestamp))

            val totalExpenses = expenses.sumOf { it.amount }
            val netBalance = outstanding.sumOf { it.amount } - payments.sumOf { it.amount }

            // Serialize full payload into JSON arrays
            val expArray = JSONArray()
            for (e in expenses) {
                expArray.put(JSONObject().apply {
                    put("id", e.id)
                    put("serialNo", e.serialNo)
                    put("date", e.date)
                    put("amount", e.amount)
                    put("note", e.note)
                    put("createdAt", e.createdAt)
                })
            }

            val outArray = JSONArray()
            for (o in outstanding) {
                outArray.put(JSONObject().apply {
                    put("id", o.id)
                    put("serialNo", o.serialNo)
                    put("date", o.date)
                    put("amount", o.amount)
                    put("note", o.note)
                    put("createdAt", o.createdAt)
                })
            }

            val payArray = JSONArray()
            for (p in payments) {
                payArray.put(JSONObject().apply {
                    put("id", p.id)
                    put("serialNo", p.serialNo)
                    put("date", p.date)
                    put("amount", p.amount)
                    put("note", p.note)
                    put("createdAt", p.createdAt)
                })
            }

            val snapshotData = hashMapOf(
                "backupId" to backupId,
                "backupTime" to backupTime,
                "timestamp" to timestamp,
                "ownerUid" to userId,
                "schemaVersion" to 2,
                "expensesCount" to expenses.size,
                "outstandingCount" to outstanding.size,
                "paymentsCount" to payments.size,
                "totalExpenses" to totalExpenses,
                "netBalance" to netBalance,
                "expensesJson" to expArray.toString(),
                "outstandingJson" to outArray.toString(),
                "paymentsJson" to payArray.toString(),
                "createdAt" to FieldValue.serverTimestamp()
            )

            // Save to versioned backup snapshot collection
            userDoc.collection("backup_snapshots").document(backupId).set(snapshotData).await()

            // Also update latest metadata pointer
            val latestMeta = hashMapOf(
                "latestBackupId" to backupId,
                "backupTime" to backupTime,
                "timestamp" to timestamp,
                "expensesCount" to expenses.size,
                "outstandingCount" to outstanding.size,
                "paymentsCount" to payments.size,
                "totalExpenses" to totalExpenses,
                "netBalance" to netBalance
            )
            userDoc.collection("sync_metadata").document("latest").set(latestMeta).await()

            val info = BackupSnapshotInfo(
                backupId = backupId,
                backupTime = backupTime,
                timestamp = timestamp,
                expensesCount = expenses.size,
                outstandingCount = outstanding.size,
                paymentsCount = payments.size,
                totalExpenses = totalExpenses,
                netBalance = netBalance
            )
            Result.success(info)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating cloud backup snapshot", e)
            Result.failure(e)
        }
    }

    /**
     * Get list of all available cloud backups
     */
    suspend fun getAvailableCloudBackups(userId: String): Result<List<BackupSnapshotInfo>> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(IllegalStateException("Firestore is unavailable"))
        try {
            val snapshots = db.collection("users").document(userId)
                .collection("backup_snapshots")
                .get()
                .await()

            val list = mutableListOf<BackupSnapshotInfo>()
            for (doc in snapshots.documents) {
                val bId = doc.getString("backupId") ?: doc.id
                val bTime = doc.getString("backupTime") ?: ""
                val bTimestamp = doc.getLong("timestamp") ?: 0L
                val expCount = doc.getLong("expensesCount")?.toInt() ?: 0
                val outCount = doc.getLong("outstandingCount")?.toInt() ?: 0
                val payCount = doc.getLong("paymentsCount")?.toInt() ?: 0
                val totalExp = doc.getDouble("totalExpenses") ?: 0.0
                val netBal = doc.getDouble("netBalance") ?: 0.0

                list.add(
                    BackupSnapshotInfo(
                        backupId = bId,
                        backupTime = bTime,
                        timestamp = bTimestamp,
                        expensesCount = expCount,
                        outstandingCount = outCount,
                        paymentsCount = payCount,
                        totalExpenses = totalExp,
                        netBalance = netBal
                    )
                )
            }
            list.sortByDescending { it.timestamp }
            Result.success(list)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching available backups", e)
            Result.failure(e)
        }
    }

    /**
     * Restore a chosen cloud backup snapshot. Supports Replace mode and Merge mode.
     */
    suspend fun restoreCloudBackup(
        userId: String,
        backupId: String,
        isReplaceMode: Boolean
    ): Result<Int> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(IllegalStateException("Firestore is unavailable"))
        try {
            val doc = db.collection("users").document(userId)
                .collection("backup_snapshots").document(backupId)
                .get().await()

            if (!doc.exists()) {
                return@withContext Result.failure(IllegalArgumentException("Backup snapshot not found"))
            }

            var totalRestored = 0
            if (isReplaceMode) {
                expenseDao.deleteAll()
                outstandingDao.deleteAll()
                paymentDao.deleteAll()
                syncQueueDao?.clearAll()
            }

            val expJson = doc.getString("expensesJson")
            if (!expJson.isNullOrBlank()) {
                val array = JSONArray(expJson)
                val list = mutableListOf<MarketExpense>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        MarketExpense(
                            id = if (isReplaceMode) obj.optLong("id", 0) else 0,
                            serialNo = obj.optInt("serialNo", i + 1),
                            date = obj.getString("date"),
                            amount = obj.getDouble("amount"),
                            note = obj.optString("note", ""),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
                expenseDao.insertAll(list)
                totalRestored += list.size
            }

            val outJson = doc.getString("outstandingJson")
            if (!outJson.isNullOrBlank()) {
                val array = JSONArray(outJson)
                val list = mutableListOf<OutstandingEntry>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        OutstandingEntry(
                            id = if (isReplaceMode) obj.optLong("id", 0) else 0,
                            serialNo = obj.optInt("serialNo", i + 1),
                            date = obj.getString("date"),
                            amount = obj.getDouble("amount"),
                            note = obj.optString("note", ""),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
                outstandingDao.insertAll(list)
                totalRestored += list.size
            }

            val payJson = doc.getString("paymentsJson")
            if (!payJson.isNullOrBlank()) {
                val array = JSONArray(payJson)
                val list = mutableListOf<PaymentRecord>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        PaymentRecord(
                            id = if (isReplaceMode) obj.optLong("id", 0) else 0,
                            serialNo = obj.optInt("serialNo", i + 1),
                            date = obj.getString("date"),
                            amount = obj.getDouble("amount"),
                            note = obj.optString("note", ""),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
                paymentDao.insertAll(list)
                totalRestored += list.size
            }

            Result.success(totalRestored)
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring backup snapshot", e)
            Result.failure(e)
        }
    }

    /**
     * Retrieve the latest cloud backup metadata
     */
    suspend fun getLatestBackupMetadata(userId: String): Result<Map<String, Any>?> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(IllegalStateException("Firestore is unavailable"))
        try {
            val doc = db.collection("users").document(userId)
                .collection("sync_metadata").document("latest").get().await()
            if (doc.exists()) {
                Result.success(doc.data)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching backup metadata", e)
            Result.failure(e)
        }
    }

    private fun parseJsonToMap(jsonString: String): HashMap<String, Any> {
        val map = HashMap<String, Any>()
        try {
            val obj = JSONObject(jsonString)
            val keys = obj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                map[key] = obj.get(key)
            }
        } catch (e: Exception) {
            Log.e(TAG, "JSON parse error", e)
        }
        return map
    }
}
