package com.example.splitwise.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.splitwise.ui.components.SyncStatusBanner
import com.example.splitwise.ui.screens.AddExpenseScreen
import com.example.splitwise.ui.screens.BalancesScreen
import com.example.splitwise.ui.screens.GroupDetailsScreen
import com.example.splitwise.ui.screens.GroupListScreen
import com.example.splitwise.ui.theme.SplitwiseTheme
import com.example.splitwise.viewmodel.SyncViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitwiseApp(
    syncViewModel: SyncViewModel = hiltViewModel()
) {
    val syncState by syncViewModel.syncState.collectAsState()

    SplitwiseTheme {
        val navController = rememberNavController()
        Scaffold { padding ->
            Column(modifier = Modifier.padding(padding)) {
                SyncStatusBanner(syncState = syncState)
                NavHost(navController = navController, startDestination = "groups") {
                    composable("groups") {
                        GroupListScreen(onGroupClick = { groupId ->
                            navController.navigate("group/$groupId")
                        })
                    }
                    composable("group/{groupId}") {
                        GroupDetailsScreen(
                            onAddExpenseClick = { groupId ->
                                navController.navigate("group/$groupId/add_expense")
                            },
                            onShowBalancesClick = { groupId ->
                                navController.navigate("group/$groupId/balances")
                            }
                        )
                    }
                    composable("group/{groupId}/add_expense") {
                        AddExpenseScreen(onExpenseAdded = { navController.popBackStack() })
                    }
                    composable("group/{groupId}/balances") {
                        BalancesScreen()
                    }
                }
            }
        }
    }
}