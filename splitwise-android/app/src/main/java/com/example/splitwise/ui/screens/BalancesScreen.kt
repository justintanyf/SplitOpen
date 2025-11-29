package com.example.splitwise.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.splitwise.domain.model.Debt
import com.example.splitwise.viewmodel.BalanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BalancesScreen(
    balanceViewModel: BalanceViewModel = hiltViewModel()
) {
    val debts by balanceViewModel.debts.collectAsState()

    LaunchedEffect(Unit) {
        balanceViewModel.calculateBalances()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Balances for Group ${balanceViewModel.groupId.take(8)}...") })
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (debts.isEmpty()) {
                Text("All settled up!", modifier = Modifier.padding(16.dp))
            } else {
                LazyColumn {
                    items(debts) { debt ->
                        DebtListItem(debt = debt)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtListItem(debt: Debt) {
    Card(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${debt.fromUserId.take(8)}... owes ${debt.toUserId.take(8)}...",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Amount: ${"%.2f".format(debt.amount)}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
