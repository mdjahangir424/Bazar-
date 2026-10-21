package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payment_records")
data class PaymentRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val serialNo: Int = 1,
    val date: String, // Format: YYYY-MM-DD (stored in Asia/Dhaka time)
    val amount: Double,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
