package com.example.whoowesme.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.whoowesme.ui.theme.BackdropBottomDark
import com.example.whoowesme.ui.theme.BackdropBottomLight
import com.example.whoowesme.ui.theme.BackdropTopDark
import com.example.whoowesme.ui.theme.BackdropTopLight
import com.example.whoowesme.util.AppLockManager
import com.example.whoowesme.util.DatabaseBackupManager
import com.example.whoowesme.util.PdfGenerator
import com.example.whoowesme.viewmodel.MainViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val isDarkMode by viewModel.isDarkMode.collectAsState(initial = false)
    val remindersEnabled by viewModel.remindersEnabled.collectAsState(initial = true)
    val appLockEnabled by viewModel.appLockEnabled.collectAsState(initial = false)
    var restoreUri by remember { mutableStateOf<Uri?>(null) }

    val exportBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            try {
                DatabaseBackupManager.exportDatabase(context, uri)
                Toast.makeText(context, "Backup exported successfully", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Backup export failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val restoreBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        restoreUri = uri
    }

    if (restoreUri != null) {
        AlertDialog(
            onDismissRequest = { restoreUri = null },
            title = { Text("Restore Backup") },
            text = {
                Text("Restoring will replace the current local data on this device. Export a fresh backup first if you want a rollback point.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        val selectedUri = restoreUri ?: return@Button
                        scope.launch {
                            try {
                                DatabaseBackupManager.restoreDatabase(context, selectedUri)
                                Toast.makeText(context, "Backup restored. Restarting app...", Toast.LENGTH_LONG).show()
                                restartApp(context)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Restore failed: ${e.message}", Toast.LENGTH_SHORT).show()
                            } finally {
                                restoreUri = null
                            }
                        }
                    }
                ) {
                    Text("Restore")
                }
            },
            dismissButton = {
                TextButton(onClick = { restoreUri = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    val backdrop = if (isDarkMode) {
        listOf(BackdropTopDark, BackdropBottomDark)
    } else {
        listOf(BackdropTopLight, BackdropBottomLight)
    }

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
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0f)
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Brush.verticalGradient(backdrop)),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (isDarkMode) 0.15f else 0.42f)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Private, simple, and under your control",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Manage appearance, reminders, privacy, and local data from one calm place.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

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
            item {
                SettingsSwitchItem(
                    title = "App Lock",
                    subtitle = "Require biometric or device unlock when opening the app",
                    icon = Icons.Outlined.Lock,
                    checked = appLockEnabled,
                    onCheckedChange = {
                        if (it && !AppLockManager.isAuthAvailable(context)) {
                            Toast.makeText(context, "Set up a device lock or biometric first", Toast.LENGTH_LONG).show()
                        } else {
                            viewModel.setAppLockEnabled(it)
                        }
                    }
                )
            }

            item { SettingsSectionHeader("Data") }
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
                    title = "Export Backup",
                    subtitle = "Save a restorable copy of your local data",
                    icon = Icons.Outlined.Backup,
                    onClick = {
                        exportBackupLauncher.launch("who_owes_me_backup_${System.currentTimeMillis()}.db")
                    }
                )
            }
            item {
                SettingsClickItem(
                    title = "Restore Backup",
                    subtitle = "Import a backup file and restart the app",
                    icon = Icons.Outlined.Restore,
                    onClick = {
                        restoreBackupLauncher.launch(
                            arrayOf(
                                "application/octet-stream",
                                "application/x-sqlite3",
                                "*/*"
                            )
                        )
                    }
                )
            }

            item { SettingsSectionHeader("Account") }
            item {
                SettingsClickItem(
                    title = "Sign Out",
                    subtitle = "Logged in as Guest",
                    icon = Icons.AutoMirrored.Outlined.ExitToApp,
                    onClick = {
                        Toast.makeText(context, "Signed out successfully", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            item { SettingsSectionHeader("About") }
            item {
                SettingsClickItem(
                    title = "Who Owes Me",
                    subtitle = "Version 1.0.0 | Premium Edition",
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
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(top = 10.dp, bottom = 2.dp)
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
    val haptic = LocalHapticFeedback.current
    Surface(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onCheckedChange(!checked)
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .padding(18.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
    val haptic = LocalHapticFeedback.current
    Surface(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .padding(18.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
            )
        }
    }
}

private fun restartApp(context: Context) {
    val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        ?.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        } ?: return

    context.startActivity(launchIntent)
    if (context is Activity) {
        context.finishAffinity()
    }
}
