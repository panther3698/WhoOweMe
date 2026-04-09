package com.example.whoowesme.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.whoowesme.model.Person
import com.example.whoowesme.model.ReminderTone
import com.example.whoowesme.model.enums.TransactionType
import com.example.whoowesme.ui.components.PremiumEmptyState
import com.example.whoowesme.ui.components.TransactionItem
import com.example.whoowesme.ui.theme.BackdropBottomDark
import com.example.whoowesme.ui.theme.BackdropBottomLight
import com.example.whoowesme.ui.theme.BackdropTopDark
import com.example.whoowesme.ui.theme.BackdropTopLight
import com.example.whoowesme.ui.theme.*
import com.example.whoowesme.util.MoneyFormatter
import com.example.whoowesme.util.PdfGenerator
import com.example.whoowesme.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

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
    val haptic = LocalHapticFeedback.current
    val transactions by viewModel.getTransactionsForPerson(personId).collectAsState(initial = emptyList())
    val isDarkMode by viewModel.isDarkMode.collectAsState(initial = false)
    var person by remember { mutableStateOf<Person?>(null) }
    var showDeletePersonDialog by remember { mutableStateOf(false) }
    var showReminderConfirmDialog by remember { mutableStateOf(false) }
    var selectedReminderTone by remember { mutableStateOf(ReminderTone.GENTLE) }

    val totalBalance = remember(transactions) {
        transactions.sumOf { if (it.type == TransactionType.GIVEN) it.amount else -it.amount }
    }
    val activeDueDate = remember(transactions, totalBalance) {
        if (totalBalance > 0) {
            transactions
                .filter { it.type == TransactionType.GIVEN }
                .mapNotNull { it.dueDate }
                .minOrNull()
        } else {
            null
        }
    }
    val activePromisedPaymentDate = remember(transactions, totalBalance) {
        if (totalBalance > 0) {
            transactions
                .filter { it.type == TransactionType.GIVEN }
                .mapNotNull { it.promisedPaymentDate }
                .maxOrNull()
        } else {
            null
        }
    }
    val dueDateText = remember(activeDueDate) {
        activeDueDate?.let { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(it)) }
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
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
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
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("A WhatsApp message will be prepared for ${person?.name} about ${MoneyFormatter.format(totalBalance, absolute = true)}.")
                    dueDateText?.let {
                        Text(
                            text = "Current due date: $it",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    ReminderTone.entries.forEach { tone ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedReminderTone == tone,
                                onClick = { selectedReminderTone = tone }
                            )
                            Column {
                                Text(tone.title, fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = tone.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        person?.let {
                            viewModel.sendManualReminder(
                                context = context,
                                person = it,
                                balance = totalBalance,
                                tone = selectedReminderTone,
                                dueDate = activePromisedPaymentDate ?: activeDueDate
                            )
                        }
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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            person?.name ?: "Details",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        person?.phoneNumber?.takeIf { it.isNotBlank() }?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
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
                        person?.let { p ->
                            val file = PdfGenerator.generateStatement(context, p, transactions, totalBalance)
                            file?.let { PdfGenerator.openPdf(context, it) }
                        }
                    }) {
                        Icon(Icons.Outlined.Visibility, contentDescription = "Preview Statement")
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
                    containerColor = Color.Transparent
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
        val backdrop = if (isDarkMode) {
            listOf(BackdropTopDark, BackdropBottomDark)
        } else {
            listOf(BackdropTopLight, BackdropBottomLight)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(backdrop))
                .padding(padding)
        ) {
            DetailHeader(
                totalBalance = totalBalance,
                isDarkMode = isDarkMode,
                dueDate = activeDueDate,
                promisedPaymentDate = activePromisedPaymentDate,
                person = person,
                viewModel = viewModel,
                onNavigateToAddTransaction = onNavigateToAddTransaction,
                onSendReminder = { showReminderConfirmDialog = true }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Transaction History",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                ) {
                    Text(
                        text = "${transactions.size} entries",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

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
                            isDarkMode = isDarkMode,
                            runningBalance = balanceAtThatTime,
                            onEdit = { 
                                onNavigateToAddTransaction(personId, null, transaction.transactionId)
                            },
                            onDelete = { viewModel.deleteTransaction(transaction) },
                            onAddNextRecurring = if (transaction.recurrenceFrequency != com.example.whoowesme.model.enums.RecurrenceFrequency.NONE) {
                                { viewModel.createNextRecurringTransaction(transaction) }
                            } else null
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
    isDarkMode: Boolean,
    dueDate: Long?,
    promisedPaymentDate: Long?,
    person: Person?,
    viewModel: MainViewModel,
    onNavigateToAddTransaction: (Long, String?, Long?) -> Unit,
    onSendReminder: () -> Unit
) {
    val balanceLabel = when {
        totalBalance > 0 -> "RECEIVABLE"
        totalBalance < 0 -> "PAYABLE"
        else -> "SETTLED"
    }

    val balanceColor = if (totalBalance >= 0) {
        if (isDarkMode) GreenIncomeDark else GreenIncome
    } else {
        if (isDarkMode) RedExpenseDark else RedExpense
    }
    
    val backgroundColor = if (totalBalance >= 0) {
        if (isDarkMode) GreenIncome.copy(alpha = 0.08f) else GreenIncomeLight.copy(alpha = 0.6f)
    } else {
        if (isDarkMode) RedExpense.copy(alpha = 0.08f) else RedExpenseLight.copy(alpha = 0.6f)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            backgroundColor,
                            Color.Transparent
                        )
                    )
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = balanceLabel,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.2.sp,
                color = balanceColor.copy(alpha = 0.85f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = MoneyFormatter.format(totalBalance, absolute = true),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black,
                color = balanceColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = when {
                    totalBalance > 0 -> "Everything due from this person."
                    totalBalance < 0 -> "What you still need to return."
                    else -> "No outstanding balance right now."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            if (dueDate != null && totalBalance > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                DueStatusBanner(
                    dueDate = dueDate,
                    promisedPaymentDate = promisedPaymentDate,
                    amount = totalBalance
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionButton(
                    icon = Icons.Outlined.AddCircleOutline,
                    label = "Give",
                    modifier = Modifier.weight(1f),
                    color = balanceColor,
                    onClick = { person?.let { onNavigateToAddTransaction(it.personId, "GIVEN", null) } }
                )
                
                ActionButton(
                    icon = Icons.Outlined.History,
                    label = "Take",
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
                    label = "Settle",
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
fun DueStatusBanner(dueDate: Long, promisedPaymentDate: Long?, amount: Double) {
    val now = System.currentTimeMillis()
    val isOverdue = dueDate < now
    val daysOffset = ((kotlin.math.abs(now - dueDate)) / TimeUnit.DAYS.toMillis(1)).coerceAtLeast(0)
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val containerColor = if (isOverdue) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
    }
    val contentColor = if (isOverdue) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = containerColor
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Text(
                text = if (isOverdue) "Overdue follow-up" else "Upcoming due date",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Text(
                text = if (isOverdue) {
                    "${MoneyFormatter.format(amount, absolute = true)} was due on ${dateFormatter.format(Date(dueDate))}"
                } else {
                    "${MoneyFormatter.format(amount, absolute = true)} is due on ${dateFormatter.format(Date(dueDate))}"
                },
                style = MaterialTheme.typography.bodySmall,
                color = contentColor
            )
            if (isOverdue) {
                Text(
                    text = "Overdue by ${daysOffset} day(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor
                )
            }
            promisedPaymentDate?.let {
                Text(
                    text = "Promised payment: ${dateFormatter.format(Date(it))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor
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
    val haptic = LocalHapticFeedback.current
    OutlinedButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
            contentColor = color
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.25f)),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

@Composable
fun EmptyTransactionsState() {
    PremiumEmptyState(
        icon = Icons.AutoMirrored.Filled.ReceiptLong,
        title = "No transactions yet",
        subtitle = "Every time you lend or borrow money from this contact, it will be listed here.",
        modifier = Modifier.padding(top = 40.dp)
    )
}
