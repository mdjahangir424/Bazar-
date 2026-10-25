package com.example.data.repository

import com.example.data.local.ExpenseDao
import com.example.data.local.OutstandingDao
import com.example.data.local.PaymentDao
import com.example.data.local.SyncQueueDao
import com.example.data.model.MarketExpense
import com.example.data.model.OutstandingEntry
import com.example.data.model.PaymentRecord
import com.example.data.model.SyncQueueItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class FinancialRepository(
    private val expenseDao: ExpenseDao,
    private val outstandingDao: OutstandingDao,
    private val paymentDao: PaymentDao,
    private val syncQueueDao: SyncQueueDao? = null,
    private val onDataChanged: (() -> Unit)? = null
) {
    val allExpenses: Flow<List<MarketExpense>> = expenseDao.getAllExpenses()
    val allOutstanding: Flow<List<OutstandingEntry>> = outstandingDao.getAllOutstanding()
    val allPayments: Flow<List<PaymentRecord>> = paymentDao.getAllPayments()

    fun getExpensesBetweenDates(startDate: String, endDate: String): Flow<List<MarketExpense>> =
        expenseDao.getExpensesBetweenDates(startDate, endDate)

    fun getOutstandingBetweenDates(startDate: String, endDate: String): Flow<List<OutstandingEntry>> =
        outstandingDao.getOutstandingBetweenDates(startDate, endDate)

    fun getPaymentsBetweenDates(startDate: String, endDate: String): Flow<List<PaymentRecord>> =
        paymentDao.getPaymentsBetweenDates(startDate, endDate)

    // Expense operations
    suspend fun addExpense(date: String, amount: Double, note: String): Long = withContext(Dispatchers.IO) {
        val nextSerial = (expenseDao.getMaxSerialNo() ?: 0) + 1
        val item = MarketExpense(
            serialNo = nextSerial,
            date = date,
            amount = amount,
            note = note.trim()
        )
        val id = expenseDao.insertExpense(item)
        val finalItem = item.copy(id = id)
        enqueueSync("EXPENSE", id, "INSERT", serializeExpense(finalItem))
        onDataChanged?.invoke()
        id
    }

    suspend fun updateExpense(expense: MarketExpense) = withContext(Dispatchers.IO) {
        expenseDao.updateExpense(expense)
        enqueueSync("EXPENSE", expense.id, "UPDATE", serializeExpense(expense))
        onDataChanged?.invoke()
    }

    suspend fun deleteExpense(expense: MarketExpense) = withContext(Dispatchers.IO) {
        expenseDao.deleteExpense(expense)
        enqueueSync("EXPENSE", expense.id, "DELETE", serializeExpense(expense))
        onDataChanged?.invoke()
    }

    // Outstanding operations
    suspend fun addOutstanding(date: String, amount: Double, note: String): Long = withContext(Dispatchers.IO) {
        val nextSerial = (outstandingDao.getMaxSerialNo() ?: 0) + 1
        val item = OutstandingEntry(
            serialNo = nextSerial,
            date = date,
            amount = amount,
            note = note.trim()
        )
        val id = outstandingDao.insertOutstanding(item)
        val finalItem = item.copy(id = id)
        enqueueSync("OUTSTANDING", id, "INSERT", serializeOutstanding(finalItem))
        onDataChanged?.invoke()
        id
    }

    suspend fun updateOutstanding(entry: OutstandingEntry) = withContext(Dispatchers.IO) {
        outstandingDao.updateOutstanding(entry)
        enqueueSync("OUTSTANDING", entry.id, "UPDATE", serializeOutstanding(entry))
        onDataChanged?.invoke()
    }

    suspend fun deleteOutstanding(entry: OutstandingEntry) = withContext(Dispatchers.IO) {
        outstandingDao.deleteOutstanding(entry)
        enqueueSync("OUTSTANDING", entry.id, "DELETE", serializeOutstanding(entry))
        onDataChanged?.invoke()
    }

    // Payment operations
    suspend fun addPayment(date: String, amount: Double, note: String): Long = withContext(Dispatchers.IO) {
        val nextSerial = (paymentDao.getMaxSerialNo() ?: 0) + 1
        val item = PaymentRecord(
            serialNo = nextSerial,
            date = date,
            amount = amount,
            note = note.trim()
        )
        val id = paymentDao.insertPayment(item)
        val finalItem = item.copy(id = id)
        enqueueSync("PAYMENT", id, "INSERT", serializePayment(finalItem))
        onDataChanged?.invoke()
        id
    }

    suspend fun updatePayment(payment: PaymentRecord) = withContext(Dispatchers.IO) {
        paymentDao.updatePayment(payment)
        enqueueSync("PAYMENT", payment.id, "UPDATE", serializePayment(payment))
        onDataChanged?.invoke()
    }

    suspend fun deletePayment(payment: PaymentRecord) = withContext(Dispatchers.IO) {
        paymentDao.deletePayment(payment)
        enqueueSync("PAYMENT", payment.id, "DELETE", serializePayment(payment))
        onDataChanged?.invoke()
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        expenseDao.deleteAll()
        outstandingDao.deleteAll()
        paymentDao.deleteAll()
        syncQueueDao?.clearAll()
    }

    private suspend fun enqueueSync(entityType: String, entityId: Long, op: String, payload: String) {
        try {
            syncQueueDao?.enqueue(
                SyncQueueItem(
                    entityType = entityType,
                    entityId = entityId,
                    operation = op,
                    payloadJson = payload,
                    timestamp = System.currentTimeMillis()
                )
            )
        } catch (e: Exception) {
            // Queue insertion failure should not block local offline operation
        }
    }

    private fun serializeExpense(item: MarketExpense): String {
        return JSONObject().apply {
            put("id", item.id)
            put("serialNo", item.serialNo)
            put("date", item.date)
            put("amount", item.amount)
            put("note", item.note)
            put("createdAt", item.createdAt)
        }.toString()
    }

    private fun serializeOutstanding(item: OutstandingEntry): String {
        return JSONObject().apply {
            put("id", item.id)
            put("serialNo", item.serialNo)
            put("date", item.date)
            put("amount", item.amount)
            put("note", item.note)
            put("createdAt", item.createdAt)
        }.toString()
    }

    private fun serializePayment(item: PaymentRecord): String {
        return JSONObject().apply {
            put("id", item.id)
            put("serialNo", item.serialNo)
            put("date", item.date)
            put("amount", item.amount)
            put("note", item.note)
            put("createdAt", item.createdAt)
        }.toString()
    }

    // Local JSON Backup & Restore
    suspend fun exportBackupJson(
        expenses: List<MarketExpense>,
        outstanding: List<OutstandingEntry>,
        payments: List<PaymentRecord>
    ): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("version", 1)
        root.put("appName", "Hisab Khata")
        root.put("exportedAt", System.currentTimeMillis())

        val expArray = JSONArray()
        for (e in expenses) {
            val obj = JSONObject()
            obj.put("id", e.id)
            obj.put("serialNo", e.serialNo)
            obj.put("date", e.date)
            obj.put("amount", e.amount)
            obj.put("note", e.note)
            obj.put("createdAt", e.createdAt)
            expArray.put(obj)
        }
        root.put("marketExpenses", expArray)

        val outArray = JSONArray()
        for (o in outstanding) {
            val obj = JSONObject()
            obj.put("id", o.id)
            obj.put("serialNo", o.serialNo)
            obj.put("date", o.date)
            obj.put("amount", o.amount)
            obj.put("note", o.note)
            obj.put("createdAt", o.createdAt)
            outArray.put(obj)
        }
        root.put("outstandingEntries", outArray)

        val payArray = JSONArray()
        for (p in payments) {
            val obj = JSONObject()
            obj.put("id", p.id)
            obj.put("serialNo", p.serialNo)
            obj.put("date", p.date)
            obj.put("amount", p.amount)
            obj.put("note", p.note)
            obj.put("createdAt", p.createdAt)
            payArray.put(obj)
        }
        root.put("paymentRecords", payArray)

        root.toString(2)
    }

    suspend fun importBackupJson(jsonString: String, replaceExisting: Boolean): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            if (!root.has("marketExpenses") && !root.has("outstandingEntries") && !root.has("paymentRecords")) {
                return@withContext Result.failure(IllegalArgumentException("Invalid Hisab Khata backup format"))
            }

            if (replaceExisting) {
                clearAllData()
            }

            var totalImported = 0

            val expArray = root.optJSONArray("marketExpenses")
            if (expArray != null) {
                val list = mutableListOf<MarketExpense>()
                for (i in 0 until expArray.length()) {
                    val obj = expArray.getJSONObject(i)
                    list.add(
                        MarketExpense(
                            id = if (replaceExisting) obj.optLong("id", 0) else 0,
                            serialNo = obj.optInt("serialNo", i + 1),
                            date = obj.getString("date"),
                            amount = obj.getDouble("amount"),
                            note = obj.optString("note", ""),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
                expenseDao.insertAll(list)
                totalImported += list.size
            }

            val outArray = root.optJSONArray("outstandingEntries")
            if (outArray != null) {
                val list = mutableListOf<OutstandingEntry>()
                for (i in 0 until outArray.length()) {
                    val obj = outArray.getJSONObject(i)
                    list.add(
                        OutstandingEntry(
                            id = if (replaceExisting) obj.optLong("id", 0) else 0,
                            serialNo = obj.optInt("serialNo", i + 1),
                            date = obj.getString("date"),
                            amount = obj.getDouble("amount"),
                            note = obj.optString("note", ""),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
                outstandingDao.insertAll(list)
                totalImported += list.size
            }

            val payArray = root.optJSONArray("paymentRecords")
            if (payArray != null) {
                val list = mutableListOf<PaymentRecord>()
                for (i in 0 until payArray.length()) {
                    val obj = payArray.getJSONObject(i)
                    list.add(
                        PaymentRecord(
                            id = if (replaceExisting) obj.optLong("id", 0) else 0,
                            serialNo = obj.optInt("serialNo", i + 1),
                            date = obj.getString("date"),
                            amount = obj.getDouble("amount"),
                            note = obj.optString("note", ""),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
                paymentDao.insertAll(list)
                totalImported += list.size
            }

            Result.success(totalImported)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
