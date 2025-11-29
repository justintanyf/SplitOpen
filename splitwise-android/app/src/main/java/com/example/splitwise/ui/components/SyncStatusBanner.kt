package com.example.splitwise.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.splitwise.data.sync.SyncState
import com.example.splitwise.data.sync.toDisplayMessage

@Composable
fun SyncStatusBanner(syncState: SyncState) {
    val backgroundColor = when (syncState) {
        is SyncState.Disconnected -> Color.Gray
        is SyncState.Advertising, is SyncState.Discovering, is SyncState.Authenticating -> Color.Blue.copy(alpha = 0.8f)
        is SyncState.Syncing -> Color(0xFFF9A825) // Amber
        is SyncState.UpToDate -> Color(0xFF2E7D32) // Dark Green
        is SyncState.Error -> Color(0xFFC62828) // Dark Red
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = syncState.toDisplayMessage(),
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}
