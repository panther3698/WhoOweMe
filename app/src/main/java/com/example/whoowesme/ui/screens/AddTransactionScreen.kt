package com.example.whoowesme.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.whoowesme.model.enums.TransactionType
import com.example.whoowesme.ui.theme.*
import com.example.whoowesme.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

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
    val isDark = isSystemInDarkTheme()
    
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
    
    var showDatePicker by remember { mutableStateOf(false) }
    var showDueDatePicker by remember { mutableStateOf(false) }
    
    var expandedPersonDropdown by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    val transactions by viewModel.allTransactions.collectAsState(initial = emptyList())
    val transactionsForPerson by viewModel.getTransactionsForPerson(selectedPersonId).collectAsState(initial = emptyList())

    val initialTransaction = remember(transactionsForPerson) {
        transactionsForPerson
            .filter { it.dueDate != null }
            .minByOrNull { it.date }
    }
    val personExistingDueDate = initialTransaction?.dueDate
    val initialTransactionId = initialTransaction?.transactionId

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
            }
        }
    }

    LaunchedEffect(personExistingDueDate, transactionId) {
        // For new "adjustment" transactions, inherit the person's existing due date
        if (transactionId == null && personExistingDueDate != null) {
            dueDate = personExistingDueDate
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = transactionDate)
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
        val dueDatePickerState = rememberDatePickerState(initialSelectedDateMillis = dueDate ?: System.currentTimeMillis())
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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (transactionId == null) "New Transaction" else "Edit Transaction", fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Person Selection
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
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )

                ExposedDropdownMenu(
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

            // Transaction Type Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val givenSelected = type == TransactionType.GIVEN
                val takenSelected = type == TransactionType.TAKEN

                val givenColor = if (isDark) GreenIncomeDark else GreenIncome
                val takenColor = if (isDark) RedExpenseDark else RedExpense

                FilterChip(
                    selected = givenSelected,
                    onClick = { type = TransactionType.GIVEN },
                    label = { Text("I Gave", modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)) },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = givenColor.copy(alpha = 0.15f),
                        selectedLabelColor = givenColor,
                        selectedLeadingIconColor = givenColor
                    ),
                    leadingIcon = if (givenSelected) {
                        { Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    } else null
                )

                FilterChip(
                    selected = takenSelected,
                    onClick = { type = TransactionType.TAKEN },
                    label = { Text("They Returned", modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)) },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = takenColor.copy(alpha = 0.15f),
                        selectedLabelColor = takenColor,
                        selectedLeadingIconColor = takenColor
                    ),
                    leadingIcon = if (takenSelected) {
                        { Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    } else null
                )
            }

            // Amount
            OutlinedTextField(
                value = amount,
                onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) amount = it },
                label = { Text("Amount") },
                placeholder = { Text("0.00") },
                prefix = { Text("₹ ", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            // Date Selection
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
                        leadingIcon = { Icon(Icons.Outlined.CalendarToday, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showDatePicker = true }
                    )
                }

                val isDueDateLocked = transactionId == null && (personExistingDueDate != null || type == TransactionType.TAKEN)
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = dueDate?.let { dateFormatter.format(Date(it)) } ?: if (type == TransactionType.TAKEN) "No Due Date" else "Set Due Date",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Due Date") },
                        leadingIcon = { Icon(Icons.Outlined.Event, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isDueDateLocked) MaterialTheme.colorScheme.outlineVariant else MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            disabledTextColor = if (dueDate != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    if (!isDueDateLocked) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { showDueDatePicker = true }
                        )
                    }
                }
            }

            // Note
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note") },
                placeholder = { Text("What is this for?") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                minLines = 3,
                leadingIcon = { Icon(Icons.AutoMirrored.Outlined.Notes, contentDescription = null) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val amountDouble = amount.toDoubleOrNull() ?: 0.0
                    if (amountDouble > 0 && selectedPersonId != 0L) {
                        if (transactionId == null) {
                            viewModel.addTransaction(
                                personId = selectedPersonId,
                                amount = amountDouble,
                                type = type,
                                note = note,
                                date = transactionDate,
                                dueDate = dueDate
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
                                        dueDate = dueDate
                                    )
                                )
                            }
                        }
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = amount.isNotBlank() && (amount.toDoubleOrNull() ?: 0.0) > 0
            ) {
                Text("Save Transaction", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}
