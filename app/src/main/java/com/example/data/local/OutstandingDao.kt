package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.OutstandingEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface OutstandingDao {
    @Query("SELECT * FROM outstanding_entries ORDER BY date DESC, id DESC")
    fun getAllOutstanding(): Flow<List<OutstandingEntry>>

    @Query("SELECT * FROM outstanding_entries WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC, id ASC")
    fun getOutstandingBetweenDates(startDate: String, endDate: String): Flow<List<OutstandingEntry>>

    @Query("SELECT MAX(serialNo) FROM outstanding_entries")
    suspend fun getMaxSerialNo(): Int?

    @Query("SELECT COUNT(*) FROM outstanding_entries")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOutstanding(entry: OutstandingEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<OutstandingEntry>)

    @Update
    suspend fun updateOutstanding(entry: OutstandingEntry)

    @Delete
    suspend fun deleteOutstanding(entry: OutstandingEntry)

    @Query("DELETE FROM outstanding_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM outstanding_entries")
    suspend fun deleteAll()
}
