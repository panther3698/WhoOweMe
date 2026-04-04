package com.example.whoowesme.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.whoowesme.model.Person
import com.example.whoowesme.model.enums.TransactionType
import com.example.whoowesme.ui.components.TransactionItem
import com.example.whoowesme.ui.theme.*
import com.example.whoowesme.util.PdfGenerator
import com.example.whoowesme.viewmodel.MainViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonDetailScreen(
    personId: Long,
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAddTransaction: (Long, String?, Long?) -> Unit,
    onNavigateToEditPerson: (Long) -> Unit
) {
    val context = LocalContext.current
    val transactions by viewModel.getTransactionsForPerson(personId).collectAsState(initial = emptyList())
    var person by remember { mutableStateOf<Person?>(null) }
    var showDeletePersonDialog by remember { mutableStateOf(false) }
    var showReminderConfirmDialog by remember { mutableStateOf(false) }

    val totalBalance = remember(transactions) {
        transactions.sumOf { if (it.type == TransactionType.GIVEN) it.amount else -it.amount }
    }

    LaunchedEffect(personId) {
        person = viewModel.getPersonById(personId)
    }

    if (showDeletePersonDialog) {
        AlertDialog(
            onDismissRequest = { showDeletePersonDialog = false },
            icon = { Icon(Icons.Outlined.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete Person") },
            text = { Text("Are you sure you want to delete ${person?.name}? All transaction history will be lost.") },
            confirmButton = {
                Button(
                    onClick = {
                        person?.let { viewModel.deletePerson(it) }
                        showDeletePersonDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeletePersonDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showReminderConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showReminderConfirmDialog = false },
            icon = { Icon(Icons.Outlined.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Send WhatsApp Reminder?") },
            text = { Text("A WhatsApp message will be prepared to remind ${person?.name} about the balance of ₹ ${String.format(Locale.getDefault(), "%,.2f", Math.abs(totalBalance))}. You'll need to hit send in WhatsApp.") },
            confirmButton = {
                Button(
                    onClick = {
                        person?.let { viewModel.sendManualReminder(context, it, totalBalance) }
                        showReminderConfirmDialog = false
                    }
                ) {
                    Text("Send")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReminderConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        person?.name ?: "Details",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToEditPerson(personId) }) {
                        Icon(Icons.Outlined.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { 
                        person?.let { PdfGenerator.generateAndShareStatement(context, it, transactions, totalBalance) }
                    }) {
                        Icon(Icons.Outlined.Share, contentDescription = "Share Statement")
                    }
                    IconButton(onClick = { showDeletePersonDialog = true }) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToAddTransaction(personId, null, null) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            DetailHeader(
                totalBalance = totalBalance,
                person = person,
                viewModel = viewModel,
                onNavigateToAddTransaction = onNavigateToAddTransaction,
                onSendReminder = { showReminderConfirmDialog = true }
            )

            Text(
                text = "Transaction History",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )

            if (transactions.isEmpty()) {
                EmptyTransactionsState()
            } else {
                val sortedTransactions = remember(transactions) {
                    transactions.sortedBy { it.date }
                }
                
                val transactionsWithBalance = remember(sortedTransactions) {
                    var currentBalance = 0.0
                    sortedTransactions.map { transaction ->
                        if (transaction.type == TransactionType.GIVEN) {
                            currentBalance += transaction.amount
                        } else {
                            currentBalance -= transaction.amount
                        }
                        transaction to currentBalance
                    }.reversed() // Show newest first in the list
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 100.dp)
                ) {
                    items(
                        items = transactionsWithBalance,
                        key = { it.first.transactionId }
                    ) { (transaction, balanceAtThatTime) ->
                        TransactionItem(
                            transaction = transaction,
                            runningBalance = balanceAtThatTime,
                            onEdit = { 
                                onNavigateToAddTransaction(personId, null, transaction.transactionId)
                            },
                            onDelete = { viewModel.deleteTransaction(transaction) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetailHeader(
    totalBalance: Double,
    person: Person?,
    viewModel: MainViewModel,
    onNavigateToAddTransaction: (Long, String?, Long?) -> Unit,
    onSendReminder: () -> Unit
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    
    val balanceLabel = when {
        totalBalance > 0 -> "THEY OWE YOU"
        totalBalance < 0 -> "YOU OWE THEM"
        else -> "ALL SETTLED"
    }

    val balanceColor = if (totalBalance >= 0) {
        if (isDark) GreenIncomeDark else GreenIncome
    } else {
        if (isDark) RedExpenseDark else RedExpense
    }
    
    val backgroundColor = if (totalBalance >= 0) {
        if (isDark) GreenIncome.copy(alpha = 0.15f) else GreenIncomeLight
    } else {
        if (isDark) RedExpense.copy(alpha = 0.15f) else RedExpenseLight
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = balanceLabel,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = balanceColor.copy(alpha = 0.8f)
            )
            Text(
                text = "₹ ${String.format(Locale.getDefault(), "%,.2f", Math.abs(totalBalance))}",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.ExtraBold,
                color = balanceColor
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionButton(
                    icon = Icons.Outlined.AddCircleOutline,
                    label = "Add Entry",
                    modifier = Modifier.weight(1f),
                    color = balanceColor,
                    onClick = { person?.let { onNavigateToAddTransaction(it.personId, "GIVEN", null) } }
                )
                
                ActionButton(
                    icon = Icons.Outlined.History,
                    label = "Record Return",
                    modifier = Modifier.weight(1f),
                    color = balanceColor,
                    onClick = {
                        person?.let { onNavigateToAddTransaction(it.personId, "TAKEN", null) }
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionButton(
                    icon = Icons.Outlined.CheckCircle,
                    label = "Settle Full",
                    modifier = Modifier.weight(1f),
                    color = balanceColor,
                    onClick = {
                        person?.let { 
                            if (totalBalance != 0.0) {
                                val settleType = if (totalBalance > 0) TransactionType.TAKEN else TransactionType.GIVEN
                                viewModel.addTransaction(
                                    personId = it.personId,
                                    amount = Math.abs(totalBalance),
                                    type = settleType,
                                    note = "Settled full balance"
                                )
                            }
                        }
                    }
                )
                
                ActionButton(
                    icon = Icons.Outlined.NotificationsActive,
                    label = "Reminder",
                    modifier = Modifier.weight(1f),
                    color = balanceColor,
                    onClick = { if (totalBalance != 0.0) onSendReminder() }
                )
            }
        }
    }
}

@Composable
fun ActionButton(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    color: Color,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = color),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 1)
        }
    }
}

@Composable
fun EmptyTransactionsState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "No transactions yet",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
