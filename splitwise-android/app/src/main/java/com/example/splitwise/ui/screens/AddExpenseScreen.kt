package com.example.splitwise.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.splitwise.data.user.UserIdManager
import com.example.splitwise.viewmodel.ExpenseViewModel
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    expenseViewModel: ExpenseViewModel = hiltViewModel(),
    onExpenseAdded: () -> Unit
) {
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    
    val currentUser = expenseViewModel.currentUserId
    val splitWith = listOf(currentUser, "dummy-user-id")

    Scaffold { padding ->
        Column(modifier = Modifier.padding(padding)) {
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") }
            )
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") }
            )
            Button(onClick = {
                val amountDouble = amount.toDoubleOrNull()
                if (amountDouble != null && description.isNotBlank()) {
                    expenseViewModel.addExpense(description, amountDouble, currentUser, splitWith)
                    onExpenseAdded()
                }
            }) {
                Text("Add Expense")
            }
        }
    }
}
