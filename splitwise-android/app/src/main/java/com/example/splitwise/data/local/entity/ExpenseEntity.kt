package com.example.splitwise.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = GroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("groupId"), Index("paidBy")]
)
data class ExpenseEntity(
    @PrimaryKey
    val id: String, // UUID
    val groupId: String,
    val description: String,
    val amount: Double,
    val paidBy: String, // userId
    val createdAt: Long,
    val lastModifiedAt: Long,
    val isDeleted: Boolean = false
)
