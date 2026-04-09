package com.example.whoowesme.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.res.stringResource
import com.example.whoowesme.R
import com.example.whoowesme.model.PersonDueStatus
import com.example.whoowesme.model.PersonWithBalance
import com.example.whoowesme.ui.theme.BackdropBottomDark
import com.example.whoowesme.ui.theme.BackdropBottomLight
import com.example.whoowesme.ui.theme.BackdropTopDark
import com.example.whoowesme.ui.theme.BackdropTopLight
import com.example.whoowesme.ui.theme.GreenIncome
import com.example.whoowesme.ui.theme.GreenIncomeDark
import com.example.whoowesme.ui.theme.RedExpense
import com.example.whoowesme.ui.theme.RedExpenseDark
import com.example.whoowesme.util.MoneyFormatter
import com.example.whoowesme.ui.components.PremiumEmptyState
import com.example.whoowesme.viewmodel.DashboardStats
import com.example.whoowesme.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
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
    val dueStatuses by viewModel.dueStatuses.collectAsState(initial = emptyList())
    val isDarkMode by viewModel.isDarkMode.collectAsState(initial = false)
    var searchQuery by remember { mutableStateOf("") }
    var personToDelete by remember { mutableStateOf<PersonWithBalance?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm && personToDelete != null) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteConfirm = false 
                personToDelete = null
            },
            title = { Text(stringResource(R.string.delete_contact_title)) },
            text = { Text(stringResource(R.string.delete_contact_msg, personToDelete?.person?.name ?: "")) },
            confirmButton = {
                TextButton(
                    onClick = {
                        personToDelete?.let { viewModel.deletePerson(it.person) }
                        showDeleteConfirm = false
                        personToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.delete_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showDeleteConfirm = false
                    personToDelete = null
                }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    val filteredPeople = remember(people, searchQuery) {
        if (searchQuery.isEmpty()) people
        else people.filter { it.person.name.contains(searchQuery, ignoreCase = true) }
    }
    val dueStatusByPerson = remember(dueStatuses) {
        dueStatuses.associateBy { it.person.personId }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                tonalElevation = 0.dp
            ) {
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Outlined.AccountBalanceWallet, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_dashboard)) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToHistory,
                    icon = { Icon(Icons.Outlined.History, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_history)) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToSettings,
                    icon = { Icon(Icons.Outlined.Settings, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_settings)) }
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
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.dashboard_add_person_content_desc))
            }
        }
    ) { padding ->
        val backdrop = if (isDarkMode) {
            listOf(BackdropTopDark, BackdropBottomDark)
        } else {
            listOf(BackdropTopLight, BackdropBottomLight)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(backdrop))
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                            ) {
                                Text(
                                    text = stringResource(R.string.dashboard_tagline),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Text(
                                text = stringResource(R.string.dashboard_title),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(R.string.dashboard_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = onNavigateToSettings,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                        ) {
                            Icon(Icons.Outlined.Settings, contentDescription = stringResource(R.string.dashboard_settings_content_desc))
                        }
                    }
                }

                item {
                    SummaryCard(stats = stats, isDarkMode = isDarkMode)
                }

                item {
                    CashFlowMiniTrend(stats = stats, isDarkMode = isDarkMode)
                }

                if (dueStatuses.isNotEmpty()) {
                    item {
                        FollowUpCard(dueStatuses = dueStatuses)
                    }
                }

                item {
                    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.contacts_header),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                            ) {
                                Text(
                                    text = "${filteredPeople.size}",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                            tonalElevation = 1.dp
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text(stringResource(R.string.search_people_hint)) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                shape = RoundedCornerShape(18.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent
                                ),
                                singleLine = true
                            )
                        }
                    }
                }

                if (filteredPeople.isEmpty()) {
                item {
                    EmptyState(
                        isSearch = searchQuery.isNotEmpty(),
                        onAddPerson = onNavigateToAddPerson
                    )
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
                                        .clip(RoundedCornerShape(22.dp))
                                        .background(color)
                                        .padding(horizontal = 24.dp),
                                    contentAlignment = alignment
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = stringResource(R.string.dashboard_delete_content_desc),
                                        tint = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            },
                            enableDismissFromStartToEnd = false,
                            modifier = Modifier
                        ) {
                            PersonItem(
                                personWithBalance = personWithBalance,
                                isDarkMode = isDarkMode,
                                dueStatus = dueStatusByPerson[personWithBalance.person.personId],
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
fun SummaryCard(stats: DashboardStats, isDarkMode: Boolean) {
    val primaryColor = MaterialTheme.colorScheme.primaryContainer
    val secondaryColor = MaterialTheme.colorScheme.surface

    val receivableColor = if (isDarkMode) GreenIncomeDark else GreenIncome
    val payableColor = if (isDarkMode) RedExpenseDark else RedExpense

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
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
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)
                ) {
                    Text(
                        text = stringResource(R.string.total_net_balance),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = MoneyFormatter.format(stats.netBalance),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (stats.netBalance >= 0) stringResource(R.string.net_balance_positive) else stringResource(R.string.net_balance_negative),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryInfoItem(
                        label = stringResource(R.string.receivable),
                        amount = stats.totalReceivable,
                        icon = Icons.Default.Add,
                        contentColor = receivableColor,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryInfoItem(
                        label = stringResource(R.string.payable),
                        amount = stats.totalPayable,
                        icon = Icons.Default.History,
                        contentColor = payableColor,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryInfoItem(label: String, amount: Double, icon: ImageVector, contentColor: Color, modifier: Modifier = Modifier) {
    val haptic = LocalHapticFeedback.current
    Surface(
        modifier = modifier,
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        },
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(contentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = contentColor)
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = MoneyFormatter.format(amount, absolute = true),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

@Composable
fun FollowUpCard(dueStatuses: List<PersonDueStatus>) {
    val overdueStatuses = dueStatuses.filter { it.isOverdue }
    val nextDue = dueStatuses.minByOrNull { it.dueDate }
    val overdueAmount = overdueStatuses.sumOf { it.balance }
    val dateFormatter = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (overdueStatuses.isNotEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                )
                Text(
                    text = stringResource(R.string.needs_follow_up),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (overdueStatuses.isNotEmpty()) {
                    stringResource(R.string.overdue_summary, overdueStatuses.size, MoneyFormatter.format(overdueAmount, absolute = true))
                } else {
                    stringResource(R.string.dues_on_track)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            nextDue?.let {
                Spacer(modifier = Modifier.height(12.dp))
                AssistChip(
                    onClick = { },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    label = {
                        Text(
                            if (it.isPromiseMissed && it.promisedPaymentDate != null) {
                                stringResource(R.string.promise_missed_label, it.person.name, dateFormatter.format(Date(it.promisedPaymentDate)))
                            } else if (it.isOverdue) {
                                stringResource(R.string.overdue_label, it.person.name, dateFormatter.format(Date(it.dueDate)))
                            } else {
                                stringResource(R.string.due_on_label, it.person.name, dateFormatter.format(Date(it.dueDate)))
                            }
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun CashFlowMiniTrend(stats: DashboardStats, isDarkMode: Boolean) {
    val receivableColor = if (isDarkMode) GreenIncomeDark else GreenIncome
    val payableColor = if (isDarkMode) RedExpenseDark else RedExpense
    val total = (stats.totalReceivable + stats.totalPayable).toFloat()
    val receivableWeight = if (total > 0f) (stats.totalReceivable.toFloat() / total) else 0f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.cash_flow_split),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (total > 0f) {
                Text(
                    text = stringResource(R.string.percent_receivable, (receivableWeight * 100).toInt()),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = receivableColor
                )
            } else {
                Text(
                    text = stringResource(R.string.no_activity),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
        ) {
            val width = size.width
            val height = size.height

            if (total == 0f) {
                drawRoundRect(
                    color = Color.Gray.copy(alpha = 0.2f),
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(height / 2),
                    style = androidx.compose.ui.graphics.drawscope.Fill
                )
            } else {
                drawRoundRect(
                    color = payableColor.copy(alpha = 0.3f),
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(height / 2),
                    style = androidx.compose.ui.graphics.drawscope.Fill
                )
                drawRoundRect(
                    color = receivableColor,
                    size = androidx.compose.ui.geometry.Size(width * receivableWeight, height),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(height / 2),
                    style = androidx.compose.ui.graphics.drawscope.Fill
                )
            }
        }
    }
}

@Composable
fun PersonItem(personWithBalance: PersonWithBalance, isDarkMode: Boolean, dueStatus: PersonDueStatus? = null, onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val balanceColor = if (personWithBalance.balance >= 0) {
        if (isDarkMode) GreenIncomeDark else GreenIncome
    } else {
        if (isDarkMode) RedExpenseDark else RedExpense
    }

    Surface(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = personWithBalance.person.name.take(1).uppercase(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = personWithBalance.person.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (personWithBalance.person.phoneNumber.isNotBlank() || dueStatus != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    if (dueStatus != null) {
                        DueStatusPill(dueStatus = dueStatus)
                    } else {
                        Text(
                            text = personWithBalance.person.phoneNumber,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = MoneyFormatter.format(personWithBalance.balance, absolute = true),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = balanceColor
                )
                Text(
                    text = if (personWithBalance.balance >= 0) stringResource(R.string.receivable) else stringResource(R.string.payable),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = balanceColor.copy(alpha = 0.85f)
                )
            }
        }
    }
}

@Composable
fun DueStatusPill(dueStatus: PersonDueStatus) {
    val dateFormatter = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }
    val containerColor = if (dueStatus.isOverdue) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
    }
    val contentColor = if (dueStatus.isOverdue) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }
    val label = if (dueStatus.isOverdue) {
        if (dueStatus.isPromiseMissed && dueStatus.promisedPaymentDate != null) {
            stringResource(R.string.due_status_promise_missed)
        } else {
            stringResource(R.string.due_status_overdue, dueStatus.daysOffset)
        }
    } else {
        dueStatus.promisedPaymentDate?.let {
            stringResource(R.string.due_status_promised, dateFormatter.format(Date(it)))
        } ?: stringResource(R.string.due_status_due, dateFormatter.format(Date(dueStatus.dueDate)))
    }

    Surface(
        shape = CircleShape,
        color = containerColor,
        tonalElevation = 0.dp
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
    }
}

@Composable
fun EmptyState(onAddPerson: () -> Unit, isSearch: Boolean = false) {
    PremiumEmptyState(
        icon = if (isSearch) Icons.Default.Search else Icons.Outlined.People,
        title = if (isSearch) stringResource(R.string.empty_state_search_title) else stringResource(R.string.empty_state_contacts_title),
        subtitle = if (isSearch) stringResource(R.string.empty_state_search_subtitle) else stringResource(R.string.empty_state_contacts_subtitle),
        isSearch = isSearch,
        actionLabel = if (isSearch) null else stringResource(R.string.empty_state_add_contact_btn),
        onAction = if (isSearch) null else onAddPerson,
        modifier = Modifier.padding(top = 48.dp, bottom = 80.dp)
    )
}
