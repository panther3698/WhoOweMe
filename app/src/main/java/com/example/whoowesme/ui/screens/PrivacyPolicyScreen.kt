package com.example.whoowesme.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Privacy Policy", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            PrivacySection(
                title = "1. Information We Collect",
                content = "Who Owes Me collects data you enter manually, including person names, phone numbers, and transaction details. This data is stored locally on your device and can be synced to Google Firebase if you choose to sign in."
            )
            
            PrivacySection(
                title = "2. How We Use Your Information",
                content = "The information is used solely to provide the app's core functionality: tracking debts, sending reminders, and generating reports. We do not sell or share your personal data with third parties."
            )
            
            PrivacySection(
                title = "3. Data Security",
                content = "We use industry-standard security measures to protect your data. When synced to the cloud, your data is protected by Firebase Security Rules and encrypted in transit."
            )
            
            PrivacySection(
                title = "4. Permissions",
                content = "The app may request access to your contacts (to easily add people) and notifications (to send reminders). These are optional but enhance the user experience."
            )
            
            PrivacySection(
                title = "5. Changes to This Policy",
                content = "We may update our Privacy Policy from time to time. You are advised to review this page periodically for any changes."
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Last updated: October 2023",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun PrivacySection(title: String, content: String) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
