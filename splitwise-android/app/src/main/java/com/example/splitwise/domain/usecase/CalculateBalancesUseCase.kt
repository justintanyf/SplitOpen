package com.example.splitwise.domain.usecase

import com.example.splitwise.data.repository.ExpenseRepository
import com.example.splitwise.domain.model.Debt
import kotlinx.coroutines.flow.first
import java.util.UUID
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.min

class CalculateBalancesUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository
) {
    suspend operator fun invoke(groupId: String): List<Debt> {
        val expenses = expenseRepository.getExpensesForGroup(groupId).first()
        if (expenses.isEmpty()) return emptyList()

        val balances = mutableMapOf<String, Double>()
        expenses.forEach { expense ->
            expense.splits.forEach { split ->
                val userBalance = balances.getOrPut(split.userId) { 0.0 }
                val newBalance = if (split.isPayer) {
                    userBalance + expense.amount - split.shareAmount
                } else {
                    userBalance - split.shareAmount
                }
                // Round to 2 decimal places to avoid floating point inaccuracies
                balances[split.userId] = "%.2f".format(newBalance).toDouble()
            }
        }
        
        val debtors = balances.filter { it.value < 0 }.toMutableMap()
        val creditors = balances.filter { it.value > 0 }.toMutableMap()
        val debts = mutableListOf<Debt>()

        while (debtors.isNotEmpty() && creditors.isNotEmpty()) {
            val (debtorId, debtorAmount) = debtors.entries.first()
            val (creditorId, creditorAmount) = creditors.entries.first()
            
            val transferAmount = min(abs(debtorAmount), creditorAmount)

            debts.add(
                Debt(
                    id = UUID.randomUUID().toString(),
                    groupId = groupId,
                    fromUserId = debtorId,
                    toUserId = creditorId,
                    amount = transferAmount
                )
            )

            val newDebtorAmount = debtorAmount + transferAmount
            val newCreditorAmount = creditorAmount - transferAmount

            if ("%.2f".format(newDebtorAmount).toDouble() == 0.0) {
                debtors.remove(debtorId)
            } else {
                debtors[debtorId] = newDebtorAmount
            }

            if ("%.2f".format(newCreditorAmount).toDouble() == 0.0) {
                creditors.remove(creditorId)
            } else {
                creditors[creditorId] = newCreditorAmount
            }
        }

        return debts
    }
}
