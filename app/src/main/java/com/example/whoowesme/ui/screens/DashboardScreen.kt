package com.example.whoowesme.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.whoowesme.model.PersonWithBalance
import com.example.whoowesme.ui.theme.GreenIncome
import com.example.whoowesme.ui.theme.GreenIncomeDark
import com.example.whoowesme.ui.theme.RedExpense
import com.example.whoowesme.ui.theme.RedExpenseDark
import com.example.whoowesme.viewmodel.DashboardStats
import com.example.whoowesme.viewmodel.MainViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigateToAddPerson: () -> Unit,
    onNavigateToPersonDetail: (Long) -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val people by viewModel.peopleWithBalance.collectAsState(initial = emptyList())
    val stats by viewModel.stats.collectAsState(initial = DashboardStats())
    var searchQuery by remember { mutableStateOf("") }
    var personToDelete by remember { mutableStateOf<PersonWithBalance?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm && personToDelete != null) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteConfirm = false 
                personToDelete = null
            },
            title = { Text("Delete Contact") },
            text = { Text("Are you sure you want to delete ${personToDelete?.person?.name}? This will also delete all associated transactions.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        personToDelete?.let { viewModel.deletePerson(it.person) }
                        showDeleteConfirm = false
                        personToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showDeleteConfirm = false
                    personToDelete = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    val filteredPeople = remember(people, searchQuery) {
        if (searchQuery.isEmpty()) people
        else people.filter { it.person.name.contains(searchQuery, ignoreCase = true) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Outlined.AccountBalanceWallet, contentDescription = null) },
                    label = { Text("Dashboard") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToHistory,
                    icon = { Icon(Icons.Outlined.History, contentDescription = null) },
                    label = { Text("History") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToSettings,
                    icon = { Icon(Icons.Outlined.Settings, contentDescription = null) },
                    label = { Text("Settings") }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddPerson,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Person")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Hello,",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Who Owes Me",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(Icons.Outlined.Notifications, contentDescription = "Notifications")
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                item {
                    SummaryCard(stats)
                }

                item {
                    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                        Text(
                            text = "Contacts",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Search contact...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            ),
                            singleLine = true
                        )
                    }
                }

                if (filteredPeople.isEmpty()) {
                    item {
                        EmptyState(isSearch = searchQuery.isNotEmpty())
                    }
                } else {
                    items(
                        items = filteredPeople,
                        key = { it.person.personId }
                    ) { personWithBalance ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = {
                                if (it == SwipeToDismissBoxValue.EndToStart) {
                                    personToDelete = personWithBalance
                                    showDeleteConfirm = true
                                    false // Don't dismiss yet, wait for confirmation
                                } else {
                                    false
                                }
                            }
                        )

                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {
                                val color = MaterialTheme.colorScheme.errorContainer
                                val alignment = Alignment.CenterEnd
                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 24.dp, vertical = 6.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(color)
                                        .padding(horizontal = 24.dp),
                                    contentAlignment = alignment
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            },
                            enableDismissFromStartToEnd = false,
                            modifier = Modifier
                        ) {
                            PersonItem(
                                personWithBalance = personWithBalance,
                                onClick = { onNavigateToPersonDetail(personWithBalance.person.personId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryCard(stats: DashboardStats) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = primaryColor,
                spotColor = primaryColor
            ),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(primaryColor, secondaryColor)
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                Text(
                    "Total Net Balance",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    text = "₹ ${String.format(Locale.getDefault(), "%,.2f", stats.netBalance)}",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SummaryInfoItem(
                        label = "Receivable",
                        amount = stats.totalReceivable,
                        icon = Icons.Default.Add,
                        contentColor = Color.White
                    )
                    SummaryInfoItem(
                        label = "Payable",
                        amount = stats.totalPayable,
                        icon = Icons.Default.History,
                        contentColor = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryInfoItem(label: String, amount: Double, icon: ImageVector, contentColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(contentColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = contentColor)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor.copy(alpha = 0.7f)
            )
            Text(
                "₹ ${String.format(Locale.getDefault(), "%,.2f", amount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}

@Composable
fun PersonItem(personWithBalance: PersonWithBalance, onClick: () -> Unit) {
    val isDark = isSystemInDarkTheme()
    val balanceColor = if (personWithBalance.balance >= 0) {
        if (isDark) GreenIncomeDark else GreenIncome
    } else {
        if (isDark) RedExpenseDark else RedExpense
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp),
        tonalElevation = 2.dp,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = personWithBalance.person.name.take(1).uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = personWithBalance.person.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (personWithBalance.person.phoneNumber.isNotBlank()) {
                    Text(
                        text = personWithBalance.person.phoneNumber,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹ ${String.format(Locale.getDefault(), "%,.2f", Math.abs(personWithBalance.balance))}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = balanceColor
                )
                Text(
                    text = if (personWithBalance.balance >= 0) "Receivable" else "Payable",
                    style = MaterialTheme.typography.labelSmall,
                    color = balanceColor.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun EmptyState(isSearch: Boolean = false) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isSearch) Icons.Default.Search else Icons.Outlined.People,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = if (isSearch) "No results found" else "Your contact list is empty",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (isSearch) "Try searching with a different name" else "Start by adding someone you've had a transaction with",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
