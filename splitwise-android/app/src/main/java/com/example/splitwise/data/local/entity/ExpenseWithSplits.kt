package com.example.splitwise.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Data class to hold an ExpenseEntity and its related list of ExpenseSplitEntity objects.
 * This is used for queries that join expenses and expense_splits tables.
 */
data class ExpenseWithSplits(
    @Embedded val expense: ExpenseEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "expenseId"
    )
    val splits: List<ExpenseSplitEntity>
)
