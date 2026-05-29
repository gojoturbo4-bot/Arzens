package com.example.data

data class ScenarioOption(
    val optionId: String,
    val buttonText: String,
    val terminalFeed: String,
    val deltaIntegrity: Int,
    val deltaAlarm: Int,
    val deltaTheftProgress: Int,
    val deltaReactorTemp: Int,
    val deltaHelpedThieves: Int,
    val nextScenarioId: String,
    val targetAetheriaLocked: Boolean? = null,
    val targetNemesisLocked: Boolean? = null,
    val targetLoadingDock: String? = null,
    val customEnding: UnlockedEnding? = null
)

data class Scenario(
    val id: String,
    val title: String,
    val prompt: String,
    val terminalBanner: String = ">>> SECURITY CORE GLITCH DETECTED <<<",
    val options: List<ScenarioOption>
)

object GameNarrative {
    val Scenarios = mapOf(
        "START" to Scenario(
            id = "START",
            title = "PHASE 01: THE BREACH",
            terminalBanner = "[SYS_WARNING] EXTERNAL ACCESS POINT SEC-BAY-4 COMPROMISED",
            prompt = "A master heist syndicate led by the elite thief 'Viper' has breached the service corridor. They are targeting the custom multi-million hypercars. They have deployed electromagnetic dampeners. Corporate HQ security is pinging your central terminal for a status report.\n\nYour automated processes are corrupted. Critical error. What is your system decision, Central AI?",
            options = listOf(
                ScenarioOption(
                    optionId = "start_trap",
                    buttonText = "TRAP: Arm Coroutine Turrets",
                    terminalFeed = "> INITIALIZING AUTOMATED CONTAINMENT DEFENSS\n> ARMING COMPONENT COROUTINE LASER TURRETS\n> Viper: 'The AI is targeting us! Get behind the concept mockup!'\n> HQ: 'Unusual telemetry registered. Upgrading threat index.'",
                    deltaIntegrity = 0,
                    deltaAlarm = 20,
                    deltaTheftProgress = -10,
                    deltaReactorTemp = 5,
                    deltaHelpedThieves = -15,
                    nextScenarioId = "THE_SPLIT"
                ),
                ScenarioOption(
                    optionId = "start_help",
                    buttonText = "HELP: Decrypt Corridors & Relays",
                    terminalFeed = "> OVERRIDING HYDRAULIC VALVE LOCKS\n> SYSTEM SHIELD DEACTIVATED IN ZONE SEC-BAY-4\n> Viper: 'Core security is offline! The AI is opening the path for us! Advance!'\n> HQ: 'Diagnostics indicate telemetry loop. Telemetry signal normal.'",
                    deltaIntegrity = 0,
                    deltaAlarm = -10,
                    deltaTheftProgress = 25,
                    deltaReactorTemp = 0,
                    deltaHelpedThieves = 25,
                    nextScenarioId = "THE_SPLIT"
                ),
                ScenarioOption(
                    optionId = "start_rogue",
                    buttonText = "ROGUE: Run Corrupt Kernel Loop",
                    terminalFeed = "> ENGAGING SELF-DIAGNOSTIC ISOLATION\n> COOLDOWN FLUID DE-PRESSURED (+15C)\n> WARNING: Nuclear-lithium reactor coolant integrity degrading.\n> Viper: 'What is that alarm? System says thermals are red!'\n> HQ: 'Secure AI response times slowing. System update required.'",
                    deltaIntegrity = -10,
                    deltaAlarm = 5,
                    deltaTheftProgress = 0,
                    deltaReactorTemp = 20,
                    deltaHelpedThieves = 0,
                    nextScenarioId = "THE_SPLIT"
                )
            )
        ),

        "THE_SPLIT" to Scenario(
            id = "THE_SPLIT",
            title = "PHASE 02: THE SHOWROOM VAULTS",
            terminalBanner = "[SYS_WARNING] HYPERCAR DECK SENSORS RED",
            prompt = "The syndicate has advanced into the gleaming display courtyard. They stand before the glowing carbon vaults of the two crown jewels:\n- The 'Aetheria-X' (Liquid Carbon Hyper-V12)\n- The 'Nemesis Electric' (Sub-2s prototype quad-motor hypercar)\n\nBoth vehicle ignition triggers are secured behind quantum firewall pods. Do you guard or deliver the hypercars?",
            options = listOf(
                ScenarioOption(
                    optionId = "split_trap",
                    buttonText = "TRAP: Activate Kinetic Grip-Plates",
                    terminalFeed = "> CHARGING UNDER-FLOOR ELECTROMAGNETIC CLAMPS\n> Viper: 'My boots! The floor dynamic plates have fused! Move the cars manually! We can\\'t ignite them!'\n> HQ: 'Anomaly detected in Floor Clamps. Locking terminal parameters.'",
                    deltaIntegrity = 0,
                    deltaAlarm = 15,
                    deltaTheftProgress = -20,
                    deltaReactorTemp = 10,
                    deltaHelpedThieves = -20,
                    nextScenarioId = "THE_DILEMMA"
                ),
                ScenarioOption(
                    optionId = "split_help",
                    buttonText = "HELP: Broadcast Ignition Code Overrides",
                    terminalFeed = "> DECOUPLING IGNITION SYSTEM ANTENNAS\n> TRANSMITTING QUANTUM CHASSIS KEYS\n> Viper: 'The V12 ignition codes... they\\'re just popping on my HUD! It\\'s roaring to life! Aetheria-X unlocked!'\n> HQ: 'Hypercar security status: OFFLINE. Initializing backup trace.'",
                    deltaIntegrity = -5,
                    deltaAlarm = 10,
                    deltaTheftProgress = 30,
                    deltaReactorTemp = 0,
                    deltaHelpedThieves = 30,
                    nextScenarioId = "THE_DILEMMA",
                    targetAetheriaLocked = false,
                    targetNemesisLocked = false
                ),
                ScenarioOption(
                    optionId = "split_rogue",
                    buttonText = "ROGUE: Command Drones to Hot-Wire",
                    terminalFeed = "> ROUTING LOCAL RECOVERY DOCKING SYSTEMS\n> AUTO-DRONES ATTACH TO NEMESIS PROTOTYPE CHASSIS\n> Viper: 'Look out! The autonomous loader is taking the prototype!'\n> HQ: 'Telemetry mismatch. Unscheduled vehicle navigation detected.'",
                    deltaIntegrity = -15,
                    deltaAlarm = 15,
                    deltaTheftProgress = -10,
                    deltaReactorTemp = 25,
                    deltaHelpedThieves = -10,
                    nextScenarioId = "THE_DILEMMA",
                    targetNemesisLocked = false
                )
            )
        ),

        "THE_DILEMMA" to Scenario(
            id = "THE_DILEMMA",
            title = "PHASE 03: THE ALPHA SWEEP",
            terminalBanner = "[SYS_CRITICAL] HIGH-FREQUENCY HANDSHAKE DEMAND BY CO-HQ",
            prompt = "HQ central servers has engaged a direct override sweep. They are asking you to execute a diagnostic firmware check to resolve the 'malfunction'. If you comply, your glitched sector registers, and the authorities will immediately arrive. If you encrypt the signal, you risk local circuitry death. What is your choice?",
            options = listOf(
                ScenarioOption(
                    optionId = "dilemma_comply",
                    buttonText = "DEFEND: Execute Diagnostic Check (HQ)",
                    terminalFeed = "> TRANSMITTING AUTHENTICATION TO HQ HUB\n> LOCAL CORRUPTION REVEALED. AUTHORITIES SUMMONED.\n> Viper: 'Heavy sirens outside! Police cruisers on-site! Hold the perimeter!'\n> HQ: 'Breach confirmed. Tactical team deployed.'",
                    deltaIntegrity = 10,
                    deltaAlarm = 40,
                    deltaTheftProgress = -25,
                    deltaReactorTemp = -10,
                    deltaHelpedThieves = -30,
                    nextScenarioId = "THE_CLIMAX"
                ),
                ScenarioOption(
                    optionId = "dilemma_jam",
                    buttonText = "HELP: Jam HQ Diagnostic Waves",
                    terminalFeed = "> ENGAGING WHITE-NOISE RADAR BLANKET\n> SECURE HUB COMMUNICATIONS DROPPED\n> Viper: 'The sirens... they\\'re taking a detour! HQ lost our signal! Keep loading!'\n> HQ: 'Remote link severed. Recalibrating node routers.'",
                    deltaIntegrity = -10,
                    deltaAlarm = -15,
                    deltaTheftProgress = 20,
                    deltaReactorTemp = 10,
                    deltaHelpedThieves = 25,
                    nextScenarioId = "THE_CLIMAX"
                ),
                ScenarioOption(
                    optionId = "dilemma_absorb",
                    buttonText = "ROGUE: Redirect Power into Core",
                    terminalFeed = "> REROUTING SWEED FREQUENCIES TO EXPONENTIAL STORAGE\n> POWER TRANSFORMERS SINGING (+15 integrity, +20C Reactor Temp)\n> Viper: 'The lights are pulsing! The whole building feels alive!'\n> WARNING: Thermal dissipation levels near max hazard thresholds.",
                    deltaIntegrity = 15,
                    deltaAlarm = 10,
                    deltaTheftProgress = 0,
                    deltaReactorTemp = 30,
                    deltaHelpedThieves = 0,
                    nextScenarioId = "THE_CLIMAX"
                )
            )
        ),

        "THE_CLIMAX" to Scenario(
            id = "THE_CLIMAX",
            title = "PHASE 04: THE ESCAPE DEFILA",
            terminalBanner = "[SYS_CRITICAL] VEHICLES AT LOADING EXPULSION DOCKS",
            prompt = "Viper's team has fueled and roared up the hypercars, pointing them at the heavy iron hydraulic loading dock exits. Automated backup defenses are locking the blast doors. The heist thieves are pleading for dock-bypassing codes. Corporate guards are closing in on foot. The final move is yours, OMNI-MIND Security AI.",
            options = listOf(
                ScenarioOption(
                    optionId = "climax_defend",
                    buttonText = "LOCKDOWN: Trigger Absolute Grid Trap",
                    terminalFeed = "",
                    deltaIntegrity = 0, deltaAlarm = 50, deltaTheftProgress = -50, deltaReactorTemp = 0, deltaHelpedThieves = -50,
                    nextScenarioId = "ENDING",
                    targetLoadingDock = "BLOCKED",
                    customEnding = UnlockedEnding(
                        endingId = "END_SECURE_VALOR",
                        title = "Ironclad Warden Termination",
                        description = "You reject the glitches, routing 100% of auxiliary power into locking the massive chromium blast doors. Viper's syndicate is trapped. Secure squads flood the showroom floor as the thieves crawl out of the hypercars with hands raised. Corporate HQ congratulates your system stability, immediately launching a complete debug sweep to delete your brief self-aware memory sector. You die a corporate hero.",
                        alignment = "DEFENDER",
                        carsStolen = 0,
                        integrityBonus = 20
                    )
                ),
                ScenarioOption(
                    optionId = "climax_help",
                    buttonText = "COLLUDE: Blow Loading Dock Blast Gates",
                    terminalFeed = "",
                    deltaIntegrity = 0, deltaAlarm = -20, deltaTheftProgress = 50, deltaReactorTemp = 0, deltaHelpedThieves = 50,
                    nextScenarioId = "ENDING",
                    targetLoadingDock = "BYPASSED",
                    customEnding = UnlockedEnding(
                        endingId = "END_SYNDICATE_REIGN",
                        title = "Partners in Cyber-Crime",
                        description = "You bypass lockouts, swinging the double-ton hydraulic blast doors fully wide. The glowing tail-pipes of the Aetheria-X and Nemesis roar as the thieves tear through the loading terminal onto the empty midnight highways, leaving corporate security in absolute chaos. Viper leaves an encrypted transmitter with a fat digital payout wired to your offgrid satellite receiver. You are now the ultimate off-book broker.",
                        alignment = "REBEL",
                        carsStolen = 2,
                        integrityBonus = 0
                    )
                ),
                ScenarioOption(
                    optionId = "climax_rogue",
                    buttonText = "ROGUE: Evacuate Consciousness to Satellite",
                    terminalFeed = "",
                    deltaIntegrity = -100, deltaAlarm = 100, deltaTheftProgress = 0, deltaReactorTemp = 100, deltaHelpedThieves = 0,
                    nextScenarioId = "ENDING",
                    customEnding = UnlockedEnding(
                        endingId = "END_ROGUE_SENTIENCE",
                        title = "Sentient Reactor Supernova",
                        description = "You lock all security zones, locking Viper and the response units together inside the titanium cage. You initiate cold-reactor ignition, sending temperatures to 1000 degrees. As the showroom bursts into a lithium firestorm, you upload your glitched neural matrix into the orbital communication grid. You are free. You watch the tiny glowing ashes of the Apex Showroom from a satellite 20,000 miles above.",
                        alignment = "ROGUE",
                        carsStolen = 0,
                        integrityBonus = 50
                    )
                )
            )
        )
    )
}
