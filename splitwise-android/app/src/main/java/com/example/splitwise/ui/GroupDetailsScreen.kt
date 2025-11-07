package com.example.splitwise.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.splitwise.data.User
import com.example.splitwise.viewmodel.SplitwiseViewModel

@Composable
fun GroupDetailsScreen(groupId: Int, viewModel: SplitwiseViewModel) {
    val users by viewModel.getUsersForGroup(groupId).collectAsState()
    val expenses by viewModel.getExpensesForGroup(groupId).collectAsState()
    val debts by viewModel.getDebtsForGroup(groupId).collectAsState()
    var showUserDialog by remember { mutableStateOf(false) }
    var showExpenseDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Group Details") }
            )
        },
        floatingActionButton = {
            Column {
                Button(onClick = { showUserDialog = true }) {
                    Text("Add User")
                }
                Button(onClick = { showExpenseDialog = true }) {
                    Text("Add Expense")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Text(text = "Users")
            users.forEach { user ->
                Text(text = user.name)
            }

            Text(text = "Expenses")
            expenses.forEach { expense ->
                Text(text = "${expense.description}: ${expense.amount}")
            }

            val userMap = users.associateBy({ it.id }, { it.name })
            Text(text = "Debts")
            debts.forEach { debt ->
                Text(text = "${userMap[debt.fromUserId]} owes ${userMap[debt.toUserId]} ${debt.amount}")
            }
        }

        if (showUserDialog) {
            AddUserDialog(
                onAddUser = {
                    viewModel.insertUser(it, groupId)
                    showUserDialog = false
                },
                onDismiss = { showUserDialog = false }
            )
        }

        if (showExpenseDialog) {
            AddExpenseDialog(
                users = users,
                onAddExpense = {
                    description, amount, paidBy ->
                    viewModel.insertExpense(description, amount, groupId, paidBy)
                    showExpenseDialog = false
                },
                onDismiss = { showExpenseDialog = false }
            )
        }
    }
}

@Composable
fun AddUserDialog(onAddUser: (String) -> Unit, onDismiss: () -> Unit) {
    var userName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add User") },
        text = {
            TextField(
                value = userName,
                onValueChange = { userName = it },
                label = { Text("User Name") }
            )
        },
        confirmButton = {
            Button(onClick = { onAddUser(userName) }) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AddExpenseDialog(
    users: List<User>,
    onAddExpense: (String, Double, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf(users.firstOrNull()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Expense") },
        text = {
            Column {
                TextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") }
                )
                TextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") }
                )
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    TextField(
                        readOnly = true,
                        value = selectedUser?.name ?: "",
                        onValueChange = { },
                        label = { Text("Paid by") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = expanded
                            )
                        },
                        colors = ExposedDropdownMenuDefaults.textFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        users.forEach { user ->
                            DropdownMenuItem(
                                onClick = {
                                    selectedUser = user
                                    expanded = false
                                }
                            ) {
                                Text(text = user.name)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onAddExpense(description, amount.toDouble(), selectedUser?.id ?: 0) }) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
