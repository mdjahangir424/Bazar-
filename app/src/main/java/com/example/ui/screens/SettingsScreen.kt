package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.CrimsonDanger
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Sync
import com.example.ui.util.AppLanguage
import com.example.ui.util.BanglaFormatter
import com.example.ui.viewmodel.FinancialViewModel
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

@Composable
fun SettingsScreen(
    viewModel: FinancialViewModel,
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    val language by viewModel.language.collectAsStateWithLifecycle()
    val allowNegative by viewModel.allowNegativeBalance.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isBn = language == AppLanguage.BANGLA
    val scope = rememberCoroutineScope()

    var showClearDataDialog by remember { mutableStateOf(false) }
    var confirmText by remember { mutableStateOf("") }

    var showRestoreConfirmDialog by remember { mutableStateOf(false) }
    var pendingRestoreJson by remember { mutableStateOf<String?>(null) }

    // File Picker for JSON Restore
    val restoreFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val reader = BufferedReader(InputStreamReader(inputStream))
                val content = reader.readText()
                reader.close()
                pendingRestoreJson = content
                showRestoreConfirmDialog = true
            } catch (e: Exception) {
                scope.launch {
                    snackbarHostState.showSnackbar("Failed to read file: ${e.message}")
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = if (isBn) "অ্যাপ সেটিংস" else "Settings",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = Navy800
        )
        Text(
            text = if (isBn) "ভাষা, ডাটা ব্যাকআপ ও সিস্টেম কনফিগারেশন" else "Language, data backup, and system configuration",
            style = MaterialTheme.typography.bodySmall,
            color = Slate600
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 0. Google Cloud Backup & Account Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (currentUser != null) EmeraldContainer else Navy800
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (currentUser != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SettingsIconBadge(
                            icon = Icons.Default.CloudDone,
                            bg = EmeraldPrimary,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isBn) "গুগল ক্লাউড সিঙ্ক সক্রিয়" else "Google Cloud Sync Active",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = EmeraldDark
                            )
                            Text(
                                text = currentUser?.email ?: (if (isBn) "সংযুক্ত" else "Connected"),
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate700
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (isBn)
                            "আপনার সমস্ত বাজার খরচ ও বাকি হিসাব গুগলের ক্লাউড ডাটাবেজে নিরাপদে সংরক্ষিত হচ্ছে। অ্যাপ মুছে গেলেও জিমেইল দিয়ে লগইন করলে ডাটা সাথে সাথে ফেরত আসবে।"
                        else
                            "All records are safely synced to Google Cloud. Data will be restored automatically upon re-login.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate700
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.syncFromCloud { res ->
                                    scope.launch {
                                        if (res.isSuccess) {
                                            snackbarHostState.showSnackbar(
                                                if (isBn) "ক্লাউড থেকে সফলভাবে সিঙ্ক সম্পন্ন হয়েছে" else "Cloud sync completed successfully"
                                            )
                                        } else {
                                            snackbarHostState.showSnackbar("Sync failed: ${res.exceptionOrNull()?.message}")
                                        }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldDark),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isBn) "এখনই সিঙ্ক করুন" else "Sync Now", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.signOut {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            if (isBn) "লগ আউট সম্পন্ন হয়েছে" else "Signed out successfully"
                                        )
                                    }
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (isBn) "লগ আউট" else "Sign Out", fontSize = 12.sp, color = CrimsonDanger)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SettingsIconBadge(
                            icon = Icons.Default.CloudSync,
                            bg = Color(0x33FFFFFF),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isBn) "ক্লাউড ব্যাকআপ ও গুগল লগইন" else "Cloud Backup & Google Login",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = if (isBn) "ডাটা কখনো হারাবে না" else "Never lose your financial records",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFCBD5E1)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (isBn)
                            "মোবাইল হারিয়ে গেলে, পরিবর্তন করলে বা অ্যাপটি মুছে গেলেও আপনার একই গুগল আইডি দিয়ে লগইন করলে সমস্ত বাজার খরচ ও বাকি হিসাব সাথে সাথে ফেরত পাবেন।"
                        else
                            "Sign in with Google so even if you uninstall the app or switch phones, you can recover all records instantly.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFE2E8F0),
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val activity = context as? Activity
                            if (activity != null) {
                                viewModel.signInWithGoogle(
                                    activity = activity,
                                    onSuccess = {
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                if (isBn) "গুগল দিয়ে সফলভাবে লগইন হয়েছে এবং ক্লাউড সিঙ্ক সক্রিয়!" else "Signed in with Google! Cloud sync active."
                                            )
                                        }
                                    },
                                    onError = { err ->
                                        scope.launch { snackbarHostState.showSnackbar(err) }
                                    }
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("google_signin_button")
                    ) {
                        Icon(Icons.Default.AccountCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBn) "গুগল দিয়ে সাইন ইন করুন (Google Login)" else "Sign in with Google",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 1. PRIMARY VERIFIED OWNERSHIP CARD (মালিকানা নিশ্চিত করুন)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("owner_verification_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(2.dp, EmeraldPrimary),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Header with Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SettingsIconBadge(
                            icon = Icons.Default.VerifiedUser,
                            bg = EmeraldContainer,
                            tint = EmeraldDark
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isBn) "মালিকানা নিশ্চিত করুন" else "Confirm Ownership",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Navy900
                            )
                            Text(
                                text = if (isBn) "অ্যাপের মূল স্বত্বাধিকারী ও নির্মাতা পরিচিতি" else "Official App Owner & Developer",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldDark
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = EmeraldContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldDark, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isBn) "যাচাইকৃত" else "Verified",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = EmeraldDark
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 1. Owner Name
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Slate100, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Navy800, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBn) "১. অ্যাপের মূল মালিক ও নির্মাতার নাম:" else "1. Owner & Developer Name:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = Slate600
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "মো: জাহাঙ্গীর আলম (Md Jahangir Alam)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Navy900,
                        modifier = Modifier.padding(start = 26.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 2. Mobile Number (SUPER PROMINENT)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(EmeraldContainer.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .border(1.5.dp, EmeraldPrimary, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = EmeraldDark, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isBn) "২. মোবাইল নাম্বার:" else "2. Mobile Number:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = EmeraldDark
                            )
                        }

                        // Call / Action Chip
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = EmeraldDark,
                            modifier = Modifier.clickable {
                                try {
                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:+8801778600028")
                                    }
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isBn) "কল করুন" else "Call",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "+8801778600028",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        ),
                        color = EmeraldDark,
                        modifier = Modifier.padding(start = 28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 3. Email Address
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Slate100, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Email, contentDescription = null, tint = Navy800, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBn) "৩. ইমেইল এড্রেস:" else "3. Email Address:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = Slate600
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "mdjahangiralam2528@gmail.com",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Navy900,
                        modifier = Modifier.padding(start = 26.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Ownership text description
                Text(
                    text = if (isBn)
                        "এই অ্যাপটি মো: জাহাঙ্গীর আলম-এর পরিকল্পনা ও নির্দেশনায় প্রস্তুতকৃত ব্যক্তিগত হিসাব খাতা। আপনি যেকোনো ব্যক্তিকে ব্যবহারের জন্য অ্যাপটি প্রদান করতে পারেন; অ্যাপের ভেতরে ও প্রতিটি মুদ্রিত রিপোর্টে আপনার এই মালিকানা সর্বদা স্থায়ীভাবে সংরক্ষিত ও নিশ্চিত থাকবে।"
                    else
                        "This application is created and owned by Md Jahangir Alam. You can share this app with anyone; your ownership and developer attribution will permanently appear in the app and on all generated reports.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate600,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // All Rights Reserved
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Slate100
                ) {
                    Text(
                        text = "© 2026 Md Jahangir Alam. সর্বস্বত্ব সংরক্ষিত (All Rights Reserved).",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = EmeraldDark,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Language & Display Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isBn) "ভাষা ও ডিসপ্লে" else "Language & Display",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Navy800
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Language Toggle Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.toggleLanguage() }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SettingsIconBadge(icon = Icons.Default.Language, bg = EmeraldContainer, tint = EmeraldDark)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isBn) "অ্যাপের ভাষা" else "App Language",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                            )
                            Text(
                                text = if (isBn) "বাংলা (ডিফল্ট)" else "English",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate600
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.toggleLanguage() },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("toggle_language_button")
                    ) {
                        Text(
                            text = if (isBn) "English" else "বাংলা",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp), color = Slate200)

                // Currency Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SettingsIconBadge(icon = Icons.Default.MonetizationOn, bg = EmeraldContainer, tint = EmeraldDark)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isBn) "মুদ্রা (Currency)" else "Currency",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                            )
                            Text(
                                text = "Bangladeshi Taka (৳)",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate600
                            )
                        }
                    }
                    Text(
                        text = "৳ BDT",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Navy800
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp), color = Slate200)

                // Timezone Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SettingsIconBadge(icon = Icons.Default.AccessTime, bg = EmeraldContainer, tint = EmeraldDark)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isBn) "টাইমজোন" else "Timezone",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                            )
                            Text(
                                text = "Asia/Dhaka (UTC+6:00)",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate600
                            )
                        }
                    }
                    Text(
                        text = "ঢাকা",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Navy800
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp), color = Slate200)

                // Date Format Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SettingsIconBadge(icon = Icons.Default.CalendarToday, bg = EmeraldContainer, tint = EmeraldDark)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isBn) "তারিখ ফরম্যাট" else "Date Format",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                            )
                            Text(
                                text = "DD-MM-YYYY",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate600
                            )
                        }
                    }
                    Text(
                        text = "দিন-মাস-বছর",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate600
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Accounting Rules Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isBn) "হিসাবরক্ষণ নীতিমালা" else "Accounting Rules",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Navy800
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBn) "নেগেটিভ বাকি ব্যালেন্স অনুমতি দিন" else "Allow Negative Balance",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                        Text(
                            text = if (isBn)
                                "চালু থাকলে বর্তমান বকেয়ার চেয়ে অতিরিক্ত পরিশোধ রেকর্ড করা যাবে।"
                            else
                                "Permits payments greater than current outstanding balance.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate600
                        )
                    }
                    Switch(
                        checked = allowNegative,
                        onCheckedChange = { viewModel.setAllowNegativeBalance(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = EmeraldPrimary)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Backup and Restore Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isBn) "ডাটা ব্যাকআপ ও রিস্টোর" else "Data Backup & Restore",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Navy800
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isBn)
                        "আপনার সকল লেনদেন সুরক্ষিত রাখতে মোবাইলে JSON ফাইল ব্যাকআপ সংরক্ষণ করুন বা যেকোনো সময় রিস্টোর করুন।"
                    else
                        "Export a secure JSON backup of your records to keep offline or restore anytime.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate600
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Export Backup
                    Button(
                        onClick = {
                            viewModel.exportBackup(context) { file ->
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    file
                                )
                                val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                    type = "application/json"
                                    putExtra(android.content.Intent.EXTRA_STREAM, uri)
                                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                val chooser = android.content.Intent.createChooser(shareIntent, "Save Hisab Khata Backup")
                                chooser.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(chooser)
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        if (isBn) "ব্যাকআপ ফাইল তৈরি হয়েছে" else "Backup file generated"
                                    )
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Navy800),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("export_backup_button")
                    ) {
                        Icon(Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isBn) "ব্যাকআপ নিন" else "Export Backup",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    // Restore Backup
                    OutlinedButton(
                        onClick = {
                            restoreFilePicker.launch("application/json")
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("import_backup_button")
                    ) {
                        Icon(Icons.Default.Restore, contentDescription = null, tint = EmeraldDark, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isBn) "রিস্টোর করুন" else "Import Backup",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Danger Zone: Clear All Data
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isBn) "সতর্কতামূলক ব্যবস্থা" else "Danger Zone",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = CrimsonDanger
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isBn)
                        "অ্যাপের সমস্ত বাজার খরচ, বাকি হিসাব ও পরিশোধের রেকর্ড সম্পূর্ণভাবে মুছে ফেলুন।"
                    else
                        "Permanently delete all market expenses, outstanding records, and payments.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate600
                )
                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        confirmText = ""
                        showClearDataDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonDanger),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("clear_all_data_btn")
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isBn) "সকল ডাটা মুছে ফেলুন" else "Clear All Data",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        // 5. App Ownership & Copyright Certificate Summary Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("app_ownership_summary_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Slate100),
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = EmeraldDark,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBn) "মালিকানা ও লাইসেন্স সনদ" else "Ownership & License Certificate",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Navy900
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isBn)
                        "মালিক ও নির্মাতা: মো: জাহাঙ্গীর আলম (Md Jahangir Alam)\nমোবাইল: +8801778600028 | ইমেইল: mdjahangiralam2528@gmail.com\n\n© 2026 Md Jahangir Alam. সর্বস্বত্ব সংরক্ষিত (All Rights Reserved)।"
                    else
                        "Owner & Developer: Md Jahangir Alam\nMobile: +8801778600028 | Email: mdjahangiralam2528@gmail.com\n\n© 2026 Md Jahangir Alam. All Rights Reserved.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate700,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 6. About App & System Architecture Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Navy800)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBn) "হিসাব খাতা সিস্টেম তথ্য" else "System Architecture",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Navy800
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = if (isBn)
                        "হিসাব খাতা – দৈনিক হিসাব একটি ১০০% অফলাইন ও ব্যক্তিগত আর্থিক রেকর্ড ম্যানেজমেন্ট সিস্টেম। এটি আপনার ফোনের নিজস্ব SQLite/Room ডাটাবেজ এবং গুগলের ফায়ারবেস ক্লাউড স্টোরেজে সুরক্ষিতভাবে সংরক্ষিত থাকে।"
                    else
                        "Hisab Khata – Daily Hisab is a personal finance and outstanding ledger management app powered by Android Room SQLite on-device storage and Google Firebase Cloud Firestore.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate600,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Version: 1.0.0 (Production Release)",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate600
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Confirmation Dialog for Restore
    if (showRestoreConfirmDialog) {
        AlertDialog(
            onDismissRequest = {
                showRestoreConfirmDialog = false
                pendingRestoreJson = null
            },
            icon = { Icon(Icons.Default.Restore, contentDescription = null, tint = EmeraldPrimary) },
            title = {
                Text(
                    text = if (isBn) "ব্যাকআপ রিস্টোর নিশ্চিতকরণ" else "Confirm Backup Restore",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = if (isBn)
                        "আপনি কি এই ব্যাকআপ ফাইলটি রিস্টোর করতে চান? পূর্ববর্তী ডাটা প্রতিস্থাপন করা হবে।"
                    else
                        "Do you want to restore records from this backup? Existing records will be replaced.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val json = pendingRestoreJson
                        if (json != null) {
                            viewModel.importBackup(json, replaceExisting = true) { res ->
                                if (res.isSuccess) {
                                    val count = res.getOrNull() ?: 0
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            if (isBn) "$count টি রেকর্ড সফলভাবে রিস্টোর হয়েছে" else "$count records successfully restored"
                                        )
                                    }
                                } else {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            "Restore error: ${res.exceptionOrNull()?.message}"
                                        )
                                    }
                                }
                            }
                        }
                        showRestoreConfirmDialog = false
                        pendingRestoreJson = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text(if (isBn) "হ্যাঁ, রিস্টোর করুন" else "Yes, Restore")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    showRestoreConfirmDialog = false
                    pendingRestoreJson = null
                }) {
                    Text(if (isBn) "বাতিল" else "Cancel")
                }
            }
        )
    }

    // Confirmation Dialog for Clear All Data
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = CrimsonDanger) },
            title = {
                Text(
                    text = if (isBn) "স্থায়ীভাবে সকল ডাটা মুছে ফেলবেন?" else "Permanently Delete All Data?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = CrimsonDanger
                )
            },
            text = {
                Column {
                    Text(
                        text = if (isBn)
                            "এই কাজটি ফিরিয়ে আনা সম্ভব নয়। সমস্ত বাজার খরচ, বাকি ও পরিশোধ রেকর্ড মুছে যাবে। নিশ্চিত করতে নিচের ঘরে 'মুছে ফেলুন' বা 'DELETE' লিখুন:"
                        else
                            "This action cannot be undone. Type 'DELETE' below to confirm:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = confirmText,
                        onValueChange = { confirmText = it },
                        label = { Text("মুছে ফেলুন / DELETE") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                val isMatched = confirmText.trim().equals("DELETE", ignoreCase = true) ||
                        confirmText.trim() == "মুছে ফেলুন"
                Button(
                    onClick = {
                        if (isMatched) {
                            viewModel.clearAllData {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        if (isBn) "সমস্ত ডাটা মুছে ফেলা হয়েছে" else "All data successfully deleted"
                                    )
                                }
                            }
                            showClearDataDialog = false
                        }
                    },
                    enabled = isMatched,
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonDanger)
                ) {
                    Text(if (isBn) "চূড়ান্ত মুছে ফেলুন" else "Delete Permanently")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showClearDataDialog = false }) {
                    Text(if (isBn) "বাতিল" else "Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsIconBadge(icon: ImageVector, bg: Color, tint: Color) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
    }
}
