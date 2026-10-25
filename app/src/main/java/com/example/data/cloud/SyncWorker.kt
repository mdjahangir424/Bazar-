package com.example.data.cloud

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.data.local.AppDatabase
import com.google.firebase.auth.FirebaseAuth
import java.util.concurrent.TimeUnit

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("SyncWorker", "Executing offline sync queue flush in background...")
        FirebaseAppInitializer.ensureInitialized(applicationContext)
        val auth = try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            null
        }
        val userId = auth?.currentUser?.uid ?: return Result.success()

        val db = AppDatabase.getDatabase(applicationContext)
        val syncManager = FirestoreSyncManager(
            context = applicationContext,
            expenseDao = db.expenseDao(),
            outstandingDao = db.outstandingDao(),
            paymentDao = db.paymentDao(),
            syncQueueDao = db.syncQueueDao()
        )

        return try {
            val syncResult = syncManager.flushPendingSyncQueue(userId)
            if (syncResult.isSuccess) {
                Log.d("SyncWorker", "Successfully flushed queue: ${syncResult.getOrNull()} items synced")
                Result.success()
            } else {
                Log.w("SyncWorker", "Queue flush encountered error: ${syncResult.exceptionOrNull()?.message}")
                if (runAttemptCount < 3) Result.retry() else Result.failure()
            }
        } catch (e: Exception) {
            Log.e("SyncWorker", "Sync worker exception", e)
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    companion object {
        private const val WORK_NAME = "hisab_khata_offline_sync"

        fun enqueueSync(context: Context) {
            try {
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

                val request = OneTimeWorkRequestBuilder<SyncWorker>()
                    .setConstraints(constraints)
                    .setBackoffCriteria(
                        BackoffPolicy.EXPONENTIAL,
                        15,
                        TimeUnit.SECONDS
                    )
                    .build()

                WorkManager.getInstance(context).enqueueUniqueWork(
                    WORK_NAME,
                    ExistingWorkPolicy.REPLACE,
                    request
                )
            } catch (e: Exception) {
                Log.e("SyncWorker", "Failed to enqueue work", e)
            }
        }
    }
}
