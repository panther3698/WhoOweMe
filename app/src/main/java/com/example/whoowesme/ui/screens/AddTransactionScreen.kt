package com.example.whoowesme.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.whoowesme.model.enums.RecurrenceFrequency
import com.example.whoowesme.model.enums.TransactionType
import com.example.whoowesme.ui.theme.BackdropBottomDark
import com.example.whoowesme.ui.theme.BackdropBottomLight
import com.example.whoowesme.ui.theme.BackdropTopDark
import com.example.whoowesme.ui.theme.BackdropTopLight
import com.example.whoowesme.ui.theme.GreenIncome
import com.example.whoowesme.ui.theme.GreenIncomeDark
import com.example.whoowesme.ui.theme.RedExpense
import com.example.whoowesme.ui.theme.RedExpenseDark
import com.example.whoowesme.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    personId: Long,
    transactionId: Long? = null,
    initialType: String? = null,
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val people by viewModel.allPeople.collectAsState(initial = emptyList())
    val transactions by viewModel.allTransactions.collectAsState(initial = emptyList())
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val haptic = LocalHapticFeedback.current

    var selectedPersonId by remember { mutableStateOf(personId) }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var type by remember {
        mutableStateOf(
            if (initialType == "TAKEN") TransactionType.TAKEN else TransactionType.GIVEN
        )
    }

    var transactionDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var dueDate by remember { mutableStateOf<Long?>(null) }
    var promisedPaymentDate by remember { mutableStateOf<Long?>(null) }
    var recurrenceFrequency by remember { mutableStateOf(RecurrenceFrequency.NONE) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showDueDatePicker by remember { mutableStateOf(false) }
    var showPromisedDatePicker by remember { mutableStateOf(false) }
    var expandedPersonDropdown by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val transactionsForPerson by viewModel.getTransactionsForPerson(selectedPersonId).collectAsState(initial = emptyList())
    val initialTransaction = remember(transactionsForPerson) {
        transactionsForPerson
            .filter { it.dueDate != null }
            .minByOrNull { it.date }
    }
    val personExistingDueDate = initialTransaction?.dueDate

    LaunchedEffect(transactionId) {
        if (transactionId != null) {
            val transaction = viewModel.getTransactionById(transactionId)
            transaction?.let {
                selectedPersonId = it.personId
                amount = it.amount.toString()
                note = it.note
                type = it.type
                transactionDate = it.date
                dueDate = it.dueDate
                promisedPaymentDate = it.promisedPaymentDate
                recurrenceFrequency = it.recurrenceFrequency
            }
        }
    }

    LaunchedEffect(personExistingDueDate, transactionId, type) {
        if (transactionId == null && type == TransactionType.GIVEN && personExistingDueDate != null) {
            dueDate = personExistingDueDate
        }
    }

    LaunchedEffect(type, transactionId, personExistingDueDate) {
        if (type == TransactionType.TAKEN) {
            dueDate = null
            promisedPaymentDate = null
            recurrenceFrequency = RecurrenceFrequency.NONE
        } else if (transactionId == null && dueDate == null && personExistingDueDate != null) {
            dueDate = personExistingDueDate
        }
    }

    if (showDatePicker) {
        val datePickerState = androidx.compose.material3.rememberDatePickerState(initialSelectedDateMillis = transactionDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { transactionDate = it }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showDueDatePicker) {
        val dueDatePickerState = androidx.compose.material3.rememberDatePickerState(initialSelectedDateMillis = dueDate ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showDueDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dueDate = dueDatePickerState.selectedDateMillis
                    showDueDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = {
                    dueDate = null
                    showDueDatePicker = false
                }) { Text("Clear") }
            }
        ) {
            DatePicker(state = dueDatePickerState)
        }
    }

    if (showPromisedDatePicker) {
        val promisedDatePickerState = androidx.compose.material3.rememberDatePickerState(
            initialSelectedDateMillis = promisedPaymentDate ?: dueDate ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showPromisedDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    promisedPaymentDate = promisedDatePickerState.selectedDateMillis
                    showPromisedDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = {
                    promisedPaymentDate = null
                    showPromisedDatePicker = false
                }) { Text("Clear") }
            }
        ) {
            DatePicker(state = promisedDatePickerState)
        }
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
                title = {
                    Text(
                        if (transactionId == null) "New Transaction" else "Edit Transaction",
                        fontWeight = FontWeight.Bold
                    )
                },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(backdrop))
        ) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.42f)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)
                        ) {
                            Text(
                                text = if (transactionId == null) "New entry" else "Update entry",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = if (type == TransactionType.GIVEN) "Capture what was lent with clarity." else "Record what came back.",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Keep the amount obvious and dates optional.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                PremiumTransactionSection(
                    title = "Transaction Details",
                    subtitle = "Choose the person and define what happened."
                ) {
                    ExposedDropdownMenuBox(
                        expanded = expandedPersonDropdown,
                        onExpandedChange = { expandedPersonDropdown = !expandedPersonDropdown },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val selectedPerson = people.find { it.personId == selectedPersonId }
                        OutlinedTextField(
                            value = selectedPerson?.name ?: "Select Person",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Person") },
                            leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPersonDropdown) },
                            colors = premiumTransactionFieldColors(),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp)
                        )

                        DropdownMenu(
                            expanded = expandedPersonDropdown,
                            onDismissRequest = { expandedPersonDropdown = false }
                        ) {
                            people.forEach { person ->
                                DropdownMenuItem(
                                    text = { Text(person.name) },
                                    onClick = {
                                        selectedPersonId = person.personId
                                        expandedPersonDropdown = false
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val givenColor = if (isDarkMode) GreenIncomeDark else GreenIncome
                        val takenColor = if (isDarkMode) RedExpenseDark else RedExpense

                        FilterChip(
                            selected = type == TransactionType.GIVEN,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                type = TransactionType.GIVEN
                            },
                            label = {
                                Text(
                                    "I Gave",
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = CircleShape,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = givenColor.copy(alpha = 0.14f),
                                selectedLabelColor = givenColor,
                                selectedLeadingIconColor = givenColor
                            ),
                            leadingIcon = if (type == TransactionType.GIVEN) {
                                {
                                    Icon(
                                        Icons.Default.ArrowUpward,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            } else null
                        )

                        FilterChip(
                            selected = type == TransactionType.TAKEN,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                type = TransactionType.TAKEN
                            },
                            label = {
                                Text(
                                    "They Returned",
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = CircleShape,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = takenColor.copy(alpha = 0.14f),
                                selectedLabelColor = takenColor,
                                selectedLeadingIconColor = takenColor
                            ),
                            leadingIcon = if (type == TransactionType.TAKEN) {
                                {
                                    Icon(
                                        Icons.Default.ArrowDownward,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            } else null
                        )
                    }

                    OutlinedTextField(
                        value = amount,
                        onValueChange = {
                            if (it.isEmpty() || it.toDoubleOrNull() != null) amount = it
                        },
                        label = { Text("Amount") },
                        placeholder = { Text("0.00") },
                        prefix = {
                            Text(
                                "\u20B9 ",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                        colors = premiumTransactionFieldColors()
                    )
                }

                PremiumTransactionSection(
                    title = "Dates",
                    subtitle = "Keep timing simple and only add what matters."
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = dateFormatter.format(Date(transactionDate)),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Date") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Outlined.CalendarToday,
                                        contentDescription = null
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = true,
                                colors = premiumTransactionFieldColors(),
                                shape = RoundedCornerShape(18.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        showDatePicker = true
                                    }
                            )
                        }

                        val isDueDateLocked =
                            transactionId == null && (personExistingDueDate != null || type == TransactionType.TAKEN)
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = dueDate?.let { dateFormatter.format(Date(it)) }
                                    ?: if (type == TransactionType.TAKEN) "No Due Date" else "Set Due Date",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Due Date") },
                                leadingIcon = { Icon(Icons.Outlined.Event, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = true,
                                colors = premiumTransactionFieldColors(
                                    focusedBorderAlpha = if (isDueDateLocked) 0.28f else 0.55f
                                ),
                                shape = RoundedCornerShape(18.dp)
                            )
                            if (!isDueDateLocked) {
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            showDueDatePicker = true
                                        }
                                )
                            }
                        }
                    }

                    if (type == TransactionType.GIVEN) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = promisedPaymentDate?.let { dateFormatter.format(Date(it)) }
                                    ?: "No promised payment date",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Promised Payment") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Outlined.TaskAlt,
                                        contentDescription = null
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = premiumTransactionFieldColors(),
                                shape = RoundedCornerShape(18.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        showPromisedDatePicker = true
                                    }
                            )
                        }
                    }
                }

                if (type == TransactionType.GIVEN) {
                    PremiumTransactionSection(
                        title = "Repeat & Notes",
                        subtitle = "Useful for rent, monthly tabs, or anything that repeats."
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RecurrenceFrequency.entries.forEach { frequency ->
                                FilterChip(
                                    selected = recurrenceFrequency == frequency,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        recurrenceFrequency = frequency
                                    },
                                    label = {
                                        Text(
                                            text = when (frequency) {
                                                RecurrenceFrequency.NONE -> "One time"
                                                RecurrenceFrequency.WEEKLY -> "Weekly"
                                                RecurrenceFrequency.MONTHLY -> "Monthly"
                                            },
                                            fontWeight = FontWeight.Bold
                                        )
                                    },
                                    leadingIcon = if (frequency != RecurrenceFrequency.NONE && recurrenceFrequency == frequency) {
                                        {
                                            Icon(
                                                Icons.Outlined.Repeat,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    } else null,
                                    shape = CircleShape
                                )
                            }
                        }

                        OutlinedTextField(
                            value = note,
                            onValueChange = { note = it },
                            label = { Text("Note") },
                            placeholder = { Text("What is this for?") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            minLines = 4,
                            leadingIcon = {
                                Icon(
                                    Icons.AutoMirrored.Outlined.Notes,
                                    contentDescription = null
                                )
                            },
                            colors = premiumTransactionFieldColors()
                        )
                    }
                } else {
                    PremiumTransactionSection(
                        title = "Note",
                        subtitle = "Optional context for this return or payment."
                    ) {
                        OutlinedTextField(
                            value = note,
                            onValueChange = { note = it },
                            label = { Text("Note") },
                            placeholder = { Text("Any detail worth remembering?") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            minLines = 4,
                            leadingIcon = {
                                Icon(
                                    Icons.AutoMirrored.Outlined.Notes,
                                    contentDescription = null
                                )
                            },
                            colors = premiumTransactionFieldColors()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        val amountDouble = amount.toDoubleOrNull() ?: 0.0
                        if (amountDouble > 0 && selectedPersonId != 0L) {
                            if (transactionId == null) {
                                viewModel.addTransaction(
                                    personId = selectedPersonId,
                                    amount = amountDouble,
                                    type = type,
                                    note = note,
                                    date = transactionDate,
                                    dueDate = dueDate,
                                    promisedPaymentDate = promisedPaymentDate,
                                    recurrenceFrequency = recurrenceFrequency
                                )
                            } else {
                                transactions.find { it.transactionId == transactionId }?.let {
                                    viewModel.updateTransaction(
                                        it.copy(
                                            personId = selectedPersonId,
                                            amount = amountDouble,
                                            type = type,
                                            note = note,
                                            date = transactionDate,
                                            dueDate = dueDate,
                                            promisedPaymentDate = promisedPaymentDate,
                                            recurrenceFrequency = recurrenceFrequency,
                                            recurringSeriesId = if (recurrenceFrequency == RecurrenceFrequency.NONE) {
                                                null
                                            } else {
                                                it.recurringSeriesId ?: UUID.randomUUID().toString()
                                            }
                                        )
                                    )
                                }
                            }
                            onNavigateBack()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(20.dp),
                    enabled = amount.isNotBlank() && (amount.toDoubleOrNull() ?: 0.0) > 0
                ) {
                    Text(
                        "Save Transaction",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun PremiumTransactionSection(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        tonalElevation = 0.dp,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            content()
        }
    }
}

@Composable
private fun premiumTransactionFieldColors(
    focusedBorderAlpha: Float = 0.55f
) = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = focusedBorderAlpha),
    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
    focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
    disabledTextColor = MaterialTheme.colorScheme.onSurface,
    disabledBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
)
