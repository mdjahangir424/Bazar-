package com.example.data.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthActionCodeException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthManager(private val context: Context) {

    private val auth: FirebaseAuth? = run {
        com.example.data.cloud.FirebaseAppInitializer.ensureInitialized(context)
        try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.e("AuthManager", "FirebaseAuth initialization error: ${e.message}", e)
            null
        }
    }
    private val credentialManager: CredentialManager = CredentialManager.create(context)

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth?.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    init {
        auth?.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
        }
    }

    /**
     * Silent auto sign-in on app start via Google Credential Manager
     */
    fun attemptAutoSignIn(scope: CoroutineScope, onComplete: (Boolean) -> Unit = {}) {
        val currentAuth = auth ?: run {
            onComplete(false)
            return
        }

        if (currentAuth.currentUser != null) {
            _currentUser.value = currentAuth.currentUser
            onComplete(true)
            return
        }

        val clientId = try {
            context.getString(R.string.default_web_client_id).ifBlank { null }
        } catch (e: Exception) {
            null
        }

        if (clientId.isNullOrBlank()) {
            onComplete(false)
            return
        }

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(clientId)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(context, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    currentAuth.signInWithCredential(authCredential).await()
                    _currentUser.value = currentAuth.currentUser
                    onComplete(true)
                } else {
                    onComplete(false)
                }
            } catch (e: Exception) {
                onComplete(false)
            }
        }
    }

    /**
     * Interactive Google Sign-In using GetSignInWithGoogleOption
     */
    fun signInWithGoogle(
        activity: Activity,
        scope: CoroutineScope,
        onSuccess: (FirebaseUser) -> Unit,
        onError: (String) -> Unit,
        onCancelled: () -> Unit = {}
    ) {
        val currentAuth = auth ?: run {
            onError("Authentication service is unavailable")
            return
        }

        val clientId = try {
            context.getString(R.string.default_web_client_id).ifBlank { null }
        } catch (e: Exception) {
            null
        }

        if (clientId.isNullOrBlank()) {
            onError("Google Sign-In is not configured yet (web client ID missing).")
            return
        }

        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInOption)
            .build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(activity, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    val authResult = currentAuth.signInWithCredential(authCredential).await()
                    val user = authResult.user
                    if (user != null) {
                        _currentUser.value = user
                        onSuccess(user)
                    } else {
                        onError("Sign-in succeeded but user is null")
                    }
                } else {
                    onError("Unexpected credential type")
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w("Auth", "Google Sign-In cancelled: ${e.message}", e)
                onCancelled()
            } catch (e: Exception) {
                Log.e("Auth", "Google Sign-In failed", e)
                onError(formatBengaliAuthError(e))
            }
        }
    }

    /**
     * Email / Password Login
     */
    fun signInWithEmail(
        email: String,
        pass: String,
        scope: CoroutineScope,
        onSuccess: (FirebaseUser) -> Unit,
        onError: (String) -> Unit
    ) {
        val currentAuth = auth ?: run {
            onError("Authentication service is unavailable")
            return
        }
        val cleanEmail = email.trim()
        val cleanPass = pass.trim()
        if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
            onError("সঠিক ইমেইল এড্রেস লিখুন")
            return
        }
        if (cleanPass.length < 6) {
            onError("পাসওয়ার্ড কমপক্ষে ৬ অক্ষরের হতে হবে")
            return
        }

        scope.launch {
            try {
                val result = currentAuth.signInWithEmailAndPassword(cleanEmail, cleanPass).await()
                val user = result.user
                if (user != null) {
                    _currentUser.value = user
                    onSuccess(user)
                } else {
                    onError("ব্যবহারকারী খুঁজে পাওয়া যায়নি")
                }
            } catch (e: Exception) {
                Log.e("AuthManager", "Email sign-in failed", e)
                onError(formatBengaliAuthError(e))
            }
        }
    }

    /**
     * Email / Password Sign Up
     */
    fun signUpWithEmail(
        email: String,
        pass: String,
        scope: CoroutineScope,
        onSuccess: (FirebaseUser) -> Unit,
        onError: (String) -> Unit
    ) {
        val currentAuth = auth ?: run {
            onError("Authentication service is unavailable")
            return
        }
        val cleanEmail = email.trim()
        val cleanPass = pass.trim()
        if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
            onError("সঠিক ইমেইল এড্রেস লিখুন")
            return
        }
        if (cleanPass.length < 6) {
            onError("পাসওয়ার্ড কমপক্ষে ৬ অক্ষরের হতে হবে")
            return
        }

        scope.launch {
            try {
                val result = currentAuth.createUserWithEmailAndPassword(cleanEmail, cleanPass).await()
                val user = result.user
                if (user != null) {
                    _currentUser.value = user
                    // Send optional verification email
                    try {
                        user.sendEmailVerification().await()
                    } catch (ign: Exception) {
                        Log.w("AuthManager", "Verification email send failed", ign)
                    }
                    onSuccess(user)
                } else {
                    onError("একাউন্ট তৈরি করা যায়নি")
                }
            } catch (e: Exception) {
                Log.e("AuthManager", "Email sign-up failed", e)
                onError(formatBengaliAuthError(e))
            }
        }
    }

    /**
     * Password Reset Email
     */
    fun sendPasswordResetEmail(
        email: String,
        scope: CoroutineScope,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val currentAuth = auth ?: run {
            onError("Authentication service is unavailable")
            return
        }
        val cleanEmail = email.trim()
        if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
            onError("সঠিক ইমেইল এড্রেস লিখুন")
            return
        }

        scope.launch {
            try {
                currentAuth.sendPasswordResetEmail(cleanEmail).await()
                onSuccess()
            } catch (e: Exception) {
                Log.e("AuthManager", "Password reset failed", e)
                onError(formatBengaliAuthError(e))
            }
        }
    }

    /**
     * Send email verification link
     */
    fun sendEmailVerification(
        scope: CoroutineScope,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val user = auth?.currentUser ?: run {
            onError("আপনি লগইন অবস্থায় নেই")
            return
        }
        scope.launch {
            try {
                user.sendEmailVerification().await()
                onSuccess()
            } catch (e: Exception) {
                onError(formatBengaliAuthError(e))
            }
        }
    }

    /**
     * Sign out of Firebase and clear CredentialManager
     */
    fun signOut(scope: CoroutineScope, onComplete: () -> Unit = {}) {
        auth?.signOut()
        _currentUser.value = null
        scope.launch {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.e("Auth", "Failed to clear credential state", e)
            } finally {
                onComplete()
            }
        }
    }

    /**
     * Translate Firebase Auth exceptions to user-friendly Bengali messages
     */
    fun formatBengaliAuthError(e: Exception): String {
        return when (e) {
            is FirebaseAuthInvalidUserException -> "এই ইমেইলে কোনো একাউন্ট পাওয়া যায়নি অথবা একাউন্টটি বন্ধ রয়েছে।"
            is FirebaseAuthInvalidCredentialsException -> {
                val msg = e.message.orEmpty()
                if (msg.contains("password", ignoreCase = true)) {
                    "পাসওয়ার্ড ভুল হয়েছে, অনুগ্রহ করে সঠিক পাসওয়ার্ড দিন।"
                } else if (msg.contains("email", ignoreCase = true) || msg.contains("badly formatted", ignoreCase = true)) {
                    "ইমেইল ফরম্যাট সঠিক নয়।"
                } else {
                    "প্রদত্ত তথ্য সঠিক নয়। আবার চেষ্টা করুন।"
                }
            }
            is FirebaseAuthUserCollisionException -> "এই ইমেইল দিয়ে ইতিমধ্যে একটি একাউন্ট খোলা আছে। অনুগ্রহ করে লগইন করুন।"
            is FirebaseAuthWeakPasswordException -> "পাসওয়ার্ড খুবই দুর্বল। কমপক্ষে ৬টি অক্ষর বা সংখ্যা ব্যবহার করুন।"
            is FirebaseNetworkException -> "ইন্টারনেট সংযোগ বিচ্ছিন্ন! আপনার নেটওয়ার্ক চেক করুন।"
            is FirebaseAuthRecentLoginRequiredException -> "নিরাপত্তার স্বার্থে আবার লগইন করতে হবে।"
            is FirebaseAuthActionCodeException -> "অনুরোধটির মেয়াদ শেষ হয়ে গেছে বা পূর্বে ব্যবহৃত হয়েছে।"
            else -> {
                val raw = e.localizedMessage ?: "অজ্ঞাত ত্রুটি ঘটেছে"
                when {
                    raw.contains("network", ignoreCase = true) -> "ইন্টারনেট সংযোগ চেক করুন।"
                    raw.contains("password", ignoreCase = true) -> "পাসওয়ার্ড সঠিক নয়।"
                    raw.contains("email", ignoreCase = true) -> "ইমেইল সঠিক নয়।"
                    raw.contains("already in use", ignoreCase = true) -> "এই ইমেইলটি ইতিমধ্যে ব্যবহৃত।"
                    else -> raw
                }
            }
        }
    }
}
