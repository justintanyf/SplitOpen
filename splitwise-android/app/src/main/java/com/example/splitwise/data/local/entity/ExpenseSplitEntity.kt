package com.example.splitwise.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expense_splits",
    foreignKeys = [
        ForeignKey(
            entity = ExpenseEntity::class,
            parentColumns = ["id"],
            childColumns = ["expenseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("expenseId"), Index("userId")]
)
data class ExpenseSplitEntity(
    @PrimaryKey
    val id: String, // UUID
    val expenseId: String,
    val userId: String,
    val shareAmount: Double,
    val isPayer: Boolean = false
)
