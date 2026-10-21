package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.MarketExpense
import com.example.data.model.OutstandingEntry
import com.example.data.model.PaymentRecord
import com.example.data.repository.FinancialRepository
import com.example.ui.util.AppLanguage
import com.example.ui.util.BanglaFormatter
import com.example.ui.util.PdfReportGenerator
import com.example.ui.util.ReportType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FinancialAccountingTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: FinancialRepository
    private lateinit var context: Context

    @Before
    fun createDb() {
        context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = FinancialRepository(
            expenseDao = db.expenseDao(),
            outstandingDao = db.outstandingDao(),
            paymentDao = db.paymentDao()
        )
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun testScenario1_to_6_FinancialAccountingCalculations() = runBlocking {
        // Test 1: Add market expense of ৳500 and another of ৳250.
        // Expected market expense total: ৳750.
        val expId1 = repository.addExpense("2026-10-21", 500.0, "মাছ ও সবজি")
        val expId2 = repository.addExpense("2026-10-21", 250.0, "চাল ও ডাল")

        var expensesList = repository.allExpenses.first()
        var marketExpenseTotal = expensesList.sumOf { it.amount }
        assertEquals(750.0, marketExpenseTotal, 0.001)

        // Test 2: Add outstanding entries of ৳1,000 and ৳500.
        // Expected total outstanding added: ৳1,500.
        repository.addOutstanding("2026-10-21", 1000.0, "মুদির দোকান বাকি")
        repository.addOutstanding("2026-10-21", 500.0, "দুধের বাকি")

        val outstandingList = repository.allOutstanding.first()
        val totalOutstandingAdded = outstandingList.sumOf { it.amount }
        assertEquals(1500.0, totalOutstandingAdded, 0.001)

        // Test 3: Record a payment of ৳300.
        // Expected current outstanding balance: ৳1,200.
        repository.addPayment("2026-10-21", 300.0, "দোকানদারকে আংশিক পরিশোধ")

        val paymentsList = repository.allPayments.first()
        val totalPayments = paymentsList.sumOf { it.amount }
        assertEquals(300.0, totalPayments, 0.001)

        val currentOutstandingBalance = totalOutstandingAdded - totalPayments
        assertEquals(1200.0, currentOutstandingBalance, 0.001)

        // Test 4: Market expense total must remain ৳750 and must NOT alter the outstanding balance.
        val currentMarketTotal = repository.allExpenses.first().sumOf { it.amount }
        assertEquals(750.0, currentMarketTotal, 0.001)
        assertEquals(1200.0, currentOutstandingBalance, 0.001)

        // Test 5: Edit an expense from ৳500 to ৳600.
        // Expected market expense total: ৳850.
        val firstExp = expensesList.first { it.id == expId1 }
        repository.updateExpense(firstExp.copy(amount = 600.0))

        expensesList = repository.allExpenses.first()
        marketExpenseTotal = expensesList.sumOf { it.amount }
        assertEquals(850.0, marketExpenseTotal, 0.001)

        // Test 6: Delete the ৳250 expense.
        // Expected market expense total: ৳600.
        val secondExp = expensesList.first { it.id == expId2 }
        repository.deleteExpense(secondExp)

        expensesList = repository.allExpenses.first()
        marketExpenseTotal = expensesList.sumOf { it.amount }
        assertEquals(600.0, marketExpenseTotal, 0.001)
    }

    @Test
    fun testBanglaFormatterAndDigits() {
        val englishNumber = "1450"
        val banglaDigits = BanglaFormatter.toBanglaDigits(englishNumber)
        assertEquals("১৪৫০", banglaDigits)

        val formattedCurrency = BanglaFormatter.formatCurrency(1450.0, true)
        assertTrue(formattedCurrency.contains("৳"))
        assertTrue(formattedCurrency.contains("১,৪৫০"))

        val todayIso = BanglaFormatter.getTodayDhakaIso()
        assertNotNull(todayIso)
        assertTrue(todayIso.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
    }

    @Test
    fun testBackupAndRestore() = runBlocking {
        repository.addExpense("2026-10-21", 350.0, "Backup Test")
        repository.addOutstanding("2026-10-21", 900.0, "Backup Due")
        repository.addPayment("2026-10-21", 400.0, "Backup Payment")

        val expenses = repository.allExpenses.first()
        val outstanding = repository.allOutstanding.first()
        val payments = repository.allPayments.first()

        val backupJson = repository.exportBackupJson(expenses, outstanding, payments)
        assertTrue(backupJson.contains("Backup Test"))
        assertTrue(backupJson.contains("Backup Due"))

        // Clear and restore
        repository.clearAllData()
        assertEquals(0, repository.allExpenses.first().size)

        val restoreResult = repository.importBackupJson(backupJson, replaceExisting = true)
        assertTrue(restoreResult.isSuccess)
        assertEquals(3, restoreResult.getOrNull())

        assertEquals(1, repository.allExpenses.first().size)
        assertEquals(350.0, repository.allExpenses.first()[0].amount, 0.001)
    }

    @Test
    fun testCsvReportGeneration() = runBlocking {
        repository.addExpense("2026-10-21", 500.0, "Report Exp")
        repository.addOutstanding("2026-10-21", 1000.0, "Report Due")
        repository.addPayment("2026-10-21", 200.0, "Report Pay")

        val expenses = repository.allExpenses.first()
        val outstanding = repository.allOutstanding.first()
        val payments = repository.allPayments.first()

        val csvExp = com.example.ui.util.CsvExporter.exportMarketExpensesCsv(
            context = context,
            list = expenses,
            language = AppLanguage.BANGLA
        )
        assertTrue(csvExp.exists())
        val csvExpText = csvExp.readText()
        assertTrue(csvExpText.contains("বাজার খরচ"))
        assertTrue(csvExpText.contains("500.0"))

        val csvOut = com.example.ui.util.CsvExporter.exportOutstandingCsv(
            context = context,
            outstanding = outstanding,
            payments = payments,
            language = AppLanguage.BANGLA
        )
        assertTrue(csvOut.exists())
        val csvOutText = csvOut.readText()
        assertTrue(csvOutText.contains("নতুন বাকি তালিকা"))
        assertTrue(csvOutText.contains("1000.0"))
        assertTrue(csvOutText.contains("200.0"))
    }
}
