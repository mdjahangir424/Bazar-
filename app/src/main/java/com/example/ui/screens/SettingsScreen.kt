package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.util.AppLanguage
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
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val reminderEnabled by viewModel.reminderEnabled.collectAsStateWithLifecycle()
    val reminderTime by viewModel.reminderTime.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isBn = language == AppLanguage.BANGLA
    val scope = rememberCoroutineScope()

    // Dialog control states
    var showProfileDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showReminderDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

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

    // Color constants precisely tuned to match user's screenshot
    val pageBg = Color(0xFF0B1320)
    val cardContainerBg = Color(0xFF131D2E)
    val cardItemBg = Color(0xFF18253B)
    val cardBorder = Color(0xFF1E2D4A)
    val cyanContainer = Color(0xFF0E384D)
    val cyanAccent = Color(0xFF22D3EE)
    val cyanChevron = Color(0xFF38BDF8)
    val slateSubtext = Color(0xFF94A3B8)
    val darkBlueIconContainer = Color(0xFF142136)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(pageBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("settings_screen_root")
    ) {
        // TOP HEADER: Circular Cyan Settings Icon + Title & Subtitle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 22.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cyan circular icon badge
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(cyanContainer)
                    .border(BorderStroke(1.5.dp, Color(0xFF0284C7).copy(alpha = 0.45f)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = cyanAccent,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 26.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Manage your profile, language and app preferences",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = slateSubtext,
                        fontSize = 13.sp
                    )
                )
            }
        }

        // ==========================================
        // 1. SECTION CARD: Settings (Profile & Language)
        // ==========================================
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = cardContainerBg),
            border = BorderStroke(1.dp, cardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Section Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0F3B52)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = cyanAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Settings",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        )
                        Text(
                            text = "Manage your profile and language preferences",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = slateSubtext,
                                fontSize = 12.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Item 1: Profile (জাহাঙ্গীর আলম • View profile details)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showProfileDialog = true }
                        .testTag("settings_profile_item"),
                    shape = RoundedCornerShape(16.dp),
                    color = cardItemBg
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0F3B52)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = cyanAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Profile",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 15.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "জাহাঙ্গীর আলম • View profile details",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = slateSubtext,
                                    fontSize = 12.5.sp
                                )
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = cyanChevron,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Item 2: Language Change
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showLanguageDialog = true }
                        .testTag("settings_language_item"),
                    shape = RoundedCornerShape(16.dp),
                    color = cardItemBg
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0F3B52)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = null,
                                tint = cyanAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Language Change",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 15.sp
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = cyanContainer,
                                    border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = if (isBn) "🇧🇩 বাংলা" else "🇬🇧 English",
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        ),
                                        color = cyanChevron
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Switch app language between",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = slateSubtext,
                                    fontSize = 12.5.sp
                                )
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = cyanChevron,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // ==========================================
        // 2. SECTION CARD: App Preferences
        // ==========================================
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = cardContainerBg),
            border = BorderStroke(1.dp, cardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "App Preferences",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 17.sp
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Item 1: Theme Mode (Dark / Light)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showThemeDialog = true }
                        .testTag("settings_theme_item"),
                    shape = RoundedCornerShape(16.dp),
                    color = cardItemBg
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(darkBlueIconContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = null,
                                tint = cyanChevron,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Theme Mode (Dark / Light)",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 15.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isDarkMode) "Current: 🌙 Dark Mode" else "Current: ☀️ Light Mode",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = slateSubtext,
                                    fontSize = 12.5.sp
                                )
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = cyanChevron,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Item 2: Daily Workout Reminders (Exact matching from screenshot)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showReminderDialog = true }
                        .testTag("settings_reminder_item"),
                    shape = RoundedCornerShape(16.dp),
                    color = cardItemBg
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(darkBlueIconContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = cyanChevron,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isBn) "নোটিফিকেশন" else "Notifications",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 15.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isBn) "অ্যাপ অ্যালার্ট ও নোটিফিকেশন নিয়ন্ত্রণ" else "Manage app alerts and notifications",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = slateSubtext,
                                    fontSize = 12.5.sp
                                )
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = cyanChevron,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // ==========================================
        // 3. SECTION CARD: About & Owner Information
        // ==========================================
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showAboutDialog = true }
                .testTag("settings_about_item"),
            shape = RoundedCornerShape(20.dp),
            color = cardContainerBg,
            border = BorderStroke(1.dp, cardBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0F3B52)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = cyanAccent,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isBn) "মালিকানা ও অ্যাপের তথ্য" else "About & Owner Information",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isBn) "মো: জাহাঙ্গীর আলম • হিসাব খাতা v2.0.0" else "Md Jahangir Alam • Hisab Khata v2.0.0",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = slateSubtext,
                            fontSize = 12.5.sp
                        )
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = cyanChevron,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }

    // ==========================================
    // MODAL DIALOGS
    // ==========================================

    // 1. Profile Dialog (Full details, Cloud Sync, Backup & Restore)
    if (showProfileDialog) {
        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            containerColor = Color(0xFF131D2E),
            titleContentColor = Color.White,
            textContentColor = Color.White,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(cyanContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = cyanAccent, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Profile Details",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = Color.White)
                        )
                    }
                    IconButton(onClick = { showProfileDialog = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = slateSubtext)
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Owner Name Card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = cardItemBg
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("নাম (Full Name):", style = MaterialTheme.typography.labelSmall, color = slateSubtext)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("মো: জাহাঙ্গীর আলম (Md Jahangir Alam)", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = Color.White))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Mobile Number Card with Dial Action
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = cardItemBg
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("মোবাইল নাম্বার (Mobile):", style = MaterialTheme.typography.labelSmall, color = slateSubtext)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("+8801778600028", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = cyanChevron))
                            }
                            IconButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+8801778600028"))
                                    context.startActivity(intent)
                                }
                            ) {
                                Icon(Icons.Default.Call, contentDescription = "Call", tint = EmeraldPrimary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Email Card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = cardItemBg
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("ইমেইল (Email):", style = MaterialTheme.typography.labelSmall, color = slateSubtext)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("mdjahangiralam2528@gmail.com", style = MaterialTheme.typography.bodyMedium.copy(color = Color.White))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = cardBorder)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Cloud Account & Sync
                    Text("Google Cloud Sync", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = cyanChevron))
                    Spacer(modifier = Modifier.height(8.dp))

                    if (currentUser != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = cardItemBg
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CloudDone, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = currentUser?.email ?: "Connected",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {
                                            viewModel.syncFromCloud { res ->
                                                scope.launch {
                                                    if (res.isSuccess) {
                                                        snackbarHostState.showSnackbar("ক্লাউড থেকে সফলভাবে সিঙ্ক সম্পন্ন হয়েছে")
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
                                        Text("Sync Now", fontSize = 12.sp)
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            viewModel.signOut {
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("Signed out successfully")
                                                }
                                            }
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Sign Out", fontSize = 12.sp, color = CrimsonDanger)
                                    }
                                }
                            }
                        }
                    } else {
                        Button(
                            onClick = {
                                val activity = context as? Activity
                                if (activity != null) {
                                    viewModel.signInWithGoogle(
                                        activity = activity,
                                        onSuccess = {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Google Sign-In successful!")
                                            }
                                        },
                                        onError = { err ->
                                            scope.launch {
                                                snackbarHostState.showSnackbar(err)
                                            }
                                        }
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldDark),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("গুগল দিয়ে সাইন ইন ও ক্লাউড সিঙ্ক")
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = cardBorder)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Backup & Restore Actions
                    Text("Backup & Restore (অফলাইন ব্যাকআপ)", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = cyanChevron))
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.exportBackup(context) { file ->
                                    if (file != null) {
                                        val uri = FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.fileprovider",
                                            file
                                        )
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "application/json"
                                            putExtra(Intent.EXTRA_STREAM, uri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Save Backup JSON"))
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F3B52)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(16.dp), tint = cyanAccent)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Download", fontSize = 12.sp, color = Color.White)
                        }

                        Button(
                            onClick = { restoreFilePicker.launch("application/json") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F3B52)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp), tint = cyanAccent)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Restore", fontSize = 12.sp, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Clear Data
                    OutlinedButton(
                        onClick = {
                            confirmText = ""
                            showClearDataDialog = true
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, tint = CrimsonDanger, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("সকল ডাটা মুছুন (Clear All Data)", color = CrimsonDanger, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showProfileDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0E384D))
                ) {
                    Text("বন্ধ করুন (Close)", color = Color.White)
                }
            }
        )
    }

    // 2. Language Change Dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            containerColor = Color(0xFF131D2E),
            titleContentColor = Color.White,
            textContentColor = Color.White,
            title = {
                Text("Select Language / ভাষা নির্বাচন", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Bangla Option
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.setLanguage(AppLanguage.BANGLA)
                                showLanguageDialog = false
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isBn) Color(0xFF0E384D) else cardItemBg,
                        border = if (isBn) BorderStroke(1.5.dp, cyanAccent) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🇧🇩", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("বাংলা (Bengali)", style = MaterialTheme.typography.bodyLarge.copy(color = Color.White, fontWeight = FontWeight.Bold))
                            }
                            if (isBn) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = cyanAccent)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // English Option
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.setLanguage(AppLanguage.ENGLISH)
                                showLanguageDialog = false
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = if (!isBn) Color(0xFF0E384D) else cardItemBg,
                        border = if (!isBn) BorderStroke(1.5.dp, cyanAccent) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🇬🇧", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("English", style = MaterialTheme.typography.bodyLarge.copy(color = Color.White, fontWeight = FontWeight.Bold))
                            }
                            if (!isBn) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = cyanAccent)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showLanguageDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0E384D))
                ) {
                    Text("Close", color = Color.White)
                }
            }
        )
    }

    // 3. Theme Mode Dialog (Dark / Light)
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            containerColor = Color(0xFF131D2E),
            titleContentColor = Color.White,
            textContentColor = Color.White,
            title = {
                Text("Theme Mode", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Dark Mode Option
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.setDarkMode(true)
                                showThemeDialog = false
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDarkMode) Color(0xFF0E384D) else cardItemBg,
                        border = if (isDarkMode) BorderStroke(1.5.dp, cyanAccent) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DarkMode, contentDescription = null, tint = cyanChevron)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Dark Mode (🌙)", style = MaterialTheme.typography.bodyLarge.copy(color = Color.White, fontWeight = FontWeight.Bold))
                            }
                            if (isDarkMode) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = cyanAccent)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Light Mode Option
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.setDarkMode(false)
                                showThemeDialog = false
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = if (!isDarkMode) Color(0xFF0E384D) else cardItemBg,
                        border = if (!isDarkMode) BorderStroke(1.5.dp, cyanAccent) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LightMode, contentDescription = null, tint = Color(0xFFFBBF24))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Light Mode (☀️)", style = MaterialTheme.typography.bodyLarge.copy(color = Color.White, fontWeight = FontWeight.Bold))
                            }
                            if (!isDarkMode) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = cyanAccent)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showThemeDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0E384D))
                ) {
                    Text("Close", color = Color.White)
                }
            }
        )
    }

    // 4. Daily Workout Reminders Dialog
    if (showReminderDialog) {
        var currentReminderTime by remember { mutableStateOf(reminderTime) }
        var currentEnabled by remember { mutableStateOf(reminderEnabled) }

        AlertDialog(
            onDismissRequest = { showReminderDialog = false },
            containerColor = Color(0xFF131D2E),
            titleContentColor = Color.White,
            textContentColor = Color.White,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = cyanChevron)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isBn) "নোটিফিকেশন ও অ্যালার্ট" else "Notifications & Alerts",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (isBn)
                            "দৈনিক হিসাব এন্ট্রি এবং সিস্টেমের গুরুত্বপূর্ণ নোটিফিকেশন নিয়ন্ত্রণ করুন।"
                        else
                            "Manage daily entry reminders and system alerts.",
                        style = MaterialTheme.typography.bodySmall,
                        color = slateSubtext
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Switch Row
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = cardItemBg
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Enable Notifications", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
                                Text(if (currentEnabled) "Active" else "Disabled", style = MaterialTheme.typography.labelSmall, color = slateSubtext)
                            }
                            Switch(
                                checked = currentEnabled,
                                onCheckedChange = { currentEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = cyanAccent,
                                    checkedTrackColor = cyanContainer
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Select Time:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
                    Spacer(modifier = Modifier.height(8.dp))

                    listOf("07:00 AM", "01:00 PM", "08:00 PM", "09:00 PM", "10:00 PM").forEach { time ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clickable(enabled = currentEnabled) {
                                    currentReminderTime = time
                                },
                            shape = RoundedCornerShape(10.dp),
                            color = if (currentReminderTime == time && currentEnabled) Color(0xFF0E384D) else cardItemBg
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(time, style = MaterialTheme.typography.bodyMedium.copy(color = if (currentEnabled) Color.White else slateSubtext))
                                if (currentReminderTime == time && currentEnabled) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = cyanAccent, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setReminderEnabled(currentEnabled)
                        viewModel.setReminderTime(currentReminderTime)
                        showReminderDialog = false
                        scope.launch {
                            snackbarHostState.showSnackbar("Reminder updated: $currentReminderTime")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0E384D))
                ) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showReminderDialog = false }) {
                    Text("Cancel", color = slateSubtext)
                }
            }
        )
    }

    // 5. About & Owner Information Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            containerColor = Color(0xFF131D2E),
            titleContentColor = Color.White,
            textContentColor = Color.White,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = cyanAccent)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("About & Owner Information", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = cardItemBg
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("যাচাইকৃত স্বত্বাধিকারী ও নির্মাতা (Verified Owner)", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = EmeraldPrimary))
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            Text("মালিক ও নির্মাতা: মো: জাহাঙ্গীর আলম", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
                            Text("Owner Name: Md Jahangir Alam", style = MaterialTheme.typography.bodySmall, color = slateSubtext)

                            Spacer(modifier = Modifier.height(10.dp))
                            Text("মোবাইল নাম্বার (Mobile): +8801778600028", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = cyanChevron))
                            Text("ইমেইল (Email): mdjahangiralam2528@gmail.com", style = MaterialTheme.typography.bodySmall, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = cardItemBg
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("অ্যাপের নাম: হিসাব খাতা (Hisab Khata)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
                            Text("সংস্করণ (Version): 2.0.0", style = MaterialTheme.typography.bodySmall, color = slateSubtext)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("© 2026 Md Jahangir Alam. সর্বস্বত্ব সংরক্ষিত।", style = MaterialTheme.typography.labelSmall, color = slateSubtext)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+8801778600028"))
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldDark),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("কল করুন", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:mdjahangiralam2528@gmail.com"))
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F3B52)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp), tint = cyanAccent)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ইমেইল", fontSize = 12.sp, color = Color.White)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAboutDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0E384D))
                ) {
                    Text("Close", color = Color.White)
                }
            }
        )
    }

    // Confirmation Dialog for Backup Restore
    if (showRestoreConfirmDialog) {
        AlertDialog(
            onDismissRequest = {
                showRestoreConfirmDialog = false
                pendingRestoreJson = null
            },
            containerColor = Color(0xFF131D2E),
            titleContentColor = Color.White,
            textContentColor = Color.White,
            icon = { Icon(Icons.Default.Restore, contentDescription = null, tint = EmeraldPrimary) },
            title = {
                Text(
                    text = "Confirm Backup Restore",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "Do you want to restore records from this backup? Existing records will be replaced.",
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
                    Text("Yes, Restore")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    showRestoreConfirmDialog = false
                    pendingRestoreJson = null
                }) {
                    Text("Cancel", color = slateSubtext)
                }
            }
        )
    }

    // Confirmation Dialog for Clear All Data
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            containerColor = Color(0xFF131D2E),
            titleContentColor = CrimsonDanger,
            textContentColor = Color.White,
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = CrimsonDanger) },
            title = {
                Text(
                    text = "Permanently Delete All Data?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = CrimsonDanger
                )
            },
            text = {
                Column {
                    Text(
                        text = "This action cannot be undone. Type 'DELETE' below to confirm:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = confirmText,
                        onValueChange = { confirmText = it },
                        label = { Text("DELETE") },
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
                                    snackbarHostState.showSnackbar("All data successfully deleted")
                                }
                            }
                            showClearDataDialog = false
                        }
                    },
                    enabled = isMatched,
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonDanger)
                ) {
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showClearDataDialog = false }) {
                    Text("Cancel", color = slateSubtext)
                }
            }
        )
    }
}
