package com.example.data.repository

import com.example.data.local.ExpenseDao
import com.example.data.local.OutstandingDao
import com.example.data.local.PaymentDao
import com.example.data.model.MarketExpense
import com.example.data.model.OutstandingEntry
import com.example.data.model.PaymentRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class FinancialRepository(
    private val expenseDao: ExpenseDao,
    private val outstandingDao: OutstandingDao,
    private val paymentDao: PaymentDao
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
        expenseDao.insertExpense(item)
    }

    suspend fun updateExpense(expense: MarketExpense) = withContext(Dispatchers.IO) {
        expenseDao.updateExpense(expense)
    }

    suspend fun deleteExpense(expense: MarketExpense) = withContext(Dispatchers.IO) {
        expenseDao.deleteExpense(expense)
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
        outstandingDao.insertOutstanding(item)
    }

    suspend fun updateOutstanding(entry: OutstandingEntry) = withContext(Dispatchers.IO) {
        outstandingDao.updateOutstanding(entry)
    }

    suspend fun deleteOutstanding(entry: OutstandingEntry) = withContext(Dispatchers.IO) {
        outstandingDao.deleteOutstanding(entry)
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
        paymentDao.insertPayment(item)
    }

    suspend fun updatePayment(payment: PaymentRecord) = withContext(Dispatchers.IO) {
        paymentDao.updatePayment(payment)
    }

    suspend fun deletePayment(payment: PaymentRecord) = withContext(Dispatchers.IO) {
        paymentDao.deletePayment(payment)
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        expenseDao.deleteAll()
        outstandingDao.deleteAll()
        paymentDao.deleteAll()
    }

    // Backup & Restore
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
