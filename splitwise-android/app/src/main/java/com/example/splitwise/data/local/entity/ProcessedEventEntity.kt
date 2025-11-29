package com.example.splitwise.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tracks processed sync events for idempotency.
 * Prevents duplicate event processing.
 */
@Entity(
    tableName = "processed_events",
    indices = [Index("groupId")]
)
data class ProcessedEventEntity(
    @PrimaryKey
    val eventId: String,
    val groupId: String,
    val processedAt: Long = System.currentTimeMillis()
)
