package com.example.splitwise.data.local.dao

import androidx.room.*
import com.example.splitwise.data.local.entity.GroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {
    @Query("SELECT * FROM groups WHERE isDeleted = 0 ORDER BY createdAt DESC")
    fun getAllGroups(): Flow<List<GroupEntity>>

    @Transaction
    @Query("SELECT * FROM groups")
    fun getAllGroupsWithMembers(): Flow<List<com.example.splitwise.data.local.entity.GroupWithMembers>>

    @Query("SELECT * FROM groups WHERE id = :groupId")
    suspend fun getGroupById(groupId: String): GroupEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(group: GroupEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(groups: List<GroupEntity>)

    @Query("DELETE FROM groups WHERE id = :groupId")
    suspend fun deleteById(groupId: String)
}
