package com.example.splitwise.data.repository

import com.example.splitwise.data.local.dao.GroupDao
import com.example.splitwise.data.local.dao.GroupMemberDao
import com.example.splitwise.data.local.dao.ProcessedEventDao
import com.example.splitwise.data.local.entity.GroupEntity
import com.example.splitwise.data.local.entity.GroupMemberEntity
import com.example.splitwise.data.local.entity.GroupWithMembers
import com.example.splitwise.data.local.entity.ProcessedEventEntity
import com.example.splitwise.data.sync.EventType
import com.example.splitwise.data.sync.SyncEvent
import com.example.splitwise.data.sync.SyncManager
import com.example.splitwise.data.user.UserIdManager
import com.example.splitwise.domain.model.Group
import com.example.splitwise.domain.model.GroupMember
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupRepository @Inject constructor(
    private val syncManager: SyncManager,
    private val groupDao: GroupDao,
    private val groupMemberDao: GroupMemberDao,
    private val processedEventDao: ProcessedEventDao,
    private val userIdManager: UserIdManager,
    private val expenseRepository: ExpenseRepository
) {
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    val groups: Flow<List<Group>> = groupDao.getAllGroupsWithMembers().map { entities ->
        entities.map { it.toDomainModel() }
    }

    init {
        // Start listening to all groups the user is a member of.
        repositoryScope.launch {
            val userId = userIdManager.getUserId()
            groupMemberDao.getGroupIdsForUser(userId).collect { groupIds ->
                groupIds.forEach { groupId ->
                    listenToGroupEvents(groupId)
                }
            }
        }
    }

    suspend fun createGroup(name: String): Result<String> {
        val groupId = UUID.randomUUID().toString()
        val userId = userIdManager.getUserId()

        val event = SyncEvent(
            type = EventType.GROUP_CREATE,
            userId = userId,
            groupId = groupId,
            data = mapOf("name" to name),
            timestamp = System.currentTimeMillis() // Local timestamp for immediate processing
        )

        // Process locally for immediate UI update
        processEvent(event)

        val createResult = syncManager.createGroup(groupId, name)
        if (createResult.isFailure) {
            return Result.failure(createResult.exceptionOrNull() ?: Exception("Sync manager failed to create group"))
        }

        syncManager.pushEvent(groupId, event)

        listenToGroupEvents(groupId)

        return Result.success(groupId)
    }

    suspend fun joinGroup(groupCode: String): Result<Unit> {
        return syncManager.joinGroup(groupCode)
    }

    private fun listenToGroupEvents(groupId: String) {
        syncManager.startListening(groupId) { event ->
            repositoryScope.launch {
                processEvent(event)
            }
        }
    }

    private suspend fun processEvent(event: SyncEvent) {
        if (processedEventDao.hasProcessed(event.id)) {
            return // Idempotency: event already processed
        }

        when (event.type) {
            EventType.GROUP_CREATE -> {
                val groupName = event.data["name"] ?: "Unnamed Group"
                val group = GroupEntity(
                    id = event.groupId,
                    name = groupName,
                    createdBy = event.userId,
                    createdAt = event.timestamp
                )
                groupDao.insert(group)

                val member = GroupMemberEntity(
                    groupId = event.groupId,
                    userId = event.userId,
                    displayName = userIdManager.getDisplayName(),
                    joinedAt = event.timestamp
                )
                groupMemberDao.insert(member)
            }
            EventType.EXPENSE_ADD, EventType.EXPENSE_EDIT, EventType.EXPENSE_DELETE -> {
                expenseRepository.processExpenseEvent(event)
            }
            else -> {}
        }

        processedEventDao.markAsProcessed(ProcessedEventEntity(eventId = event.id, groupId = event.groupId))
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
