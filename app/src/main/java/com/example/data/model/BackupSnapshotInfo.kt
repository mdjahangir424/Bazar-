package com.example.data.model

data class BackupSnapshotInfo(
    val backupId: String = "",
    val backupTime: String = "",
    val timestamp: Long = 0L,
    val expensesCount: Int = 0,
    val outstandingCount: Int = 0,
    val paymentsCount: Int = 0,
    val totalExpenses: Double = 0.0,
    val netBalance: Double = 0.0
)
