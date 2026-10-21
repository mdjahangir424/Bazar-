package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.MarketExpense
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM market_expenses ORDER BY date DESC, id DESC")
    fun getAllExpenses(): Flow<List<MarketExpense>>

    @Query("SELECT * FROM market_expenses WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC, id ASC")
    fun getExpensesBetweenDates(startDate: String, endDate: String): Flow<List<MarketExpense>>

    @Query("SELECT MAX(serialNo) FROM market_expenses")
    suspend fun getMaxSerialNo(): Int?

    @Query("SELECT COUNT(*) FROM market_expenses")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: MarketExpense): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(expenses: List<MarketExpense>)

    @Update
    suspend fun updateExpense(expense: MarketExpense)

    @Delete
    suspend fun deleteExpense(expense: MarketExpense)

    @Query("DELETE FROM market_expenses WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM market_expenses")
    suspend fun deleteAll()
}
