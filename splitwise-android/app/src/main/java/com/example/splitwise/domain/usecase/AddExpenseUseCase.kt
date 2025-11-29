package com.example.splitwise.domain.usecase

import com.example.splitwise.data.repository.ExpenseRepository
import javax.inject.Inject

class AddExpenseUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository
) {
    suspend operator fun invoke(
        groupId: String,
        description: String,
        amount: Double,
        paidByUserId: String,
        splitWithUserIds: List<String>
    ): Result<String> {
        if (description.isBlank()) {
            return Result.failure(IllegalArgumentException("Description cannot be blank"))
        }
        if (amount <= 0) {
            return Result.failure(IllegalArgumentException("Amount must be positive"))
        }
        if (splitWithUserIds.isEmpty()) {
            return Result.failure(IllegalArgumentException("Expense must be split with at least one person"))
        }
        return expenseRepository.addExpense(
            groupId = groupId,
            description = description,
            amount = amount,
            paidByUserId = paidByUserId,
            splitWithUserIds = splitWithUserIds
        )
    }
}
