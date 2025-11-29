package com.example.splitwise.data.local.dao

import androidx.room.*
import com.example.splitwise.data.local.entity.ProcessedEventEntity

@Dao
interface ProcessedEventDao {
    @Query("SELECT EXISTS(SELECT 1 FROM processed_events WHERE eventId = :eventId)")
    suspend fun hasProcessed(eventId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun markAsProcessed(event: ProcessedEventEntity)

    @Query("DELETE FROM processed_events WHERE groupId = :groupId AND processedAt < :olderThan")
    suspend fun cleanupOldEvents(groupId: String, olderThan: Long)
}
