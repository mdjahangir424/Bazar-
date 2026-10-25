package com.example.data.cloud

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.local.ExpenseDao
import com.example.data.local.OutstandingDao
import com.example.data.local.PaymentDao
import com.example.data.model.MarketExpense
import com.example.data.model.OutstandingEntry
import com.example.data.model.PaymentRecord
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirestoreSyncManager(
    private val context: Context,
    private val expenseDao: ExpenseDao,
    private val outstandingDao: OutstandingDao,
    private val paymentDao: PaymentDao
) {
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
            Log.e("FirestoreSyncManager", "Could not initialize Firestore: ${e.message}", e)
            null
        }
    }

    /**
     * Pull all records from Firestore for this user and store into local Room SQLite.
     * Guarantees that if the user deletes the app or switches devices, logging in recovers all accounts!
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
            Log.e("FirestoreSync", "Error syncing from cloud", e)
            Result.failure(e)
        }
    }

    /**
     * Upload local records to Firestore under user scope
     */
    suspend fun uploadExpense(userId: String, expense: MarketExpense) = withContext(Dispatchers.IO) {
        try {
            val docId = expense.id.toString()
            val data = hashMapOf(
                "userId" to userId,
                "serialNo" to expense.serialNo,
                "date" to expense.date,
                "amount" to expense.amount,
                "note" to expense.note,
                "createdAt" to expense.createdAt
            )
            val db = firestore ?: return@withContext
            db.collection("users").document(userId)
                .collection("market_expenses").document(docId)
                .set(data).await()
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Error saving expense to cloud", e)
        }
    }

    suspend fun deleteExpense(userId: String, expenseId: Long) = withContext(Dispatchers.IO) {
        try {
            val db = firestore ?: return@withContext
            db.collection("users").document(userId)
                .collection("market_expenses").document(expenseId.toString())
                .delete().await()
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Error deleting expense from cloud", e)
        }
    }

    suspend fun uploadOutstanding(userId: String, entry: OutstandingEntry) = withContext(Dispatchers.IO) {
        try {
            val docId = entry.id.toString()
            val data = hashMapOf(
                "userId" to userId,
                "serialNo" to entry.serialNo,
                "date" to entry.date,
                "amount" to entry.amount,
                "note" to entry.note,
                "createdAt" to entry.createdAt
            )
            val db = firestore ?: return@withContext
            db.collection("users").document(userId)
                .collection("outstanding_entries").document(docId)
                .set(data).await()
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Error saving outstanding to cloud", e)
        }
    }

    suspend fun deleteOutstanding(userId: String, entryId: Long) = withContext(Dispatchers.IO) {
        try {
            val db = firestore ?: return@withContext
            db.collection("users").document(userId)
                .collection("outstanding_entries").document(entryId.toString())
                .delete().await()
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Error deleting outstanding from cloud", e)
        }
    }

    suspend fun uploadPayment(userId: String, payment: PaymentRecord) = withContext(Dispatchers.IO) {
        try {
            val docId = payment.id.toString()
            val data = hashMapOf(
                "userId" to userId,
                "serialNo" to payment.serialNo,
                "date" to payment.date,
                "amount" to payment.amount,
                "note" to payment.note,
                "createdAt" to payment.createdAt
            )
            val db = firestore ?: return@withContext
            db.collection("users").document(userId)
                .collection("payment_records").document(docId)
                .set(data).await()
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Error saving payment to cloud", e)
        }
    }

    suspend fun deletePayment(userId: String, paymentId: Long) = withContext(Dispatchers.IO) {
        try {
            val db = firestore ?: return@withContext
            db.collection("users").document(userId)
                .collection("payment_records").document(paymentId.toString())
                .delete().await()
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Error deleting payment from cloud", e)
        }
    }

    /**
     * Complete cloud-based backup feature using Firebase Firestore.
     * Backs up all grocery expenses, outstanding balances, payments, and metadata.
     */
    suspend fun backupAllToCloud(
        userId: String,
        expenses: List<MarketExpense>,
        outstanding: List<OutstandingEntry>,
        payments: List<PaymentRecord>
    ): Result<String> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(IllegalStateException("Firestore is unavailable"))
        try {
            val userDoc = db.collection("users").document(userId)

            // 1. Upload all grocery expenses
            for (item in expenses) {
                val data = hashMapOf(
                    "userId" to userId,
                    "serialNo" to item.serialNo,
                    "date" to item.date,
                    "amount" to item.amount,
                    "note" to item.note,
                    "createdAt" to item.createdAt
                )
                userDoc.collection("market_expenses").document(item.id.toString()).set(data).await()
            }

            // 2. Upload all outstanding dues
            for (item in outstanding) {
                val data = hashMapOf(
                    "userId" to userId,
                    "serialNo" to item.serialNo,
                    "date" to item.date,
                    "amount" to item.amount,
                    "note" to item.note,
                    "createdAt" to item.createdAt
                )
                userDoc.collection("outstanding_entries").document(item.id.toString()).set(data).await()
            }

            // 3. Upload all payments
            for (item in payments) {
                val data = hashMapOf(
                    "userId" to userId,
                    "serialNo" to item.serialNo,
                    "date" to item.date,
                    "amount" to item.amount,
                    "note" to item.note,
                    "createdAt" to item.createdAt
                )
                userDoc.collection("payment_records").document(item.id.toString()).set(data).await()
            }

            // 4. Record Backup Metadata
            val backupTime = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault()).format(java.util.Date())
            val metadata = hashMapOf(
                "userId" to userId,
                "backupTime" to backupTime,
                "timestamp" to System.currentTimeMillis(),
                "expensesCount" to expenses.size,
                "outstandingCount" to outstanding.size,
                "paymentsCount" to payments.size,
                "totalExpenses" to expenses.sumOf { it.amount },
                "netBalance" to (outstanding.sumOf { it.amount } - payments.sumOf { it.amount })
            )
            userDoc.collection("backup_metadata").document("latest").set(metadata).await()

            Result.success(backupTime)
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Error backing up to cloud", e)
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
                .collection("backup_metadata").document("latest").get().await()
            if (doc.exists()) {
                Result.success(doc.data)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Error fetching backup metadata", e)
            Result.failure(e)
        }
    }
}
