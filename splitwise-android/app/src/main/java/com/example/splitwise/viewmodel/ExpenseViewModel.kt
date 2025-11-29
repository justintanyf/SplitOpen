package com.example.splitwise.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitwise.data.repository.ExpenseRepository
import com.example.splitwise.domain.model.Expense
import com.example.splitwise.domain.usecase.AddExpenseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val addExpenseUseCase: AddExpenseUseCase,
    userIdManager: com.example.splitwise.data.user.UserIdManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val groupId: String = savedStateHandle.get<String>("groupId")!!
    val currentUserId: String = userIdManager.getUserId()

    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()

    init {
        viewModelScope.launch {
            expenseRepository.getExpensesForGroup(groupId).collect {
                _expenses.value = it
            }
        }
    }

    fun addExpense(
        description: String,
        amount: Double,
        paidByUserId: String,
        splitWithUserIds: List<String>
    ) {
        viewModelScope.launch {
            addExpenseUseCase(
                groupId = groupId,
                description = description,
                amount = amount,
                paidByUserId = paidByUserId,
                splitWithUserIds = splitWithUserIds
            )
        }
    }
}
