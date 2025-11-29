package com.example.splitwise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.splitwise.ui.SplitwiseApp
import com.example.splitwise.ui.theme.SplitwiseTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SplitwiseTheme {
                SplitwiseApp()
            }
        }
    }
}
