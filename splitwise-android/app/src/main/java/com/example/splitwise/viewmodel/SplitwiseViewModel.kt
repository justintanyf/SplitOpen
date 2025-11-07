package com.example.splitwise.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.splitwise.data.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SplitwiseViewModel(private val repository: SplitwiseRepository) : ViewModel() {
    val groups: StateFlow<List<Group>> = repository.groups.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun getUsersForGroup(groupId: Int): StateFlow<List<User>> = repository.getUsersForGroup(groupId).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun getExpensesForGroup(groupId: Int): StateFlow<List<Expense>> = repository.getExpensesForGroup(groupId).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun getDebtsForGroup(groupId: Int): StateFlow<List<Debt>> = repository.getDebtsForGroup(groupId).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun insertGroup(groupName: String) {
        viewModelScope.launch {
            repository.insertGroup(Group(name = groupName))
        }
    }

    fun insertUser(userName: String, groupId: Int) {
        viewModelScope.launch {
            repository.insertUser(User(name = userName, groupId = groupId))
        }
    }

    fun insertExpense(description: String, amount: Double, groupId: Int, paidByUserId: Int) {
        viewModelScope.launch {
            repository.insertExpense(Expense(description = description, amount = amount, groupId = groupId, paidByUserId = paidByUserId))
            // For simplicity, split equally among all users in the group
            val users = repository.getUsersForGroup(groupId).first()
            val share = amount / users.size
            users.forEach { user ->
                if (user.id != paidByUserId) {
                    repository.insertDebt(Debt(fromUserId = user.id, toUserId = paidByUserId, amount = share, groupId = groupId))
                }
            }
        }
    }
}

class SplitwiseViewModelFactory(private val repository: SplitwiseRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SplitwiseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SplitwiseViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
