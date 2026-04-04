package com.example.whoowesme.ui.screens

import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.filled.ContactPage
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import com.example.whoowesme.model.enums.TransactionType
import com.example.whoowesme.ui.theme.GreenIncome
import com.example.whoowesme.ui.theme.RedExpense
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.whoowesme.ui.theme.GreenIncomeDark
import com.example.whoowesme.ui.theme.RedExpenseDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.unit.dp
import com.example.whoowesme.model.Person
import com.example.whoowesme.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPersonScreen(
    personId: Long? = null,
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var transactionType by remember { mutableStateOf(TransactionType.GIVEN) }
    var transactionDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var dueDate by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showDueDatePicker by remember { mutableStateOf(false) }
    var existingPerson by remember { mutableStateOf<Person?>(null) }
    
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

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
    
    val isEditMode = personId != null

    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri ->
        uri?.let {
            val cursor = context.contentResolver.query(it, null, null, null, null)
            cursor?.use { c ->
                if (c.moveToFirst()) {
                    val id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                    val contactName = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))
                    name = contactName

                    val hasPhoneNumber = c.getInt(c.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                    if (hasPhoneNumber > 0) {
                        val phones = context.contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,
                            null,
                            null
                        )
                        phones?.use { p ->
                            if (p.moveToFirst()) {
                                phoneNumber = p.getString(p.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                            }
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(personId) {
        if (personId != null) {
            val person = viewModel.getPersonById(personId)
            if (person != null) {
                existingPerson = person
                name = person.name
                phoneNumber = person.phoneNumber
                notes = person.notes
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        if (!isEditMode) "Add New Contact" else "Edit Contact",
                        fontWeight = FontWeight.Bold
                    ) 
                },
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
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                placeholder = { Text("e.g. John Doe") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            Button(
                onClick = { contactPickerLauncher.launch(null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Icon(Icons.Default.ContactPage, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Import from Contacts")
            }

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Phone Number") },
                placeholder = { Text("Optional") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                leadingIcon = { Icon(Icons.Outlined.Phone, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                placeholder = { Text("Anything to remember about this person?") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                leadingIcon = { Icon(Icons.AutoMirrored.Outlined.Notes, contentDescription = null) },
                minLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            if (!isEditMode) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                Text(
                    "First Transaction (Optional)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    placeholder = { Text("0.00") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    prefix = { Text("₹ ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )

                val isDark = isSystemInDarkTheme()
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = transactionType == TransactionType.GIVEN,
                        onClick = { transactionType = TransactionType.GIVEN },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = if (isDark) GreenIncomeDark.copy(alpha = 0.2f) else GreenIncome.copy(alpha = 0.1f),
                            activeContentColor = if (isDark) GreenIncomeDark else GreenIncome
                        )
                    ) {
                        Text("I GAVE")
                    }
                    SegmentedButton(
                        selected = transactionType == TransactionType.TAKEN,
                        onClick = { transactionType = TransactionType.TAKEN },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = if (isDark) RedExpenseDark.copy(alpha = 0.2f) else RedExpense.copy(alpha = 0.1f),
                            activeContentColor = if (isDark) RedExpenseDark else RedExpense
                        )
                    ) {
                        Text("I TOOK")
                    }
                }

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
                            leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
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

                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = dueDate?.let { dateFormatter.format(Date(it)) } ?: "Set Due Date",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Due Date") },
                            leadingIcon = { Icon(Icons.Default.Event, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                disabledTextColor = if (dueDate != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { showDueDatePicker = true }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        if (personId == null) {
                            val initialAmount = amount.toDoubleOrNull() ?: 0.0
                            viewModel.addPerson(name, phoneNumber, notes, initialAmount, transactionType, dueDate, transactionDate)
                        } else {
                            existingPerson?.let {
                                viewModel.updatePerson(it.copy(name = name, phoneNumber = phoneNumber, notes = notes))
                            }
                        }
                        onNavigateBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = name.isNotBlank()
            ) {
                Text(
                    if (!isEditMode) "Save Contact" else "Update Contact",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
