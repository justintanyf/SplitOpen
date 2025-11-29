package com.example.splitwise.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.splitwise.domain.model.Expense
import com.example.splitwise.viewmodel.ExpenseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailsScreen(
    expenseViewModel: ExpenseViewModel = hiltViewModel(),
    onAddExpenseClick: (String) -> Unit,
    onShowBalancesClick: (String) -> Unit
) {
    val expenses by expenseViewModel.expenses.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Group: ${expenseViewModel.groupId.take(8)}...") },
                actions = {
                    Button(onClick = { onShowBalancesClick(expenseViewModel.groupId) }) {
                        Text("Balances")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onAddExpenseClick(expenseViewModel.groupId) }) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            LazyColumn {
                items(expenses) { expense ->
                    ExpenseListItem(expense = expense)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListItem(expense: Expense) {
    Card(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = expense.description, style = MaterialTheme.typography.titleMedium)
            Text(text = "Amount: ${expense.amount}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Paid by: ${expense.paidBy.take(8)}...", style = MaterialTheme.typography.bodySmall)
        }
    }
}
