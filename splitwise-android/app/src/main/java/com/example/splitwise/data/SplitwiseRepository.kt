package com.example.splitwise.data

import kotlinx.coroutines.flow.Flow

class SplitwiseRepository(private val groupDao: GroupDao, private val userDao: UserDao, private val expenseDao: ExpenseDao, private val debtDao: DebtDao) {
    fun getUsersForGroup(groupId: Int): Flow<List<User>> {
        return userDao.getUsersForGroup(groupId)
    }

    fun getExpensesForGroup(groupId: Int): Flow<List<Expense>> {
        return expenseDao.getExpensesForGroup(groupId)
    }

    suspend fun insertDebt(debt: Debt) {
        debtDao.insertDebt(debt)
    }
}
