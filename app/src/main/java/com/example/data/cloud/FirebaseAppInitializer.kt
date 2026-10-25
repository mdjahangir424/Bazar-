package com.example.data.cloud

import android.content.Context
import android.util.Log
import com.example.R
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

object FirebaseAppInitializer {
    private const val TAG = "FirebaseAppInit"

    fun ensureInitialized(context: Context) {
        if (FirebaseApp.getApps(context).isNotEmpty()) {
            return
        }

        // Try default initialization (e.g. from google-services.json if present)
        try {
            val app = FirebaseApp.initializeApp(context)
            if (app != null) {
                Log.d(TAG, "Initialized default FirebaseApp")
                return
            }
        } catch (e: Exception) {
            Log.w(TAG, "Default Firebase initialization did not succeed: ${e.message}")
        }

        // Fallback initialization with options to prevent crashes when google-services.json is missing
        try {
            val projectId = try {
                context.getString(R.string.firestore_database_id)
            } catch (e: Exception) {
                "hisab-khata-app"
            }

            val options = FirebaseOptions.Builder()
                .setApplicationId(context.packageName)
                .setApiKey("placeholder_api_key")
                .setProjectId(projectId.ifBlank { "hisab-khata-app" })
                .build()

            FirebaseApp.initializeApp(context, options)
            Log.d(TAG, "Initialized fallback FirebaseApp with options")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize fallback FirebaseApp", e)
        }
    }
}
