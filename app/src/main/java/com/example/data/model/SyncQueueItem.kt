package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Persistent sync queue for offline-first data replication.
 * Captures all local insertions, updates, and deletions so they survive app restarts
 * and can be reliably flushed to Cloud Firestore once network is restored.
 */
@Entity(tableName = "sync_queue")
data class SyncQueueItem(
    @PrimaryKey(autoGenerate = true)
    val queueId: Long = 0,
    val entityType: String, // "EXPENSE", "OUTSTANDING", "PAYMENT"
    val entityId: Long,
    val operation: String, // "INSERT", "UPDATE", "DELETE"
    val payloadJson: String,
    val timestamp: Long = System.currentTimeMillis(),
    val retryCount: Int = 0,
    val status: String = "PENDING", // "PENDING", "PROCESSING", "FAILED"
    val errorMessage: String? = null
)
