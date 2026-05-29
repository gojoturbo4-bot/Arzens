package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM active_state WHERE stateId = 1")
    fun getActiveStateFlow(): Flow<ActiveGameState?>

    @Query("SELECT * FROM active_state WHERE stateId = 1")
    suspend fun getActiveState(): ActiveGameState?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveActiveState(state: ActiveGameState)

    @Query("SELECT * FROM unlocked_endings ORDER BY timestamp DESC")
    fun getAllUnlockedEndingsFlow(): Flow<List<UnlockedEnding>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun unlockEnding(ending: UnlockedEnding)

    @Query("SELECT * FROM system_logs ORDER BY id DESC LIMIT 50")
    fun getLatestLogsFlow(): Flow<List<SystemLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: SystemLog)

    @Query("DELETE FROM system_logs")
    suspend fun clearLogs()

    @Query("DELETE FROM active_state")
    suspend fun clearActiveState()
}
