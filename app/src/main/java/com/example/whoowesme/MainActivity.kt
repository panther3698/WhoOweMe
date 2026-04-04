package com.example.whoowesme

import android.os.Bundle
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.work.WorkManager
import com.example.whoowesme.data.SettingsManager
import com.example.whoowesme.database.AppDatabase
import com.example.whoowesme.repository.AppRepository
import com.example.whoowesme.viewmodel.MainViewModel
import com.example.whoowesme.viewmodel.MainViewModelFactory
import com.example.whoowesme.ui.screens.AddPersonScreen
import com.example.whoowesme.ui.screens.AddTransactionScreen
import com.example.whoowesme.ui.screens.DashboardScreen
import com.example.whoowesme.ui.screens.AuthScreen
import com.example.whoowesme.ui.screens.OnboardingScreen
import com.example.whoowesme.ui.screens.PersonDetailScreen
import com.example.whoowesme.ui.screens.PrivacyPolicyScreen
import com.example.whoowesme.ui.screens.SettingsScreen
// import com.google.firebase.auth.FirebaseAuth
import com.example.whoowesme.ui.screens.TransactionHistoryScreen
import com.example.whoowesme.ui.theme.WhoOwesMeTheme

import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Handle permission results
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        
        val permissionsToRequest = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.READ_CONTACTS)
        }
        
        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
        
        val database = AppDatabase.getDatabase(this)
        val repository = AppRepository(database.personDao(), database.transactionDao())
        val workManager = WorkManager.getInstance(this)
        val settingsManager = SettingsManager(this)
        val viewModelFactory = MainViewModelFactory(repository, workManager, settingsManager)

        setContent {
            val viewModel: MainViewModel = viewModel(factory = viewModelFactory)
            val isDarkMode = viewModel.isDarkMode.collectAsState(initial = false).value
            val onboardingCompleted = viewModel.onboardingCompleted.collectAsState(initial = false).value

            WhoOwesMeTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    // val currentUser = FirebaseAuth.getInstance().currentUser
                    val startDestination = when {
                        !onboardingCompleted -> "onboarding"
                        // currentUser == null -> "auth"
                        else -> "dashboard"
                    }
                    
                    NavHost(navController = navController, startDestination = startDestination) {
                        composable("onboarding") {
                            OnboardingScreen(
                                viewModel = viewModel,
                                onFinished = { 
                                    navController.navigate("auth") {
                                        popUpTo("onboarding") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("auth") {
                            AuthScreen(
                                onAuthSuccess = {
                                    navController.navigate("dashboard") {
                                        popUpTo("auth") { inclusive = true }
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
                            val personId = backStackEntry.arguments?.getString("personId")?.toLongOrNull() ?: return@composable
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
                            val personId = backStackEntry.arguments?.getString("personId")?.toLongOrNull() ?: return@composable
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
                                }
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
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
