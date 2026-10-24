package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.util.AppLanguage
import com.example.ui.viewmodel.FinancialViewModel
import kotlinx.coroutines.launch

enum class SettingsSubScreen {
    MAIN,
    PROFILE,
    HELP_AND_SUPPORT,
    PRIVACY_POLICY
}

@Composable
fun SettingsScreen(
    viewModel: FinancialViewModel,
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val language by viewModel.language.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val reminderEnabled by viewModel.reminderEnabled.collectAsStateWithLifecycle()
    val reminderTime by viewModel.reminderTime.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val lastCloudBackupTime by viewModel.lastCloudBackupTime.collectAsStateWithLifecycle()
    val expensesList by viewModel.expenses.collectAsStateWithLifecycle()
    val outstandingList by viewModel.outstanding.collectAsStateWithLifecycle()
    val paymentsList by viewModel.payments.collectAsStateWithLifecycle()
    val isBn = language == AppLanguage.BANGLA
    val scope = rememberCoroutineScope()

    var currentSubScreen by remember { mutableStateOf(SettingsSubScreen.MAIN) }

    // Dialog & loading states for cloud and options
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showNotificationDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showRestoreConfirmDialog by remember { mutableStateOf(false) }
    var isBackingUp by remember { mutableStateOf(false) }
    var isRestoring by remember { mutableStateOf(false) }
    var isSigningIn by remember { mutableStateOf(false) }

    // Color definitions that dynamically adapt with perfect contrast in BOTH Dark and Light Mode
    val pageBg = if (isDarkMode) Color(0xFF0B1320) else Color(0xFFF8FAFC)
    val cardContainerBg = if (isDarkMode) Color(0xFF131D2E) else Color(0xFFFFFFFF)
    val cardItemBg = if (isDarkMode) Color(0xFF18253B) else Color(0xFFF1F5F9)
    val cardBorder = if (isDarkMode) Color(0xFF1E2D4A) else Color(0xFFE2E8F0)
    val titleColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val subtextColor = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF475569)
    val cyanContainer = if (isDarkMode) Color(0xFF0E384D) else Color(0xFFE0F2FE)
    val cyanAccent = if (isDarkMode) Color(0xFF22D3EE) else Color(0xFF0284C7)
    val cyanChevron = if (isDarkMode) Color(0xFF38BDF8) else Color(0xFF0284C7)
    val darkBlueIconContainer = if (isDarkMode) Color(0xFF142136) else Color(0xFFE2E8F0)

    // Handle system back button when inside sub-screens
    if (currentSubScreen != SettingsSubScreen.MAIN) {
        BackHandler {
            currentSubScreen = SettingsSubScreen.MAIN
        }
    }

    when (currentSubScreen) {
        // ==========================================
        // SUB-SCREEN 1: PROFILE DEDICATED PAGE
        // ==========================================
        SettingsSubScreen.PROFILE -> {
            var profileName by remember(userProfile) { mutableStateOf(userProfile.name) }
            var profilePhone by remember(userProfile) { mutableStateOf(userProfile.phone) }
            var profileEmail by remember(userProfile) { mutableStateOf(userProfile.email) }
            var profileGender by remember(userProfile) { mutableStateOf(userProfile.gender) }
            var nameError by remember { mutableStateOf<String?>(null) }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(pageBg)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
                    .testTag("profile_page_root")
            ) {
                // Top App Bar with Back Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { currentSubScreen = SettingsSubScreen.MAIN },
                        modifier = Modifier
                            .size(42.dp)
                            .background(cardItemBg, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = titleColor
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = if (isBn) "প্রোফাইল তথ্য" else "Profile Information",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = titleColor,
                                fontSize = 22.sp
                            )
                        )
                        Text(
                            text = if (isBn) "রিপোর্ট ও স্বাক্ষরের জন্য আপনার তথ্য" else "Personal profile for reports and signature",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = subtextColor,
                                fontSize = 12.sp
                            )
                        )
                    }
                }

                // Profile Avatar Hero
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(86.dp)
                            .clip(CircleShape)
                            .background(cyanContainer)
                            .border(BorderStroke(2.dp, cyanAccent), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (profileGender == "মেয়ে" || profileGender == "Female") Icons.Default.Face else Icons.Default.Person,
                            contentDescription = null,
                            tint = cyanAccent,
                            modifier = Modifier.size(46.dp)
                        )
                    }
                }

                // Profile Form Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = cardContainerBg),
                    border = BorderStroke(1.dp, cardBorder)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        // 1. Full Name
                        Text(
                            text = if (isBn) "পূর্ণ নাম (Full Name):" else "Full Name:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = titleColor
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = profileName,
                            onValueChange = {
                                profileName = it
                                nameError = null
                            },
                            placeholder = { Text("মো: জাহাঙ্গীর আলম", color = subtextColor) },
                            isError = nameError != null,
                            supportingText = nameError?.let { { Text(it, color = Color(0xFFDC2626)) } },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = titleColor,
                                unfocusedTextColor = titleColor,
                                focusedBorderColor = cyanAccent,
                                unfocusedBorderColor = cardBorder,
                                focusedContainerColor = cardItemBg,
                                unfocusedContainerColor = cardItemBg
                            ),
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = cyanChevron)
                            }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // 2. Mobile Number
                        Text(
                            text = if (isBn) "মোবাইল নাম্বার (Mobile Number):" else "Mobile Number:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = titleColor
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = profilePhone,
                            onValueChange = { profilePhone = it },
                            placeholder = { Text("+8801778600028", color = subtextColor) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = titleColor,
                                unfocusedTextColor = titleColor,
                                focusedBorderColor = cyanAccent,
                                unfocusedBorderColor = cardBorder,
                                focusedContainerColor = cardItemBg,
                                unfocusedContainerColor = cardItemBg
                            ),
                            leadingIcon = {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = cyanChevron)
                            }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // 3. Email Address
                        Text(
                            text = if (isBn) "ইমেইল এড্রেস (Email Address):" else "Email Address:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = titleColor
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = profileEmail,
                            onValueChange = { profileEmail = it },
                            placeholder = { Text("mdjahangiralam2528@gmail.com", color = subtextColor) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = titleColor,
                                unfocusedTextColor = titleColor,
                                focusedBorderColor = cyanAccent,
                                unfocusedBorderColor = cardBorder,
                                focusedContainerColor = cardItemBg,
                                unfocusedContainerColor = cardItemBg
                            ),
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null, tint = cyanChevron)
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 4. Gender (ছেলে না মেয়ে / Male or Female)
                        Text(
                            text = if (isBn) "লিঙ্গ নির্বাচন (Gender):" else "Gender:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = titleColor
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val isMale = profileGender == "ছেলে" || profileGender == "Male"
                            val isFemale = profileGender == "মেয়ে" || profileGender == "Female"

                            // Male Option (ছেলে)
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { profileGender = "ছেলে" },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isMale) cyanContainer else cardItemBg,
                                border = if (isMale) BorderStroke(1.5.dp, cyanAccent) else BorderStroke(1.dp, cardBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = if (isMale) cyanAccent else subtextColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isBn) "ছেলে" else "Male",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isMale) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isMale) titleColor else subtextColor
                                        )
                                    )
                                    if (isMale) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(Icons.Default.Check, contentDescription = null, tint = cyanAccent, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            // Female Option (মেয়ে)
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { profileGender = "মেয়ে" },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isFemale) cyanContainer else cardItemBg,
                                border = if (isFemale) BorderStroke(1.5.dp, cyanAccent) else BorderStroke(1.dp, cardBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Face,
                                        contentDescription = null,
                                        tint = if (isFemale) cyanAccent else subtextColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isBn) "মেয়ে" else "Female",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isFemale) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isFemale) titleColor else subtextColor
                                        )
                                    )
                                    if (isFemale) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(Icons.Default.Check, contentDescription = null, tint = cyanAccent, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(22.dp))

                        // 5. Save Button
                        Button(
                            onClick = {
                                if (profileName.isBlank()) {
                                    nameError = if (isBn) "দয়া করে আপনার নাম লিখুন" else "Please enter your name"
                                    return@Button
                                }
                                viewModel.updateProfile(
                                    name = profileName.trim(),
                                    phone = profilePhone.trim(),
                                    email = profileEmail.trim(),
                                    gender = profileGender
                                )
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        if (isBn) "প্রোফাইল সফলভাবে সংরক্ষিত হয়েছে!" else "Profile saved successfully!"
                                    )
                                }
                                currentSubScreen = SettingsSubScreen.MAIN
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("save_profile_button")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isBn) "প্রোফাইল সেভ করুন" else "Save Profile",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (isBn)
                                "সংরক্ষিত নাম রিপোর্ট ও সার্টিফিকেটে প্রস্তুতকারকের স্বাক্ষর হিসেবে প্রদর্শিত হবে।"
                            else
                                "Saved name will appear as the author's signature on all financial reports and statements.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = subtextColor,
                                fontSize = 11.5.sp
                            ),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }
        }

        // ==========================================
        // SUB-SCREEN 2: HELP & SUPPORT PAGE
        // ==========================================
        SettingsSubScreen.HELP_AND_SUPPORT -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(pageBg)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Header with Back Arrow
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { currentSubScreen = SettingsSubScreen.MAIN },
                        modifier = Modifier
                            .size(42.dp)
                            .background(cardItemBg, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = titleColor
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = if (isBn) "Help & Support (সহায়তা)" else "Help & Support",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = titleColor,
                                fontSize = 22.sp
                            )
                        )
                        Text(
                            text = if (isBn) "ডেভেলপার ও কাস্টমার সার্ভিস যোগাযোগ" else "Developer contact and customer service",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = subtextColor,
                                fontSize = 12.sp
                            )
                        )
                    }
                }

                // Help & Support Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = cardContainerBg),
                    border = BorderStroke(1.dp, cardBorder)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(cyanContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.HelpOutline, contentDescription = null, tint = cyanAccent)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isBn) "২৪/৭ সরাসরি সহায়তা" else "24/7 Dedicated Support",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = titleColor)
                                )
                                Text(
                                    text = if (isBn) "অ্যাপ সম্পর্কিত যেকোনো প্রয়োজনে যোগাযোগ করুন" else "Reach out for any assistance with Hisab Khata",
                                    style = MaterialTheme.typography.bodySmall.copy(color = subtextColor)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = cardBorder)
                        Spacer(modifier = Modifier.height(16.dp))

                        // Developer Info Block
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = cardItemBg
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isBn) "অ্যাপের মূল নির্মাতা ও স্বত্বাধিকারী" else "Official Developer & Owner",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "মো: জাহাঙ্গীর আলম (Md Jahangir Alam)",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = titleColor)
                                )
                                Text(
                                    text = "Lead Developer & Financial Software Architect",
                                    style = MaterialTheme.typography.bodySmall.copy(color = subtextColor)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Call Action Row
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = cardItemBg
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(EmeraldContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Phone, contentDescription = null, tint = EmeraldDark, modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(if (isBn) "হেল্পলাইন মোবাইল:" else "Helpline Mobile:", style = MaterialTheme.typography.labelSmall, color = subtextColor)
                                        Text("+8801778600028", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = titleColor))
                                    }
                                }

                                Button(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+8801778600028"))
                                        context.startActivity(intent)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldDark),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isBn) "কল করুন" else "Call", fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Email Action Row
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = cardItemBg
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
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
                                        Icon(Icons.Default.Email, contentDescription = null, tint = cyanAccent, modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(if (isBn) "অফিসিয়াল ইমেইল:" else "Official Email:", style = MaterialTheme.typography.labelSmall, color = subtextColor)
                                        Text("mdjahangiralam2528@gmail.com", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium, color = titleColor))
                                    }
                                }

                                Button(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:mdjahangiralam2528@gmail.com"))
                                        context.startActivity(intent)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F3B52)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(if (isBn) "মেইল" else "Mail", fontSize = 12.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // SUB-SCREEN 3: PRIVACY POLICY PAGE
        // ==========================================
        SettingsSubScreen.PRIVACY_POLICY -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(pageBg)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Header with Back Arrow
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { currentSubScreen = SettingsSubScreen.MAIN },
                        modifier = Modifier
                            .size(42.dp)
                            .background(cardItemBg, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = titleColor
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = if (isBn) "প্রাইভেসি পলিসি" else "Privacy Policy",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = titleColor,
                                fontSize = 22.sp
                            )
                        )
                        Text(
                            text = if (isBn) "গুগল প্লে স্টোর পলিসি ও ডাটা নিরাপত্তা" else "Google Play Store Data Safety & Privacy Terms",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = subtextColor,
                                fontSize = 12.sp
                            )
                        )
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = cardContainerBg),
                    border = BorderStroke(1.dp, cardBorder)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(cyanContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = cyanAccent, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (isBn) "হিসাব খাতা প্রাইভেসি নীতিমালা" else "Hisab Khata Privacy Policy",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = titleColor)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = cardBorder)
                        Spacer(modifier = Modifier.height(14.dp))

                        val sections = listOf(
                            ("১. ডাটা নিরাপত্তা ও সংরক্ষণ" to "হিসাব খাতা অ্যাপ আপনার বাজার খরচ ও আর্থিক লেনদেনের সমস্ত তথ্য আপনার নিজস্ব ডিভাইসে এনক্রিপ্টেড ডাটাবেজে সংরক্ষণ করে। ক্লাউড ব্যাকআপের ক্ষেত্রে গুগলের নিরাপদ ফায়ারবেস ক্লাউড প্রটোকল ব্যবহার করা হয়।"),
                            ("২. কোনো তৃতীয় পক্ষের সাথে ডাটা আদান-প্রদান নয়" to "আমরা ব্যবহারকারীর কোনো ব্যক্তিগত বা আর্থিক তথ্য কোনো প্রকার তৃতীয় পক্ষ বা বিজ্ঞাপনদাতার কাছে শেয়ার, বিক্রি বা হস্তান্তর করি না।"),
                            ("৩. আপনার ডাটার ওপর সম্পূর্ণ নিয়ন্ত্রণ" to "আপনি যেকোনো সময় সেটিংস থেকে আপনার সম্পূর্ণ ডাটার ব্যাকআপ JSON আকারে ডাউনলোড করতে পারবেন এবং প্রয়োজনে এক ক্লিকেই সমস্ত ডাটা স্থায়ীভাবে মুছে ফেলতে পারবেন।"),
                            ("৪. নীতিমালার আপডেট ও যোগাযোগ" to "প্রাইভেসি সংক্রান্ত যেকোনো জিজ্ঞাসা বা প্রশ্নের জন্য আমাদের ডেভেলপার মো: জাহাঙ্গীর আলম (mdjahangiralam2528@gmail.com / +8801778600028)-এর সাথে সরাসরি যোগাযোগ করতে পারেন।")
                        )

                        for ((title, desc) in sections) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = cyanChevron)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodyMedium.copy(color = subtextColor, lineHeight = 20.sp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = cardItemBg,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "সর্বশেষ আপডেট: অক্টোবর ২০২৬ • সর্বস্বত্ব সংরক্ষিত",
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.labelSmall.copy(color = subtextColor)
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // SUB-SCREEN: MAIN SETTINGS VIEW
        // ==========================================
        SettingsSubScreen.MAIN -> {
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
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(cyanContainer)
                            .border(BorderStroke(1.5.dp, cyanAccent.copy(alpha = 0.45f)), CircleShape),
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
                            text = if (isBn) "সেটিংস" else "Settings",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = titleColor,
                                fontSize = 26.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isBn) "প্রোফাইল, ভাষা ও সিস্টেম পছন্দসমূহ পরিচালনা করুন" else "Manage your profile, language and app preferences",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = subtextColor,
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(cyanContainer),
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
                                    text = if (isBn) "সেটিংস" else "Settings",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = titleColor,
                                        fontSize = 16.sp
                                    )
                                )
                                Text(
                                    text = if (isBn) "প্রোফাইল ও ভাষা পছন্দসমূহ পরিচালনা করুন" else "Manage your profile and language preferences",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = subtextColor,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Item 1: Profile (Navigates to dedicated Profile Page!)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { currentSubScreen = SettingsSubScreen.PROFILE }
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
                                    .background(cyanContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (userProfile.gender == "মেয়ে") Icons.Default.Face else Icons.Default.Person,
                                        contentDescription = null,
                                        tint = cyanAccent,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isBn) "প্রোফাইল" else "Profile",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = titleColor,
                                            fontSize = 15.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${userProfile.name} • ${if (isBn) "বিস্তারিত দেখুন" else "View profile details"}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = subtextColor,
                                            fontSize = 12.5.sp
                                        ),
                                        maxLines = 1
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
                                        .background(cyanContainer),
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
                                            text = if (isBn) "ভাষা পরিবর্তন" else "Language Change",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = titleColor,
                                                fontSize = 15.sp
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = cyanContainer,
                                            border = BorderStroke(1.dp, cyanAccent.copy(alpha = 0.5f))
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
                                        text = if (isBn) "বাংলা ও ইংরেজির মাঝে পরিবর্তন" else "Switch app language between",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = subtextColor,
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
                // CLOUD BACKUP & RESTORE CARD (FIRESTORE)
                // ==========================================
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cloud_backup_restore_card"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = cardContainerBg),
                    border = BorderStroke(1.dp, cardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(cyanContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = null,
                                    tint = cyanAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isBn) "ক্লাউড ব্যাকআপ ও রিস্টোর" else "Cloud Backup & Restore",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = titleColor,
                                        fontSize = 16.sp
                                    )
                                )
                                Text(
                                    text = if (isBn) "নতুন ফোনে ডাটা ফিরিয়ে আনতে গুগল ক্লাউড ব্যাকআপ" else "Google Firestore sync to restore data on new devices",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = subtextColor,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (currentUser == null) {
                            // User is NOT signed in
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                color = cardItemBg,
                                border = BorderStroke(1.dp, cardBorder)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.CloudUpload,
                                            contentDescription = null,
                                            tint = cyanAccent,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (isBn) "গুগল ক্লাউড সিঙ্ক সক্রিয় করুন" else "Enable Google Cloud Sync",
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = titleColor
                                            )
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = if (isBn)
                                            "আপনার বাজার খরচ, বাকি হিসাব ও জমার সমস্ত রেকর্ড যাতে নতুন ডিভাইসে বা ফোন পরিবর্তন করলেও স্বয়ংক্রিয়ভাবে ফিরে পান, সেজন্য আপনার গুগল একাউন্টে সাইন ইন করুন।"
                                        else
                                            "Sign in with your Google account to back up and restore your grocery expenses and outstanding balances seamlessly across devices.",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = subtextColor,
                                            lineHeight = 18.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = {
                                            if (activity != null) {
                                                isSigningIn = true
                                                viewModel.signInWithGoogle(
                                                    activity = activity,
                                                    onSuccess = { user ->
                                                        isSigningIn = false
                                                        scope.launch {
                                                            snackbarHostState.showSnackbar(
                                                                if (isBn) "গুগল একাউন্টে সফলভাবে সংযুক্ত হয়েছে!" else "Connected to Google account!"
                                                            )
                                                        }
                                                    },
                                                    onError = { err ->
                                                        isSigningIn = false
                                                        scope.launch {
                                                            snackbarHostState.showSnackbar(
                                                                if (isBn) "সাইন ইন ব্যর্থ: $err" else "Sign-in failed: $err"
                                                            )
                                                        }
                                                    },
                                                    onCancelled = {
                                                        isSigningIn = false
                                                    }
                                                )
                                            } else {
                                                scope.launch {
                                                    snackbarHostState.showSnackbar(
                                                        if (isBn) "অ্যাক্টিভিটি উপলব্ধ নয়" else "Activity not available"
                                                    )
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(46.dp)
                                            .testTag("google_signin_button"),
                                        enabled = !isSigningIn
                                    ) {
                                        if (isSigningIn) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(18.dp),
                                                color = Color.White,
                                                strokeWidth = 2.dp
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                        } else {
                                            Icon(
                                                Icons.Default.AccountCircle,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                        }
                                        Text(
                                            text = if (isBn) "গুগল সাইন ইন ও ক্লাউড সিঙ্ক" else "Sign In with Google",
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        )
                                    }
                                }
                            }
                        } else {
                            // User IS signed in
                            // 1. Account info banner
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = cardItemBg,
                                border = BorderStroke(1.dp, cardBorder)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(EmeraldContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.AccountCircle,
                                            contentDescription = null,
                                            tint = EmeraldDark,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = currentUser?.displayName ?: (if (isBn) "গুগল ব্যবহারকারী" else "Google User"),
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = titleColor
                                                ),
                                                maxLines = 1
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = EmeraldPrimary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                        Text(
                                            text = currentUser?.email ?: "",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = subtextColor,
                                                fontSize = 11.5.sp
                                            ),
                                            maxLines = 1
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            viewModel.signOut {
                                                scope.launch {
                                                    snackbarHostState.showSnackbar(
                                                        if (isBn) "গুগল একাউন্ট থেকে সাইন আউট সম্পন্ন" else "Signed out of Google account"
                                                    )
                                                }
                                            }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Logout,
                                            contentDescription = "Sign Out",
                                            tint = subtextColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // 2. Status & Statistics Box
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = cardItemBg,
                                border = BorderStroke(1.dp, cardBorder)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (lastCloudBackupTime != null) Icons.Default.CloudDone else Icons.Default.Warning,
                                                contentDescription = null,
                                                tint = if (lastCloudBackupTime != null) EmeraldPrimary else Color(0xFFF59E0B),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (isBn) "সর্বশেষ ক্লাউড ব্যাকআপ:" else "Last Cloud Backup:",
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = titleColor
                                                )
                                            )
                                        }

                                        Text(
                                            text = lastCloudBackupTime ?: (if (isBn) "নেওয়া হয়নি" else "Not yet"),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = if (lastCloudBackupTime != null) cyanChevron else Color(0xFFF59E0B),
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    HorizontalDivider(color = cardBorder)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Data Stats
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceAround
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "${expensesList.size}",
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = cyanAccent
                                                )
                                            )
                                            Text(
                                                text = if (isBn) "বাজার খরচ" else "Expenses",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = subtextColor,
                                                    fontSize = 11.sp
                                                )
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "${outstandingList.size}",
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFF59E0B)
                                                )
                                            )
                                            Text(
                                                text = if (isBn) "বাকি হিসাব" else "Outstanding",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = subtextColor,
                                                    fontSize = 11.sp
                                                )
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "${paymentsList.size}",
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = EmeraldPrimary
                                                )
                                            )
                                            Text(
                                                text = if (isBn) "পরিশোধ" else "Payments",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = subtextColor,
                                                    fontSize = 11.sp
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // 3. Backup and Restore Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Backup Now Button
                                Button(
                                    onClick = {
                                        isBackingUp = true
                                        viewModel.backupAllToCloud { result ->
                                            isBackingUp = false
                                            result.onSuccess { timeStr ->
                                                scope.launch {
                                                    snackbarHostState.showSnackbar(
                                                        if (isBn) "ক্লাউডে সফলভাবে ব্যাকআপ সম্পন্ন হয়েছে! ($timeStr)"
                                                        else "Successfully backed up to Google Cloud! ($timeStr)"
                                                    )
                                                }
                                            }.onFailure { ex ->
                                                scope.launch {
                                                    snackbarHostState.showSnackbar(
                                                        if (isBn) "ব্যাকআপ ব্যর্থ: ${ex.message}"
                                                        else "Backup failed: ${ex.message}"
                                                    )
                                                }
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("backup_now_button"),
                                    enabled = !isBackingUp && !isRestoring
                                ) {
                                    if (isBackingUp) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    } else {
                                        Icon(
                                            Icons.Default.CloudUpload,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text(
                                        text = if (isBn) "ব্যাকআপ নিন" else "Backup Now",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    )
                                }

                                // Restore from Cloud Button
                                Button(
                                    onClick = { showRestoreConfirmDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = cyanAccent),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("restore_from_cloud_button"),
                                    enabled = !isBackingUp && !isRestoring
                                ) {
                                    if (isRestoring) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = Color(0xFF0F172A),
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    } else {
                                        Icon(
                                            Icons.Default.CloudDownload,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = Color(0xFF0F172A)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text(
                                        text = if (isBn) "রিস্টোর করুন" else "Restore Data",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A)
                                        )
                                    )
                                }
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
                            text = if (isBn) "অ্যাপ পছন্দসমূহ" else "App Preferences",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = titleColor,
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
                                        text = if (isBn) "থিম মোড (ডার্ক / লাইট)" else "Theme Mode (Dark / Light)",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = titleColor,
                                            fontSize = 15.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (isDarkMode) (if (isBn) "বর্তমান: 🌙 ডার্ক মোড" else "Current: 🌙 Dark Mode") else (if (isBn) "বর্তমান: ☀️ লাইট মোড" else "Current: ☀️ Light Mode"),
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = subtextColor,
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

                        // Item 2: Notifications
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showNotificationDialog = true }
                                .testTag("settings_notification_item"),
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
                                            color = titleColor,
                                            fontSize = 15.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (isBn) "অ্যাপ অ্যালার্ট ও নোটিফিকেশন নিয়ন্ত্রণ" else "Manage app alerts and notifications",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = subtextColor,
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
                // 3. SECTION CARD: Help, Privacy & About
                // ==========================================
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = cardContainerBg),
                    border = BorderStroke(1.dp, cardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (isBn) "সহায়তা ও আইনি তথ্য" else "Support & Information",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = titleColor,
                                fontSize = 17.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Help & Support Item
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { currentSubScreen = SettingsSubScreen.HELP_AND_SUPPORT }
                                .testTag("settings_help_item"),
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
                                        .background(cyanContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.HelpOutline,
                                        contentDescription = null,
                                        tint = cyanAccent,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isBn) "Help & Support (সহায়তা)" else "Help & Support",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = titleColor,
                                            fontSize = 15.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (isBn) "ডেভেলপার যোগাযোগ ও সহায়তা সেবা" else "Developer contacts & assistance",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = subtextColor,
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

                        // Privacy Policy Item
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { currentSubScreen = SettingsSubScreen.PRIVACY_POLICY }
                                .testTag("settings_privacy_item"),
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
                                        .background(cyanContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Policy,
                                        contentDescription = null,
                                        tint = cyanAccent,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isBn) "প্রাইভেসি পলিসি" else "Privacy Policy",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = titleColor,
                                            fontSize = 15.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (isBn) "প্লে স্টোর ডাটা নিরাপত্তা ও নীতিমালা" else "Play Store data safety & privacy terms",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = subtextColor,
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

                        // About & Owner Information Item
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showAboutDialog = true }
                                .testTag("settings_about_item"),
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
                                        .background(cyanContainer),
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
                                            color = titleColor,
                                            fontSize = 15.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${userProfile.name} • ${if (isBn) "হিসাব খাতা v2.0.0" else "Hisab Khata v2.0.0"}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = subtextColor,
                                            fontSize = 12.5.sp
                                        ),
                                        maxLines = 1
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

                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }

    // ==========================================
    // MODAL DIALOGS
    // ==========================================

    // Language Change Dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            containerColor = cardContainerBg,
            titleContentColor = titleColor,
            textContentColor = titleColor,
            title = {
                Text(
                    text = if (isBn) "ভাষা নির্বাচন করুন" else "Select Language",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = titleColor)
                )
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
                        color = if (isBn) cyanContainer else cardItemBg,
                        border = if (isBn) BorderStroke(1.5.dp, cyanAccent) else BorderStroke(1.dp, cardBorder)
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
                                Text("বাংলা (Bengali)", style = MaterialTheme.typography.bodyLarge.copy(color = titleColor, fontWeight = FontWeight.Bold))
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
                        color = if (!isBn) cyanContainer else cardItemBg,
                        border = if (!isBn) BorderStroke(1.5.dp, cyanAccent) else BorderStroke(1.dp, cardBorder)
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
                                Text("English", style = MaterialTheme.typography.bodyLarge.copy(color = titleColor, fontWeight = FontWeight.Bold))
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
                    colors = ButtonDefaults.buttonColors(containerColor = cyanContainer)
                ) {
                    Text(if (isBn) "বন্ধ করুন" else "Close", color = cyanAccent)
                }
            }
        )
    }

    // Theme Mode Dialog (Dark / Light)
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            containerColor = cardContainerBg,
            titleContentColor = titleColor,
            textContentColor = titleColor,
            title = {
                Text(
                    text = if (isBn) "থিম মোড নির্বাচন" else "Select Theme Mode",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = titleColor)
                )
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
                        color = if (isDarkMode) cyanContainer else cardItemBg,
                        border = if (isDarkMode) BorderStroke(1.5.dp, cyanAccent) else BorderStroke(1.dp, cardBorder)
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
                                Text(if (isBn) "ডার্ক মোড (🌙)" else "Dark Mode (🌙)", style = MaterialTheme.typography.bodyLarge.copy(color = titleColor, fontWeight = FontWeight.Bold))
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
                        color = if (!isDarkMode) cyanContainer else cardItemBg,
                        border = if (!isDarkMode) BorderStroke(1.5.dp, cyanAccent) else BorderStroke(1.dp, cardBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LightMode, contentDescription = null, tint = Color(0xFFF59E0B))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(if (isBn) "লাইট মোড (☀️)" else "Light Mode (☀️)", style = MaterialTheme.typography.bodyLarge.copy(color = titleColor, fontWeight = FontWeight.Bold))
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
                    colors = ButtonDefaults.buttonColors(containerColor = cyanContainer)
                ) {
                    Text(if (isBn) "বন্ধ করুন" else "Close", color = cyanAccent)
                }
            }
        )
    }

    // Notifications Dialog
    if (showNotificationDialog) {
        var currentReminderTime by remember { mutableStateOf(reminderTime) }
        var currentEnabled by remember { mutableStateOf(reminderEnabled) }

        AlertDialog(
            onDismissRequest = { showNotificationDialog = false },
            containerColor = cardContainerBg,
            titleContentColor = titleColor,
            textContentColor = titleColor,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = cyanChevron)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isBn) "নোটিফিকেশন ও অ্যালার্ট" else "Notifications & Alerts",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = titleColor)
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (isBn)
                            "দৈনিক বাজার খরচ ও বাকি হিসাব এন্ট্রির নোটিফিকেশন সময়সূচি নিয়ন্ত্রণ করুন।"
                        else
                            "Manage daily entry reminder schedule and app alerts.",
                        style = MaterialTheme.typography.bodySmall,
                        color = subtextColor
                    )
                    Spacer(modifier = Modifier.height(14.dp))

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
                                Text(if (isBn) "নোটিফিকেশন সক্রিয় রাখুন" else "Enable Notifications", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = titleColor))
                                Text(if (currentEnabled) (if (isBn) "চালু আছে" else "Enabled") else (if (isBn) "বন্ধ আছে" else "Disabled"), style = MaterialTheme.typography.labelSmall, color = subtextColor)
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
                    Text(if (isBn) "অ্যালার্ট সময় নির্বাচন:" else "Select Alert Time:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = titleColor))
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
                            color = if (currentReminderTime == time && currentEnabled) cyanContainer else cardItemBg
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(time, style = MaterialTheme.typography.bodyMedium.copy(color = if (currentEnabled) titleColor else subtextColor))
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
                        showNotificationDialog = false
                        scope.launch {
                            snackbarHostState.showSnackbar(if (isBn) "নোটিফিকেশন আপডেট সম্পন্ন: $currentReminderTime" else "Notifications updated: $currentReminderTime")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text(if (isBn) "সেভ করুন" else "Save", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showNotificationDialog = false }) {
                    Text(if (isBn) "বাতিল" else "Cancel", color = subtextColor)
                }
            }
        )
    }

    // About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            containerColor = cardContainerBg,
            titleContentColor = titleColor,
            textContentColor = titleColor,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = cyanAccent)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isBn) "মালিকানা ও অ্যাপ পরিচিতি" else "About & Owner Information",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = titleColor)
                    )
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
                                Text(if (isBn) "যাচাইকৃত স্বত্বাধিকারী ও নির্মাতা" else "Verified Owner & Developer", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = EmeraldPrimary))
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            Text("মো: জাহাঙ্গীর আলম (Md Jahangir Alam)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = titleColor))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("মোবাইল: +8801778600028", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = cyanChevron))
                            Text("ইমেইল: mdjahangiralam2528@gmail.com", style = MaterialTheme.typography.bodySmall, color = subtextColor)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = cardItemBg
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(if (isBn) "অ্যাপের নাম: হিসাব খাতা (Hisab Khata)" else "App: Hisab Khata", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = titleColor))
                            Text("সংস্করণ: 2.0.0 (Google Play Store Ready)", style = MaterialTheme.typography.bodySmall, color = subtextColor)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("© 2026 Md Jahangir Alam. সর্বস্বত্ব সংরক্ষিত।", style = MaterialTheme.typography.labelSmall, color = subtextColor)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAboutDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = cyanContainer)
                ) {
                    Text(if (isBn) "বন্ধ করুন" else "Close", color = cyanAccent)
                }
            }
        )
    }

    // Cloud Restore Confirmation Dialog
    if (showRestoreConfirmDialog) {
        AlertDialog(
            onDismissRequest = { if (!isRestoring) showRestoreConfirmDialog = false },
            containerColor = cardContainerBg,
            titleContentColor = titleColor,
            textContentColor = titleColor,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudDownload, contentDescription = null, tint = cyanAccent)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isBn) "ক্লাউড থেকে ডাটা রিস্টোর করবেন?" else "Restore Cloud Data?",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = titleColor)
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = if (isBn)
                            "গুগল ক্লাউড ফায়ারবেসে সংরক্ষিত আপনার সকল বাজার খরচ, বাকি ও জমার হিসাব এই ডিভাইসে নামিয়ে আনা (রিস্টোর) হবে। এতে কোনো বিদ্যমান ডাটা মুছে যাবে না, বরং ক্লাউডের সব রেকর্ড ডিভাইসে আপডেট ও সিঙ্ক হবে।"
                        else
                            "All grocery expenses, outstanding balances, and payment records saved in your Google Cloud account will be synchronized to this device.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = subtextColor, lineHeight = 20.sp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    if (lastCloudBackupTime != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = cardItemBg,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isBn) "সর্বশেষ ব্যাকআপের সময়: $lastCloudBackupTime" else "Latest cloud backup: $lastCloudBackupTime",
                                modifier = Modifier.padding(10.dp),
                                style = MaterialTheme.typography.labelSmall.copy(color = cyanChevron, fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isRestoring = true
                        viewModel.restoreFromCloud { result ->
                            isRestoring = false
                            showRestoreConfirmDialog = false
                            result.onSuccess { count ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        if (isBn) "ক্লাউড থেকে সফলভাবে $count টি রেকর্ড রিস্টোর করা হয়েছে!"
                                        else "Successfully restored $count records from cloud!"
                                    )
                                }
                            }.onFailure { ex ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        if (isBn) "রিস্টোর ব্যর্থ: ${ex.message}"
                                        else "Restore failed: ${ex.message}"
                                    )
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    enabled = !isRestoring
                ) {
                    if (isRestoring) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(if (isBn) "হ্যাঁ, রিস্টোর করুন" else "Yes, Restore", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showRestoreConfirmDialog = false },
                    enabled = !isRestoring
                ) {
                    Text(if (isBn) "বাতিল" else "Cancel", color = subtextColor)
                }
            }
        )
    }
}
