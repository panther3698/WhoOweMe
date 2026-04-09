package com.example.whoowesme.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.whoowesme.ui.theme.BackdropBottomDark
import com.example.whoowesme.ui.theme.BackdropBottomLight
import com.example.whoowesme.ui.theme.BackdropTopDark
import com.example.whoowesme.ui.theme.BackdropTopLight
import com.example.whoowesme.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val isDarkMode by viewModel.isDarkMode.collectAsState(initial = false)
    
    val backdrop = if (isDarkMode) {
        listOf(BackdropTopDark, BackdropBottomDark)
    } else {
        listOf(BackdropTopLight, BackdropBottomLight)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(backdrop))
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    title = { 
                        Text(
                            "Privacy Policy", 
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.titleLarge
                        ) 
                    },
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
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        PrivacySection(
                            title = "1. Information We Collect",
                            content = "Who Owes Me collects data you enter manually, including person names, phone numbers, and transaction details. This data is stored locally on your device and can be synced to Google Firebase if you choose to sign in."
                        )
                        
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 16.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                        )
                        
                        PrivacySection(
                            title = "2. How We Use Your Information",
                            content = "The information is used solely to provide the app's core functionality: tracking debts, sending reminders, and generating reports. We do not sell or share your personal data with third parties."
                        )
                        
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 16.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                        )
                        
                        PrivacySection(
                            title = "3. Data Security",
                            content = "We use industry-standard security measures to protect your data. When synced to the cloud, your data is protected by Firebase Security Rules and encrypted in transit."
                        )
                        
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 16.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                        )
                        
                        PrivacySection(
                            title = "4. Permissions",
                            content = "The app may request access to your contacts (to easily add people) and notifications (to send reminders). These are optional but enhance the user experience."
                        )
                        
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 16.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                        )
                        
                        PrivacySection(
                            title = "5. Changes to This Policy",
                            content = "We may update our Privacy Policy from time to time. You are advised to review this page periodically for any changes."
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Text(
                            text = "Last updated: October 2023",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun PrivacySection(title: String, content: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            lineHeight = 22.sp
        )
    }
}
