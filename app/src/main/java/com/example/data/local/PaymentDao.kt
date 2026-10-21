package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.PaymentRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payment_records ORDER BY date DESC, id DESC")
    fun getAllPayments(): Flow<List<PaymentRecord>>

    @Query("SELECT * FROM payment_records WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC, id ASC")
    fun getPaymentsBetweenDates(startDate: String, endDate: String): Flow<List<PaymentRecord>>

    @Query("SELECT MAX(serialNo) FROM payment_records")
    suspend fun getMaxSerialNo(): Int?

    @Query("SELECT COUNT(*) FROM payment_records")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(payments: List<PaymentRecord>)

    @Update
    suspend fun updatePayment(payment: PaymentRecord)

    @Delete
    suspend fun deletePayment(payment: PaymentRecord)

    @Query("DELETE FROM payment_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM payment_records")
    suspend fun deleteAll()
}
