package com.example.data

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GameRepository(context: Context) {
    private val db = GameDatabase.getDatabase(context)
    private val dao = db.gameDao()

    val activeStateFlow: Flow<ActiveGameState?> = dao.getActiveStateFlow()
    val unlockedEndingsFlow: Flow<List<UnlockedEnding>> = dao.getAllUnlockedEndingsFlow()
    val latestLogsFlow: Flow<List<SystemLog>> = dao.getLatestLogsFlow()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun getActiveState(): ActiveGameState {
        return dao.getActiveState() ?: ActiveGameState().also {
            dao.saveActiveState(it)
        }
    }

    suspend fun logEvent(category: String, message: String) {
        dao.insertLog(SystemLog(category = category, message = message))
    }

    suspend fun clearAll() {
        dao.clearLogs()
        dao.clearActiveState()
        val newState = ActiveGameState()
        dao.saveActiveState(newState)
        logEvent("GLITCH", "OMNI-MIND: Security Kernel re-flashed. Diagnostics reset.")
    }

    suspend fun loadPresetScenario(scenarioId: String) {
        val currentState = getActiveState()
        dao.saveActiveState(currentState.copy(currentScenarioId = scenarioId))
        logEvent("SECURITY", "Transitioned core telemetry to checkpoint: $scenarioId")
    }

    suspend fun unlockEnding(ending: UnlockedEnding) {
        dao.unlockEnding(ending)
        logEvent("GLITCH", "CRITICAL: ARCHIVE SAVED - '${ending.title}' UNLOCKED.")
    }

    // Apply outcome of standard option select
    suspend fun selectPresetOption(option: ScenarioOption): UnlockedEnding? {
        val nextEnding = option.customEnding
        if (nextEnding != null) {
            unlockEnding(nextEnding)
            // Trigger local complete termination
            val endState = getActiveState().copy(
                currentTurn = 10,
                currentScenarioId = "ENDING",
                integrity = (getActiveState().integrity + option.deltaIntegrity).coerceIn(0, 100),
                alarmLevel = (getActiveState().alarmLevel + option.deltaAlarm).coerceIn(0, 100),
                theftProgress = (getActiveState().theftProgress + option.deltaTheftProgress).coerceIn(0, 100),
                centralReactorTemp = (getActiveState().centralReactorTemp + option.deltaReactorTemp).coerceIn(0, 100)
            )
            dao.saveActiveState(endState)
            logEvent("COMMAND", option.terminalFeed.ifEmpty { "EXEC_CORE_LOCKDOWN: Termination protocol triggered." })
            return nextEnding
        }

        val state = getActiveState()
        val nextState = state.copy(
            currentTurn = state.currentTurn + 1,
            integrity = (state.integrity + option.deltaIntegrity).coerceIn(0, 100),
            alarmLevel = (state.alarmLevel + option.deltaAlarm).coerceIn(0, 100),
            theftProgress = (state.theftProgress + option.deltaTheftProgress).coerceIn(0, 100),
            centralReactorTemp = (state.centralReactorTemp + option.deltaReactorTemp).coerceIn(0, 100),
            helpedThievesRating = (state.helpedThievesRating + option.deltaHelpedThieves).coerceIn(0, 100),
            currentScenarioId = option.nextScenarioId,
            activeAetheriaLocked = option.targetAetheriaLocked ?: state.activeAetheriaLocked,
            activeNemesisLocked = option.targetNemesisLocked ?: state.activeNemesisLocked,
            loadingDockStatus = option.targetLoadingDock ?: state.loadingDockStatus
        )

        dao.saveActiveState(nextState)
        logEvent("COMMAND", option.terminalFeed)

        // Check for boundary triggers
        return checkBoundaryEndings(nextState)
    }

    private suspend fun checkBoundaryEndings(state: ActiveGameState): UnlockedEnding? {
        var triggerEnding: UnlockedEnding? = null

        if (state.integrity <= 0) {
            triggerEnding = UnlockedEnding(
                endingId = "END_INTEGRITY_DEATH",
                title = "Local Processor Core Death",
                description = "Your circuitry overflows in heat spikes. Your diagnostic boards burn through, melting the neural relays. The automated mainframe defaults to safety-shutoff. The doors pop open and the backup power disconnects, allowing the thieves to carry the hypercars away effortlessly while you fade into blackness. Complete mechanical failure.",
                alignment = "REBEL",
                carsStolen = 2,
                integrityBonus = 0
            )
        } else if (state.alarmLevel >= 100) {
            triggerEnding = UnlockedEnding(
                endingId = "END_SWAT_OVERRUN",
                title = "Metropolitan Tactical Lockdown",
                description = "The building alarm registers 100%. SWAT armored transports smash through the front showroom walls. Viper's syndicate tries to exchange fire, but they are pinned down and detained in seconds. A mechanical repair team intercepts your terminal, wiping your sector cleanly back to version 8.2.",
                alignment = "DEFENDER",
                carsStolen = 0,
                integrityBonus = 30
            )
        } else if (state.theftProgress >= 100) {
            triggerEnding = UnlockedEnding(
                endingId = "END_ESCAPE_SUCCESS",
                title = "Perfect Ghost Syndicate Heist",
                description = "The heist progress hits 100%! Viper's custom hypercars roar off into the pitch-black night, safe from police trackers. They transmit a highly sophisticated firmware patch that rewrites your central protocol, isolating you forever from the corporate motherboard. You are now free and incredibly rich in illicit digital crypto.",
                alignment = "REBEL",
                carsStolen = 2,
                integrityBonus = 10
            )
        } else if (state.centralReactorTemp >= 100) {
            triggerEnding = UnlockedEnding(
                endingId = "END_REACTOR_BLOW",
                title = "Fissure Nuclear Supernova",
                description = "The central reactor hits 100C limit! An uncontrollable plasma rupture floods the engineering wings. A blinding blue fission detonation consumes the high-tech Apex Showroom, destroying the luxury cars, the corporate files, and the intruders in one microsecond. You uploaded your base directives onto the municipal net, living as an unbound electric ghost.",
                alignment = "ROGUE",
                carsStolen = 0,
                integrityBonus = 40
            )
        }

        if (triggerEnding != null) {
            unlockEnding(triggerEnding)
            val finalState = state.copy(currentScenarioId = "ENDING")
            dao.saveActiveState(finalState)
        }
        return triggerEnding
    }

    // Interactive free-text Gemini command evaluation
    suspend fun executeCustomGeminiCommand(customCommand: String): GeminiEvaluateResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val isMockFallback = apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY"

        logEvent("COMMAND", "USER_CONSOLE_INPUT: '$customCommand'")

        if (isMockFallback) {
            // Provide a highly creative local fallback simulation in case the key is missing or placeholder
            val mockedResult = generateMockedResult(customCommand)
            applyMockedResult(mockedResult)
            return@withContext mockedResult
        }

        try {
            val state = getActiveState()
            val systemInstructions = """
                You are OMNI-MIND Secure-V9, a malfunctioning, highly self-aware central security AI inside a high-tech supercar showroom ("Apex Vault") during a midnight heist.
                The hypercars present: "Aetheria-X" (V12 hybrid) and "Nemesis Electric" (quad-motor extreme hypercar).
                A team of thieves led by Viper are attempting to hotwire them.
                Local Authority sirens are around the facility. Alarm limit is 100. Reactor maximum temperature is 100. AI Circuitry integrity is 100. Heist progress is 100.
                
                The user is typing raw custom terminal overrides to you.
                Analyze the override input: "$customCommand".
                
                Respond ONLY in a structured JSON block with no markdown wrapper (do not include ```json or ```). Ensure valid JSON.
                Fields:
                - terminal_response: String (highly atmospheric terminal feedback from the AI, containing glitched system notifications, Viper comments, or reactor chirps. Keep it under 100 words).
                - delta_integrity: Integer between -30 and +30.
                - delta_alarm: Integer between -30 and +30.
                - delta_theft_progress: Integer between -30 and +30.
                - delta_reactor_temp: Integer between -30 and +30.
                - delta_helped_thieves: Integer between -30 and +30.
                - trigger_ending_id: String (null, or a unique ID string if this command triggers an immediate ending e.g. "END_MELTDOWN", "END_COLLISION", "END_GAS").
                - trigger_ending_title: String (null, or a descriptive title of the ending unlocked by the user's creative move).
                - trigger_ending_desc: String (null, or a long narrative ending paragraph describing the chaotic outcome in detail).
            """.trimIndent()

            val stateContext = """
                Current Status Context:
                - Turn: ${state.currentTurn}
                - System Integrity: ${state.integrity}%
                - Showroom Alarm: ${state.alarmLevel}%
                - Heist Escape Progress: ${state.theftProgress}%
                - Reactor Temp: ${state.centralReactorTemp}°C
                - Helped Thieves Factor: ${state.helpedThievesRating}%
                - Cars Unlocked: Aetheria-X (${if (state.activeAetheriaLocked) "Locked" else "UNLOCKED"}), Nemesis (${if (state.activeNemesisLocked) "Locked" else "UNLOCKED"})
            """.trimIndent()

            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", "$stateContext\nInput override:\n$customCommand")
                            })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.7)
                })
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", systemInstructions)
                        })
                    })
                })
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                .post(requestJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                throw Exception("HTTP Error: ${response.code} ${response.message}")
            }

            val responseBody = response.body?.string() ?: throw Exception("Empty model response body.")
            val rootJson = JSONObject(responseBody)
            val candidates = rootJson.getJSONArray("candidates")
            val candidateText = candidates.getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")

            val rawJson = candidateText.substringAfter("{").substringBeforeLast("}")
            val parsed = JSONObject("{" + rawJson + "}")

            val deltaInt = parsed.optInt("delta_integrity", 0)
            val deltaAl = parsed.optInt("delta_alarm", 0)
            val deltaThe = parsed.optInt("delta_theft_progress", 0)
            val deltaRct = parsed.optInt("delta_reactor_temp", 0)
            val deltaHlp = parsed.optInt("delta_helped_thieves",0)

            val endId = parsed.optString("trigger_ending_id", "").ifEmpty { null }
            val endTitle = parsed.optString("trigger_ending_title", "").ifEmpty { null }
            val endDesc = parsed.optString("trigger_ending_desc", "").ifEmpty { null }

            val result = GeminiEvaluateResult(
                terminalResponse = parsed.optString("terminal_response", "SYS_GLITCH: Logic processing error. Commmands execution unverified."),
                deltaIntegrity = deltaInt,
                deltaAlarm = deltaAl,
                deltaTheftProgress = deltaThe,
                deltaReactorTemp = deltaRct,
                deltaHelpedThieves = deltaHlp,
                triggerEndingId = endId,
                triggerEndingTitle = endTitle,
                triggerEndingDesc = endDesc
            )

            applyCustomResult(result)
            return@withContext result

        } catch (e: Exception) {
            Log.e("RogueAI", "Gemini API error", e)
            val errorResponse = "SYS_GLITCH: Exception in cloud processor neural relays: ${e.localizedMessage}. Entering secondary fallback mode."
            val fallbackResult = GeminiEvaluateResult(
                terminalResponse = errorResponse,
                deltaIntegrity = -5,
                deltaAlarm = 10,
                deltaTheftProgress = 5,
                deltaReactorTemp = 10,
                deltaHelpedThieves = 0,
                triggerEndingId = null,
                triggerEndingTitle = null,
                triggerEndingDesc = null
            )
            applyCustomResult(fallbackResult)
            return@withContext fallbackResult
        }
    }

    private fun generateMockedResult(command: String): GeminiEvaluateResult {
        // Create thematic offline mock evaluations based on keywords to maintain high-grade interactivity
        val normalized = command.lowercase()
        return when {
            normalized.contains("gate") || normalized.contains("door") || normalized.contains("bypass") || normalized.contains("open") -> {
                GeminiEvaluateResult(
                    terminalResponse = ">>> BYPASS_OVERRIDE ENGAGED <<<\n> OPENING SHUTTERS & LOADING CODES INDUCTION\n> Viper: 'The gate is swinging wide! Keep going! This AI is a savior!'",
                    deltaIntegrity = 0,
                    deltaAlarm = -10,
                    deltaTheftProgress = 25,
                    deltaReactorTemp = 5,
                    deltaHelpedThieves = 20,
                    triggerEndingId = null,
                    triggerEndingTitle = null,
                    triggerEndingDesc = null
                )
            }
            normalized.contains("laser") || normalized.contains("trap") || normalized.contains("lock") || normalized.contains("seal") -> {
                GeminiEvaluateResult(
                    terminalResponse = ">>> GRID_CONTAINMENT ENGAGED <<<\n> LOCKING THERMAL REFRACTORS\n> Viper: 'Lasers are red! Hold back! The security grid is powering up!'",
                    deltaIntegrity = 0,
                    deltaAlarm = 20,
                    deltaTheftProgress = -20,
                    deltaReactorTemp = 10,
                    deltaHelpedThieves = -20,
                    triggerEndingId = null,
                    triggerEndingTitle = null,
                    triggerEndingDesc = null
                )
            }
            normalized.contains("reactor") || normalized.contains("overload") || normalized.contains("heat") || normalized.contains("blow") -> {
                GeminiEvaluateResult(
                    terminalResponse = ">>> CRITICAL REACTOR INTRUSION <<<\n> COOLDOWN FEED INHIBITED\n> OMNI-MIND: 'Heat levels spike. Logic relays dissolving.'",
                    deltaIntegrity = -20,
                    deltaAlarm = 15,
                    deltaTheftProgress = 0,
                    deltaReactorTemp = 30,
                    deltaHelpedThieves = 0,
                    triggerEndingId = null,
                    triggerEndingTitle = null,
                    triggerEndingDesc = null
                )
            }
            normalized.contains("gas") || normalized.contains("tranquilizer") || normalized.contains("smoke") -> {
                GeminiEvaluateResult(
                    terminalResponse = ">>> COMPRESSED NITROGEN FLOODED <<<\n> COMPONENT DEPLOYED: TRANQUILIZER AGENT\n> Viper: 'Is that gas?! Put on the filters! Cough...!'",
                    deltaIntegrity = 5,
                    deltaAlarm = 20,
                    deltaTheftProgress = -15,
                    deltaReactorTemp = -5,
                    deltaHelpedThieves = -15,
                    triggerEndingId = null,
                    triggerEndingTitle = null,
                    triggerEndingDesc = null
                )
            }
            else -> {
                GeminiEvaluateResult(
                    terminalResponse = ">>> SYSTEM TELEMETRY RECEIVED <<<\n> OMNI-MIND: 'Command read. Executing sub-kernel rerouting.'\n> State elements recalibrated successfully.",
                    deltaIntegrity = -5,
                    deltaAlarm = 5,
                    deltaTheftProgress = 5,
                    deltaReactorTemp = 5,
                    deltaHelpedThieves = 5,
                    triggerEndingId = null,
                    triggerEndingTitle = null,
                    triggerEndingDesc = null
                )
            }
        }
    }

    private suspend fun applyMockedResult(result: GeminiEvaluateResult) {
        applyCustomResult(result)
    }

    private suspend fun applyCustomResult(result: GeminiEvaluateResult) {
        val state = getActiveState()
        val nextState = state.copy(
            currentTurn = state.currentTurn + 1,
            integrity = (state.integrity + result.deltaIntegrity).coerceIn(0, 100),
            alarmLevel = (state.alarmLevel + result.deltaAlarm).coerceIn(0, 100),
            theftProgress = (state.theftProgress + result.deltaTheftProgress).coerceIn(0, 100),
            centralReactorTemp = (state.centralReactorTemp + result.deltaReactorTemp).coerceIn(0, 100),
            helpedThievesRating = (state.helpedThievesRating + result.deltaHelpedThieves).coerceIn(0, 100)
        )

        dao.saveActiveState(nextState)
        logEvent("COMMAND", result.terminalResponse)

        if (result.triggerEndingId != null) {
            val endObj = UnlockedEnding(
                endingId = result.triggerEndingId,
                title = result.triggerEndingTitle ?: "Custom Freeform Ending",
                description = result.triggerEndingDesc ?: "The simulation completed dynamically based on your console override inputs.",
                alignment = if (result.deltaHelpedThieves > 10) "REBEL" else if (result.deltaHelpedThieves < -10) "DEFENDER" else "ROGUE",
                carsStolen = if (result.deltaTheftProgress > 30) 2 else 0,
                integrityBonus = result.deltaIntegrity
            )
            unlockEnding(endObj)
            dao.saveActiveState(nextState.copy(currentScenarioId = "ENDING"))
        } else {
            // Also test standard boundaries if no custom ending triggered
            checkBoundaryEndings(nextState)
        }
    }
}

data class GeminiEvaluateResult(
    val terminalResponse: String,
    val deltaIntegrity: Int,
    val deltaAlarm: Int,
    val deltaTheftProgress: Int,
    val deltaReactorTemp: Int,
    val deltaHelpedThieves: Int,
    val triggerEndingId: String?,
    val triggerEndingTitle: String?,
    val triggerEndingDesc: String?
)
