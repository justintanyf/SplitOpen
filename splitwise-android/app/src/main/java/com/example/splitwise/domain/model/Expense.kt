package com.example.splitwise.domain.model

/**
 * Domain model for an expense with its splits.
 */
data class Expense(
    val id: String,
    val groupId: String,
    val description: String,
    val amount: Double,
    val paidBy: String,
    val createdAt: Long,
    val lastModifiedAt: Long,
    val isDeleted: Boolean = false,
    val splits: List<ExpenseSplit> = emptyList()
)

data class ExpenseSplit(
    val id: String,
    val expenseId: String,
    val userId: String,
    val shareAmount: Double,
    val isPayer: Boolean = false
)
