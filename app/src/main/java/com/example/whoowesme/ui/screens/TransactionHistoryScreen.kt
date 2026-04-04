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
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.whoowesme.ui.components.TransactionItem
import com.example.whoowesme.viewmodel.MainViewModel
import com.example.whoowesme.model.enums.TransactionType
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEditTransaction: (Long, Long) -> Unit
) {
    val transactions by viewModel.allTransactions.collectAsState(initial = emptyList())
    val people by viewModel.allPeople.collectAsState(initial = emptyList())
    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf<TransactionType?>(null) }

    val filteredTransactions = remember(transactions, searchQuery, people, selectedTypeFilter) {
        transactions.filter { transaction ->
            val matchesSearch = if (searchQuery.isBlank()) true
            else {
                val name = people.find { it.personId == transaction.personId }?.name ?: ""
                name.contains(searchQuery, ignoreCase = true) || 
                transaction.note.contains(searchQuery, ignoreCase = true) ||
                transaction.amount.toString().contains(searchQuery)
            }
            
            val matchesType = selectedTypeFilter == null || transaction.type == selectedTypeFilter
            
            matchesSearch && matchesType
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Transaction History", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Sort/Filter Dialog */ }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
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
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                placeholder = { Text("Search by name or note...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                singleLine = true
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedTypeFilter == null,
                    onClick = { selectedTypeFilter = null },
                    label = { Text("All") },
                    shape = RoundedCornerShape(12.dp)
                )
                FilterChip(
                    selected = selectedTypeFilter == TransactionType.GIVEN,
                    onClick = { selectedTypeFilter = TransactionType.GIVEN },
                    label = { Text("Gave") },
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = if (selectedTypeFilter == TransactionType.GIVEN) {
                        { Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null
                )
                FilterChip(
                    selected = selectedTypeFilter == TransactionType.TAKEN,
                    onClick = { selectedTypeFilter = TransactionType.TAKEN },
                    label = { Text("Took") },
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = if (selectedTypeFilter == TransactionType.TAKEN) {
                        { Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null
                )
            }

            if (filteredTransactions.isEmpty()) {
                EmptyHistoryState(isSearch = searchQuery.isNotBlank())
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 32.dp, top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = filteredTransactions.sortedByDescending { it.date },
                        key = { it.transactionId }
                    ) { transaction ->
                        val personName = people.find { it.personId == transaction.personId }?.name
                        TransactionItem(
                            transaction = transaction,
                            personName = personName,
                            onEdit = { onNavigateToEditTransaction(transaction.personId, transaction.transactionId) },
                            onDelete = { viewModel.deleteTransaction(transaction) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyHistoryState(isSearch: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
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
                imageVector = if (isSearch) Icons.Default.Search else Icons.Outlined.History,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = if (isSearch) "No transactions found" else "No history yet",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (isSearch) "Try a different search term" else "Your transactions will appear here",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
