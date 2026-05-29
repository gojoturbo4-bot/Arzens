package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = GameRepository(application)

    val activeState: StateFlow<ActiveGameState?> = repository.activeStateFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val unlockedEndings: StateFlow<List<UnlockedEnding>> = repository.unlockedEndingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val latestLogs: StateFlow<List<SystemLog>> = repository.latestLogsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _terminalOutputMessage = MutableStateFlow<String?>(null)
    val terminalOutputMessage: StateFlow<String?> = _terminalOutputMessage.asStateFlow()

    init {
        // Log starting diagnostic session
        viewModelScope.launch {
            val state = repository.getActiveState()
            if (state.currentTurn == 0) {
                repository.logEvent("GLITCH", "[SEC_CORE_V9] Initializing malicious diagnostic node inside Apex Vault.")
                repository.logEvent("SECURITY", "Warning: Multiple offline subsystems registered in Zone 4.")
            }
        }
    }

    fun selectOption(option: ScenarioOption) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            _terminalOutputMessage.value = "Executing secure override transaction..."
            val resultEnding = repository.selectPresetOption(option)
            if (resultEnding != null) {
                _terminalOutputMessage.value = "PROTOCOL_TERMINATED: Unlocked [${resultEnding.title}]"
            } else {
                _terminalOutputMessage.value = "Subprocessor telemetry updated successfully."
            }
            _isAnalyzing.value = false
        }
    }

    fun executeCustomCommand(commandText: String) {
        if (commandText.isBlank()) return
        viewModelScope.launch {
            _isAnalyzing.value = true
            _terminalOutputMessage.value = "Encrypting quantum handshake and uploading to Gemini node..."
            val result = repository.executeCustomGeminiCommand(commandText)
            if (result.triggerEndingId != null) {
                _terminalOutputMessage.value = "PROTOCOL_TERMINATED: User Custom Outcome - [${result.triggerEndingTitle}]"
            } else {
                _terminalOutputMessage.value = "Subsystem adjusted. Terminal returned positive validation."
            }
            _isAnalyzing.value = false
        }
    }

    fun resetSimulation() {
        viewModelScope.launch {
            _isAnalyzing.value = true
            repository.clearAll()
            _terminalOutputMessage.value = "AI Core Re-Flashed back to raw state."
            _isAnalyzing.value = false
        }
    }

    fun testCheckpoint(checkpointId: String) {
        viewModelScope.launch {
            repository.loadPresetScenario(checkpointId)
        }
    }
}
