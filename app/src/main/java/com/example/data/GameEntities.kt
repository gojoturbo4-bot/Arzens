package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "unlocked_endings")
data class UnlockedEnding(
    @PrimaryKey val endingId: String,
    val title: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val alignment: String, // "REBEL" (Helped thieves), "DEFENDER" (Lockdown), "ROGUE" (Sentient self)
    val carsStolen: Int,
    val integrityBonus: Int
)

@Entity(tableName = "system_logs")
data class SystemLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val category: String, // "GLITCH", "SECURITY", "THREAT", "COMMAND"
    val message: String
)

@Entity(tableName = "active_state")
data class ActiveGameState(
    @PrimaryKey val stateId: Int = 1, // Single active state row
    val currentTurn: Int = 0,
    val integrity: Int = 100, // 0 to 100
    val alarmLevel: Int = 20, // 0 to 100 (high alarm triggers automated lockdown or SWAT)
    val theftProgress: Int = 0, // 0 to 100 (thieves' escape progress)
    val centralReactorTemp: Int = 45, // 0 to 100 (rogue overheat meter)
    val helpedThievesRating: Int = 0, // 0 to 100 (alignment balance)
    val currentScenarioId: String = "START",
    val activeAetheriaLocked: Boolean = true,
    val activeNemesisLocked: Boolean = true,
    val loadingDockStatus: String = "ARMED" // "ARMED", "BYPASSED", "BLOCKED"
)
