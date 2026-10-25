package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.AppDatabase
import com.example.data.repository.FinancialRepository
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.MarketExpensesScreen
import com.example.ui.screens.OutstandingScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.Slate600
import com.example.ui.util.AppLanguage
import com.example.ui.util.AppStrings
import com.example.ui.viewmodel.FinancialViewModel
import com.example.ui.viewmodel.FinancialViewModelFactory

enum class NavigationScreen {
    DASHBOARD,
    MARKET_EXPENSES,
    OUTSTANDING,
    REPORTS,
    SETTINGS
}

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: FinancialViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        com.example.data.cloud.FirebaseAppInitializer.ensureInitialized(applicationContext)

        val database = AppDatabase.getDatabase(applicationContext)
        val syncQueueDao = database.syncQueueDao()
        val networkMonitor = com.example.data.network.NetworkMonitor(applicationContext)
        val syncManager = com.example.data.cloud.FirestoreSyncManager(
            context = applicationContext,
            expenseDao = database.expenseDao(),
            outstandingDao = database.outstandingDao(),
            paymentDao = database.paymentDao(),
            syncQueueDao = syncQueueDao
        )
        val repository = FinancialRepository(
            expenseDao = database.expenseDao(),
            outstandingDao = database.outstandingDao(),
            paymentDao = database.paymentDao(),
            syncQueueDao = syncQueueDao
        )
        val authManager = com.example.data.auth.AuthManager(applicationContext)
        val factory = FinancialViewModelFactory(
            repository = repository,
            authManager = authManager,
            syncManager = syncManager,
            syncQueueDao = syncQueueDao,
            networkMonitor = networkMonitor,
            context = applicationContext
        )
        viewModel = ViewModelProvider(this, factory)[FinancialViewModel::class.java]

        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
            MyApplicationTheme(darkTheme = isDarkMode) {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: FinancialViewModel) {
    var currentScreen by remember { mutableStateOf(NavigationScreen.DASHBOARD) }
    val snackbarHostState = remember { SnackbarHostState() }
    val language by viewModel.language.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

    // Handle system back navigation: return to Dashboard from any secondary screen
    if (currentScreen != NavigationScreen.DASHBOARD) {
        BackHandler {
            currentScreen = NavigationScreen.DASHBOARD
        }
    }

    val navBarContainer = if (isDarkMode) Color(0xFF0F1E36) else Color.White
    val navUnselected = if (isDarkMode) Color(0xFF94A3B8) else Slate600
    val navSelectedIndicator = if (isDarkMode) Color(0xFF1E293B) else Navy800
    val navSelectedText = if (isDarkMode) Color(0xFF38BDF8) else Navy900

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = navBarContainer,
                tonalElevation = 8.dp
            ) {
                // 1. Dashboard
                NavigationBarItem(
                    selected = currentScreen == NavigationScreen.DASHBOARD,
                    onClick = { currentScreen = NavigationScreen.DASHBOARD },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = {
                        Text(
                            text = AppStrings.dashboard(language),
                            fontSize = 11.sp,
                            fontWeight = if (currentScreen == NavigationScreen.DASHBOARD) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = navSelectedText,
                        indicatorColor = navSelectedIndicator,
                        unselectedIconColor = navUnselected,
                        unselectedTextColor = navUnselected
                    ),
                    modifier = Modifier.testTag("nav_dashboard")
                )

                // 2. Market Expenses
                NavigationBarItem(
                    selected = currentScreen == NavigationScreen.MARKET_EXPENSES,
                    onClick = { currentScreen = NavigationScreen.MARKET_EXPENSES },
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Market Expenses") },
                    label = {
                        Text(
                            text = if (language == AppLanguage.BANGLA) "খরচ" else "Expenses",
                            fontSize = 11.sp,
                            fontWeight = if (currentScreen == NavigationScreen.MARKET_EXPENSES) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = EmeraldDark,
                        indicatorColor = EmeraldPrimary,
                        unselectedIconColor = navUnselected,
                        unselectedTextColor = navUnselected
                    ),
                    modifier = Modifier.testTag("nav_market_expenses")
                )

                // 3. Outstanding
                NavigationBarItem(
                    selected = currentScreen == NavigationScreen.OUTSTANDING,
                    onClick = { currentScreen = NavigationScreen.OUTSTANDING },
                    icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Outstanding") },
                    label = {
                        Text(
                            text = if (language == AppLanguage.BANGLA) "বাকি" else "Due",
                            fontSize = 11.sp,
                            fontWeight = if (currentScreen == NavigationScreen.OUTSTANDING) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = navSelectedText,
                        indicatorColor = navSelectedIndicator,
                        unselectedIconColor = navUnselected,
                        unselectedTextColor = navUnselected
                    ),
                    modifier = Modifier.testTag("nav_outstanding")
                )

                // 4. Reports & Print
                NavigationBarItem(
                    selected = currentScreen == NavigationScreen.REPORTS,
                    onClick = { currentScreen = NavigationScreen.REPORTS },
                    icon = { Icon(Icons.Default.Assessment, contentDescription = "Reports") },
                    label = {
                        Text(
                            text = if (language == AppLanguage.BANGLA) "রিপোর্ট" else "Reports",
                            fontSize = 11.sp,
                            fontWeight = if (currentScreen == NavigationScreen.REPORTS) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = navSelectedText,
                        indicatorColor = navSelectedIndicator,
                        unselectedIconColor = navUnselected,
                        unselectedTextColor = navUnselected
                    ),
                    modifier = Modifier.testTag("nav_reports")
                )

                // 5. Settings
                NavigationBarItem(
                    selected = currentScreen == NavigationScreen.SETTINGS,
                    onClick = { currentScreen = NavigationScreen.SETTINGS },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = {
                        Text(
                            text = AppStrings.settings(language),
                            fontSize = 11.sp,
                            fontWeight = if (currentScreen == NavigationScreen.SETTINGS) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = navSelectedText,
                        indicatorColor = navSelectedIndicator,
                        unselectedIconColor = navUnselected,
                        unselectedTextColor = navUnselected
                    ),
                    modifier = Modifier.testTag("nav_settings")
                )
            }
        }
    ) { innerPadding ->
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                NavigationScreen.DASHBOARD -> DashboardScreen(
                    viewModel = viewModel,
                    snackbarHostState = snackbarHostState,
                    onNavigateToExpenses = { currentScreen = NavigationScreen.MARKET_EXPENSES },
                    onNavigateToOutstanding = { currentScreen = NavigationScreen.OUTSTANDING }
                )
                NavigationScreen.MARKET_EXPENSES -> MarketExpensesScreen(
                    viewModel = viewModel,
                    snackbarHostState = snackbarHostState
                )
                NavigationScreen.OUTSTANDING -> OutstandingScreen(
                    viewModel = viewModel,
                    snackbarHostState = snackbarHostState
                )
                NavigationScreen.REPORTS -> ReportsScreen(
                    viewModel = viewModel,
                    snackbarHostState = snackbarHostState
                )
                NavigationScreen.SETTINGS -> SettingsScreen(
                    viewModel = viewModel,
                    snackbarHostState = snackbarHostState
                )
            }
        }
    }
}
