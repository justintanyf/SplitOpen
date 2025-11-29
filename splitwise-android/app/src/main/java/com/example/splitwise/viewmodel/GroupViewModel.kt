package com.example.splitwise.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitwise.data.repository.GroupRepository
import com.example.splitwise.domain.model.Group
import com.example.splitwise.domain.usecase.CreateGroupUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val createGroupUseCase: CreateGroupUseCase
) : ViewModel() {

    private val _groups = MutableStateFlow<List<Group>>(emptyList())
    val groups: StateFlow<List<Group>> = _groups.asStateFlow()

    private val _createGroupResult = MutableStateFlow<Result<String>?>(null)
    val createGroupResult: StateFlow<Result<String>?> = _createGroupResult.asStateFlow()

    init {
        viewModelScope.launch {
            groupRepository.groups.collect {
                _groups.value = it
            }
        }
    }

    fun createGroup(name: String) {
        viewModelScope.launch {
            _createGroupResult.value = createGroupUseCase(name)
        }
    }
}
