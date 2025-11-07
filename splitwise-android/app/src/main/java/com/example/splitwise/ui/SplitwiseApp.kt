package com.example.splitwise.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.splitwise.data.DbHelper
import com.example.splitwise.data.Group
import com.example.splitwise.ui.theme.SplitwiseTheme
import com.example.splitwise.viewmodel.SplitwiseViewModel

@Composable
fun SplitwiseApp(viewModel: SplitwiseViewModel) {
    val navController = rememberNavController()

    SplitwiseTheme {
        NavHost(navController = navController, startDestination = "groups") {
            composable("groups") {
                GroupListScreen(viewModel = viewModel, navController = navController)
            }
            composable("group/{groupId}") { backStackEntry ->
                val groupId = backStackEntry.arguments?.getString("groupId")?.toIntOrNull()
                if (groupId != null) {
                    GroupDetailsScreen(groupId = groupId, viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun GroupListScreen(viewModel: SplitwiseViewModel, navController: NavController) {
    val groups by viewModel.groups.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

        val context = LocalContext.current

        val launcher = rememberLauncherForActivityResult(

            contract = ActivityResultContracts.RequestPermission(),

            onResult = { isGranted: Boolean ->

                if (isGranted) {

                    DbHelper(context).exportDb()

                }

            }

        )

    

            val importLauncher = rememberLauncherForActivityResult(

    

                contract = ActivityResultContracts.GetContent(),

    

                onResult = { uri: android.net.Uri? ->

    

                    uri?.let {
                DbHelper(context).importDb(it)
                android.widget.Toast.makeText(context, "Database imported. Please restart the app.", android.widget.Toast.LENGTH_LONG).show()
            }

    

                }

    

            )

    

        

    

            Scaffold(

    

                topBar = {

    

                    TopAppBar(

    

                        title = { Text("Splitwise Free") }

    

                    )

    

                },

    

                floatingActionButton = {

    

                    Column {

    

                        FloatingActionButton(onClick = { showDialog = true }) {

    

                            Icon(Icons.Filled.Add, contentDescription = "Add Group")

    

                        }

    

                        Button(onClick = { launcher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) }) {

    

                            Text("Export DB")

    

                        }

    

                        Button(onClick = { importLauncher.launch("*/*") }) {

    

                            Text("Import DB")

    

                        }

    

                    }

    

                }    ) { padding ->
        GroupList(modifier = Modifier.padding(padding), groups = groups) {
            navController.navigate("group/${it.id}")
        }

        if (showDialog) {
            AddGroupDialog(
                onAddGroup = {
                    viewModel.insertGroup(it)
                    showDialog = false
                },
                onDismiss = { showDialog = false }
            )
        }
    }
}

@Composable
fun GroupList(modifier: Modifier = Modifier, groups: List<Group>, onGroupClick: (Group) -> Unit) {
    LazyColumn(modifier = modifier) {
        items(groups) { group ->
            Text(text = group.name, modifier = Modifier.clickable { onGroupClick(group) })
        }
    }
}

@Composable
fun AddGroupDialog(onAddGroup: (String) -> Unit, onDismiss: () -> Unit) {
    var groupName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Group") },
        text = {
            TextField(
                value = groupName,
                onValueChange = { groupName = it },
                label = { Text("Group Name") }
            )
        },
        confirmButton = {
            Button(onClick = { onAddGroup(groupName) }) {
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