package com.example.splitwise.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "debts",
    foreignKeys = [
        ForeignKey(
            entity = GroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("groupId"), Index("fromUserId"), Index("toUserId")]
)
data class DebtEntity(
    @PrimaryKey
    val id: String, // UUID
    val groupId: String,
    val fromUserId: String,
    val toUserId: String,
    val amount: Double,
    val isSettled: Boolean = false
)
