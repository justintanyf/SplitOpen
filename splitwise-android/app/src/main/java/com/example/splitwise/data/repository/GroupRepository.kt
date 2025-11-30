package com.example.splitwise.data.repository

import com.example.splitwise.data.local.dao.GroupDao
import com.example.splitwise.data.local.dao.GroupMemberDao
import com.example.splitwise.data.local.entity.GroupWithMembers
import com.example.splitwise.data.sync.EventType
import com.example.splitwise.data.sync.SyncEvent
import com.example.splitwise.data.sync.SyncManager
import com.example.splitwise.data.user.UserIdManager
import com.example.splitwise.domain.model.Group
import com.example.splitwise.domain.model.GroupMember
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupRepository @Inject constructor(
    private val syncManager: SyncManager,
    private val groupDao: GroupDao,
    private val groupMemberDao: GroupMemberDao,
    private val userIdManager: UserIdManager
) {
    val groups: Flow<List<Group>> = groupDao.getAllGroupsWithMembers().map { entities ->
        entities.map { it.toDomainModel() }
    }

    suspend fun createGroup(name: String): Result<String> {
        val groupId = UUID.randomUUID().toString()
        val userId = userIdManager.getUserId()

        val event = SyncEvent(
            type = EventType.GROUP_CREATE,
            userId = userId,
            groupId = groupId,
            data = mapOf("name" to name),
            timestamp = System.currentTimeMillis()
        )

        val createResult = syncManager.createGroup(groupId, name)
        if (createResult.isFailure) {
            return Result.failure(createResult.exceptionOrNull() ?: Exception("Sync manager failed to create group"))
        }

        val pushResult = syncManager.pushEvent(groupId, event)
        if (pushResult.isFailure) {
            return Result.failure(pushResult.exceptionOrNull() ?: Exception("Failed to push create group event"))
        }

        return Result.success(groupId)
    }

    suspend fun joinGroup(groupCode: String): Result<Unit> {
        return syncManager.joinGroup(groupCode)
    }

}

private fun GroupWithMembers.toDomainModel(): Group {
    return Group(
        id = this.group.id,
        name = this.group.name,
        createdBy = this.group.createdBy,
        createdAt = this.group.createdAt,
        members = this.members.map { it.toDomainModel() }
    )
}

private fun GroupMemberEntity.toDomainModel(): GroupMember {
    return GroupMember(
        userId = this.userId,
        displayName = this.displayName,
        joinedAt = this.joinedAt
    )
}
