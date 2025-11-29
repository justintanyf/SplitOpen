package com.example.splitwise.domain.usecase

import com.example.splitwise.data.repository.GroupRepository
import javax.inject.Inject

class CreateGroupUseCase @Inject constructor(
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke(name: String): Result<String> {
        if (name.isBlank()) {
            return Result.failure(IllegalArgumentException("Group name cannot be blank"))
        }
        return groupRepository.createGroup(name)
    }
}
