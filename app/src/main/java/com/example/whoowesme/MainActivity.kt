package com.example.whoowesme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.work.WorkManager
import com.example.whoowesme.data.SettingsManager
import com.example.whoowesme.database.AppDatabase
import com.example.whoowesme.repository.AppRepository
import com.example.whoowesme.ui.screens.AddPersonScreen
import com.example.whoowesme.ui.screens.AddTransactionScreen
import com.example.whoowesme.ui.screens.AppLockScreen
import com.example.whoowesme.ui.screens.DashboardScreen
import com.example.whoowesme.ui.screens.OnboardingScreen
import com.example.whoowesme.ui.screens.PersonDetailScreen
import com.example.whoowesme.ui.screens.PrivacyPolicyScreen
import com.example.whoowesme.ui.screens.SettingsScreen
import com.example.whoowesme.ui.screens.TransactionHistoryScreen
import com.example.whoowesme.ui.theme.WhoOwesMeTheme
import com.example.whoowesme.util.AppLockManager
import com.example.whoowesme.viewmodel.MainViewModel
import com.example.whoowesme.viewmodel.MainViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {
    private lateinit var settingsManager: SettingsManager
    private var isAppUnlocked by mutableStateOf(false)
    private var authInProgress = false
    private var lastStopTimestamp: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getDatabase(this)
        val repository = AppRepository(database.personDao(), database.transactionDao())
        val workManager = WorkManager.getInstance(this)
        settingsManager = SettingsManager(this)
        val viewModelFactory = MainViewModelFactory(repository, workManager, settingsManager)

        setContent {
            val viewModel: MainViewModel = viewModel(factory = viewModelFactory)
            val isDarkMode by viewModel.isDarkMode.collectAsState(initial = false)
            val onboardingCompleted by viewModel.onboardingCompleted.collectAsState(initial = null)
            val appLockEnabled by viewModel.appLockEnabled.collectAsState(initial = null)

            WhoOwesMeTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (onboardingCompleted == null || appLockEnabled == null) {
                        return@Surface
                    }

                    val navController = rememberNavController()
                    val startDestination = if (onboardingCompleted == false) "onboarding" else "dashboard"

                    Box(modifier = Modifier.fillMaxSize()) {
                        NavHost(
                            navController = navController,
                            startDestination = startDestination,
                            enterTransition = {
                                slideIntoContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                    animationSpec = tween(400)
                                ) + fadeIn(animationSpec = tween(400))
                            },
                            exitTransition = {
                                slideOutOfContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                    animationSpec = tween(400)
                                ) + fadeOut(animationSpec = tween(400))
                            },
                            popEnterTransition = {
                                slideIntoContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                    animationSpec = tween(400)
                                ) + fadeIn(animationSpec = tween(400))
                            },
                            popExitTransition = {
                                slideOutOfContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                    animationSpec = tween(400)
                                ) + fadeOut(animationSpec = tween(400))
                            }
                        ) {
                            composable("onboarding") {
                                OnboardingScreen(
                                    viewModel = viewModel,
                                    onFinished = {
                                        navController.navigate("dashboard") {
                                            popUpTo("onboarding") { inclusive = true }
                                        }
                                    }
                                )
                            }
                            composable("dashboard") {
                                DashboardScreen(
                                    viewModel = viewModel,
                                    onNavigateToAddPerson = { navController.navigate("add_person") },
                                    onNavigateToPersonDetail = { personId ->
                                        navController.navigate("person_detail/$personId")
                                    },
                                    onNavigateToHistory = { navController.navigate("history") },
                                    onNavigateToSettings = { navController.navigate("settings") }
                                )
                            }
                            composable("add_person?personId={personId}") { backStackEntry ->
                                val personId = backStackEntry.arguments?.getString("personId")?.toLongOrNull()
                                AddPersonScreen(
                                    personId = personId,
                                    viewModel = viewModel,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                            composable("person_detail/{personId}") { backStackEntry ->
                                val personId = backStackEntry.arguments?.getString("personId")?.toLongOrNull()
                                    ?: return@composable
                                PersonDetailScreen(
                                    personId = personId,
                                    viewModel = viewModel,
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToAddTransaction = { id, type, transactionId ->
                                        val typeArg = if (type != null) "type=$type" else ""
                                        val txIdArg = if (transactionId != null) "transactionId=$transactionId" else ""
                                        val query = listOf(typeArg, txIdArg).filter { it.isNotEmpty() }.joinToString("&")
                                        val route = if (query.isNotEmpty()) "add_transaction/$id?$query" else "add_transaction/$id"
                                        navController.navigate(route)
                                    },
                                    onNavigateToEditPerson = { id ->
                                        navController.navigate("add_person?personId=$id")
                                    }
                                )
                            }
                            composable("add_transaction/{personId}?type={type}&transactionId={transactionId}") { backStackEntry ->
                                val personId = backStackEntry.arguments?.getString("personId")?.toLongOrNull()
                                    ?: return@composable
                                val type = backStackEntry.arguments?.getString("type")
                                val transactionId = backStackEntry.arguments?.getString("transactionId")?.toLongOrNull()
                                AddTransactionScreen(
                                    personId = personId,
                                    initialType = type,
                                    transactionId = transactionId,
                                    viewModel = viewModel,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                            composable("history") {
                                TransactionHistoryScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToEditTransaction = { personId, transactionId ->
                                        navController.navigate("add_transaction/$personId?transactionId=$transactionId")
                                    },
                                    onNavigateToSettings = { navController.navigate("settings") },
                                    onNavigateToDashboard = { navController.navigate("dashboard") }
                                )
                            }
                            composable("settings") {
                                SettingsScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToPrivacyPolicy = { navController.navigate("privacy_policy") }
                                )
                            }
                            composable("privacy_policy") {
                                PrivacyPolicyScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                        }

                        if (appLockEnabled == true && !isAppUnlocked) {
                            AppLockScreen(
                                viewModel = viewModel,
                                onUnlock = { requestAppUnlock() }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::settingsManager.isInitialized) {
            lifecycleScope.launch {
                val enabled = settingsManager.appLockEnabled.first()
                if (enabled) {
                    val currentTime = System.currentTimeMillis()
                    // Only lock if more than 30 seconds have passed since the app was last stopped.
                    if (!isAppUnlocked || (currentTime - lastStopTimestamp > 30000)) {
                        isAppUnlocked = false
                        requestAppUnlock()
                    }
                } else {
                    isAppUnlocked = true
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
        lastStopTimestamp = System.currentTimeMillis()
    }

    private fun requestAppUnlock() {
        if (authInProgress || isAppUnlocked) return
        if (!AppLockManager.isAuthAvailable(this)) {
            isAppUnlocked = true
            return
        }
        authInProgress = true
        val prompt = BiometricPrompt(
            this,
            ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    authInProgress = false
                    isAppUnlocked = true
                }
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    authInProgress = false
                }
                override fun onAuthenticationFailed() {
                }
            }
        )
        prompt.authenticate(AppLockManager.buildPromptInfo())
    }
}
