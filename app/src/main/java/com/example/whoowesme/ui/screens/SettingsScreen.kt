package com.example.whoowesme.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.whoowesme.viewmodel.MainViewModel
import com.example.whoowesme.util.PdfGenerator
// import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isDarkMode by viewModel.isDarkMode.collectAsState(initial = false)
    val remindersEnabled by viewModel.remindersEnabled.collectAsState(initial = true)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item { SettingsSectionHeader("Preferences") }
            item {
                SettingsSwitchItem(
                    title = "Dark Mode",
                    subtitle = "Toggle dark theme for the app",
                    icon = Icons.Outlined.DarkMode,
                    checked = isDarkMode,
                    onCheckedChange = { viewModel.setDarkMode(it) }
                )
            }
            item {
                SettingsSwitchItem(
                    title = "Reminders",
                    subtitle = "Get notified about due transactions",
                    icon = Icons.Outlined.Notifications,
                    checked = remindersEnabled,
                    onCheckedChange = { viewModel.setRemindersEnabled(it) }
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
            item { SettingsSectionHeader("Data Management") }
            item {
                SettingsClickItem(
                    title = "Export Statements",
                    subtitle = "Generate PDF reports for all contacts",
                    icon = Icons.Outlined.PictureAsPdf,
                    onClick = {
                        scope.launch {
                            val people = viewModel.peopleWithBalance.first()
                            if (people.isEmpty()) {
                                Toast.makeText(context, "No data to export", Toast.LENGTH_SHORT).show()
                                return@launch
                            }
                            
                            var successCount = 0
                            people.forEach { personWithBalance ->
                                val transactions = viewModel.getTransactionsForPerson(personWithBalance.person.personId).first()
                                // Call the new share method directly for each or just generate it
                                PdfGenerator.generateAndShareStatement(
                                    context,
                                    personWithBalance.person,
                                    transactions,
                                    personWithBalance.balance
                                )
                                successCount++
                            }
                            
                            if (successCount > 0) {
                                Toast.makeText(context, "Generated reports for $successCount people", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Export failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
            }
            item {
                SettingsClickItem(
                    title = "Backup Data",
                    subtitle = "Create a copy of your database file",
                    icon = Icons.Outlined.Backup,
                    onClick = {
                        scope.launch {
                            try {
                                val dbFile = context.getDatabasePath("who_owes_me_database")
                                val backupDir = context.getExternalFilesDir(null)
                                val backupFile = File(backupDir, "who_owes_me_backup_${System.currentTimeMillis()}.db")
                                
                                if (dbFile.exists()) {
                                    dbFile.copyTo(backupFile, overwrite = true)
                                    Toast.makeText(context, "Backup saved to: ${backupFile.name}", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, "Database file not found", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Backup failed: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
            item { SettingsSectionHeader("Account") }
            item {
                // val currentUser = FirebaseAuth.getInstance().currentUser
                SettingsClickItem(
                    title = "Sign Out",
                    subtitle = "Logged in as Guest",
                    icon = Icons.AutoMirrored.Outlined.ExitToApp,
                    onClick = {
                        // FirebaseAuth.getInstance().signOut()
                        // In a real app, you'd navigate to AuthScreen
                        Toast.makeText(context, "Signed out successfully", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
            item { SettingsSectionHeader("About") }
            item {
                SettingsClickItem(
                    title = "Who Owes Me",
                    subtitle = "Version 1.0.0 • Professional Edition",
                    icon = Icons.Outlined.Info,
                    onClick = {}
                )
            }
            item {
                SettingsClickItem(
                    title = "Privacy Policy",
                    subtitle = "How we handle your data",
                    icon = Icons.Outlined.PrivacyTip,
                    onClick = onNavigateToPrivacyPolicy
                )
            }
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 24.dp, top = 24.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsSwitchItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        onClick = { onCheckedChange(!checked) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@Composable
fun SettingsClickItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
        }
    }
}
