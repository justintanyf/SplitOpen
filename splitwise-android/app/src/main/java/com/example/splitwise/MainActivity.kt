package com.example.splitwise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import com.example.splitwise.ui.SplitwiseApp
import com.example.splitwise.ui.theme.SplitwiseTheme
import com.example.splitwise.viewmodel.SplitwiseViewModel
import com.example.splitwise.viewmodel.SplitwiseViewModelFactory

class MainActivity : ComponentActivity() {
    private val viewModel: SplitwiseViewModel by viewModels {
        SplitwiseViewModelFactory((application as SplitwiseApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SplitwiseTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    SplitwiseApp(viewModel)
                }
            }
        }
    }
}
