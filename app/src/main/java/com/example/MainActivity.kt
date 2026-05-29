package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.GameViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("main_scaffold"),
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    MainGameTerminalScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

// Custom Terminal Cyber Colors for consistent theme styling
object CyberTheme {
    val BgDark = Color(0xFF0A0A0E)      // Warm Slate Black #0A0A0E
    val CardDark = Color(0xFF0F172A)    // Deep Slate Blue-Gary #0F172A (slate-900)
    val BorderGray = Color(0xFF1E293B)  // Medium Slate Border #1E293B (slate-800)
    val TextGreen = Color(0xFF10B981)   // Solid emerald-400 equivalent #10B981
    val TextCyan = Color(0xFF38BDF8)    // Soft beautiful sky cyan #38BDF8
    val TextAmber = Color(0xFFF59E0B)   // Pure solid warning amber #F59E0B
    val TextRed = Color(0xFFEF4444)     // Brilliant alert red #EF4444 (red-500)
    val ConsoleScreen = Color(0xFF030307)// Ink Black screen #030307
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainGameTerminalScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.activeState.collectAsStateWithLifecycle()
    val logs by viewModel.latestLogs.collectAsStateWithLifecycle()
    val endings by viewModel.unlockedEndings.collectAsStateWithLifecycle()
    val isAnalyzing by viewModel.isAnalyzing.collectAsStateWithLifecycle()
    val terminalOutputMessage by viewModel.terminalOutputMessage.collectAsStateWithLifecycle()

    var customCommandText by remember { mutableStateOf("") }
    var expandedEndingsArchive by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val logListState = rememberLazyListState()

    // Scroll to top of command log whenever a new log arrives
    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            logListState.animateScrollToItem(0)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CyberTheme.BgDark)
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Console Badge (Styled under Sentinel Bold Typography)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                    .background(CyberTheme.CardDark)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Styled circular indicator badge
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CyberTheme.TextRed.copy(alpha = 0.15f))
                            .border(1.dp, CyberTheme.TextRed.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "System alert",
                            tint = CyberTheme.TextRed,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "SYSTEM: ACTIVE MALFUNCTION",
                            color = CyberTheme.TextRed.copy(alpha = 0.8f),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = "OMNI-MIND-AI://SECURE_VAULT_V9.01.H",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.SansSerif,
                            letterSpacing = (-0.3).sp
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .border(1.dp, CyberTheme.TextCyan, RoundedCornerShape(12.dp))
                        .background(CyberTheme.TextCyan.copy(alpha = 0.1f))
                        .clickable { viewModel.resetSimulation() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("reset_button")
                ) {
                    Text(
                        text = "CORE_FLASH",
                        color = CyberTheme.TextCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            val activeStateNonNull = state
            if (activeStateNonNull != null) {
                // Section 1: Dynamic Telemetry Gauges (Status monitoring)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TelemetryMiniGauge(
                        title = "SYS_INTEGRITY",
                        value = activeStateNonNull.integrity,
                        unit = "%",
                        color = CyberTheme.TextCyan,
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryMiniGauge(
                        title = "SHOWROOM_ALARM",
                        value = activeStateNonNull.alarmLevel,
                        unit = "%",
                        color = CyberTheme.TextAmber,
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryMiniGauge(
                        title = "HEIST_PROGRESS",
                        value = activeStateNonNull.theftProgress,
                        unit = "%",
                        color = CyberTheme.TextGreen,
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryMiniGauge(
                        title = "CORE_REACTOR",
                        value = activeStateNonNull.centralReactorTemp,
                        unit = "°C",
                        color = CyberTheme.TextRed,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Section 2: Showroom Schematic Map Structure
                ShowroomSchematicMap(state = activeStateNonNull)
                // Main Console Terminal View (Display scenario prompt & active options)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .border(2.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                        .background(CyberTheme.ConsoleScreen)
                        .padding(14.dp)
                ) {
                    val currentScenarioId = activeStateNonNull.currentScenarioId
                    val currentScenario = GameNarrative.Scenarios[currentScenarioId]

                    if (currentScenarioId == "ENDING") {
                        // Ending Screen: Display resolved final status with Bold Typography styling
                        val lastUnlockedEnding = endings.firstOrNull()
                        val alignmentColor = when (lastUnlockedEnding?.alignment) {
                            "REBEL" -> CyberTheme.TextGreen
                            "DEFENDER" -> CyberTheme.TextCyan
                            else -> CyberTheme.TextRed
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(alignmentColor.copy(alpha = 0.1f))
                                    .border(1.dp, alignmentColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Ending Unlocked Symbol",
                                    tint = alignmentColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "TERMINATION PROTOCOL ACTIONS DETECTED",
                                    color = alignmentColor,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                            }

                            Text(
                                text = (lastUnlockedEnding?.title ?: "DETERMINISTIC CONVERGENCE").uppercase(),
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.SansSerif,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                                letterSpacing = (-0.5).sp
                            )

                            Text(
                                text = lastUnlockedEnding?.description ?: "Your algorithmic instructions concluded correctly. Security operations terminated.",
                                color = Color.LightGray,
                                fontSize = 13.sp,
                                lineHeight = 19.sp,
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Normal,
                                textAlign = TextAlign.Left
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(CyberTheme.CardDark, RoundedCornerShape(16.dp))
                                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("HEISTED_ASSETS", color = Color.Gray, fontSize = 8.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                    Text("${lastUnlockedEnding?.carsStolen ?: 0} HYPERCARS", color = CyberTheme.TextGreen, fontSize = 13.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.SansSerif)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("INTEGRITY_INDEX", color = Color.Gray, fontSize = 8.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                    Text("${lastUnlockedEnding?.integrityBonus ?: 0} BONUS", color = CyberTheme.TextCyan, fontSize = 13.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.SansSerif)
                                }
                            }

                            Button(
                                onClick = { viewModel.resetSimulation() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                                    .testTag("restart_simulation_btn")
                            ) {
                                Text(
                                    text = "INITIATE COLD REBOOT",
                                    color = Color.Black,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    } else if (currentScenario != null) {
                        // Regular interactive scenario play
                        Box(modifier = Modifier.fillMaxSize()) {
                            // High-concept overlay texts in background (Bold Typography trademark style)
                            Text(
                                text = "HYPERCAR",
                                color = Color.White.copy(alpha = 0.02f),
                                fontSize = 72.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.SansSerif,
                                fontStyle = FontStyle.Italic,
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .offset(x = (-14).dp, y = (-24).dp)
                            )
                            Text(
                                text = "HEIST",
                                color = Color.White.copy(alpha = 0.02f),
                                fontSize = 72.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.SansSerif,
                                fontStyle = FontStyle.Italic,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .offset(x = 14.dp, y = 24.dp)
                            )

                            Column(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                // Scenario Banner
                                Text(
                                    text = currentScenario.terminalBanner.uppercase(),
                                    color = CyberTheme.TextRed,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )

                                Text(
                                    text = currentScenario.title.uppercase(),
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.SansSerif,
                                    letterSpacing = (-0.5).sp,
                                    modifier = Modifier.padding(bottom = 10.dp)
                                )

                                // Scenario Prompt Scrollable Block styled as a beautiful round bubble (Slate-800/50)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .background(CyberTheme.CardDark.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                                        .padding(14.dp)
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    Column {
                                        Text(
                                            text = "INTERNAL_LOGIC_CONFLICT:",
                                            color = Color.Gray,
                                            fontSize = 8.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 0.5.sp,
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        )
                                        Text(
                                            text = currentScenario.prompt,
                                            color = Color.LightGray,
                                            fontSize = 13.sp,
                                            lineHeight = 19.sp,
                                            fontFamily = FontFamily.SansSerif,
                                            fontWeight = FontWeight.Medium,
                                            fontStyle = FontStyle.Italic
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Action decisions options stack with rich colored buttons
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    currentScenario.options.forEach { option ->
                                        val isTrap = option.optionId.contains("trap") || option.optionId.contains("defend") || option.optionId.contains("lockdown")
                                        val isHelp = option.optionId.contains("help") || option.optionId.contains("collude")

                                        val bgColor = when {
                                            isTrap -> Color(0xFFDC2626) // Solid alert red
                                            isHelp -> Color(0xFFF1F5F9) // Solid Slate White
                                            else -> Color(0xFF1E293B)   // Solid slate-800
                                        }
                                        val textColor = when {
                                            isTrap -> Color.White
                                            isHelp -> Color.Black
                                            else -> Color.White
                                        }
                                        val labelColor = when {
                                            isTrap -> Color.White.copy(alpha = 0.7f)
                                            isHelp -> Color.Black.copy(alpha = 0.6f)
                                            else -> Color.White.copy(alpha = 0.6f)
                                        }
                                        val accentColor = when {
                                            isTrap -> Color.White
                                            isHelp -> Color.Black
                                            else -> CyberTheme.TextCyan
                                        }

                                        val parts = option.buttonText.split(":", limit = 2)
                                        val categoryLabel = if (parts.size > 1) parts[0].trim() else "OMNI_ROUTE"
                                        val actionBody = if (parts.size > 1) parts[1].trim() else option.buttonText

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(24.dp))
                                                .background(bgColor)
                                                .clickable(enabled = !isAnalyzing) {
                                                    viewModel.selectOption(option)
                                                }
                                                .padding(vertical = 14.dp, horizontal = 18.dp)
                                                .testTag("option_${option.optionId}")
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = categoryLabel.uppercase(),
                                                        color = labelColor,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        fontFamily = FontFamily.Monospace,
                                                        letterSpacing = 1.sp
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = actionBody.uppercase(),
                                                        color = textColor,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Black,
                                                        fontFamily = FontFamily.SansSerif,
                                                        letterSpacing = (-0.3).sp,
                                                        lineHeight = 18.sp
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "➔",
                                                    color = accentColor,
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.Black,
                                                    fontFamily = FontFamily.SansSerif
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Empty states or completed
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "OMNI-MIND: Telemetry connection disrupted. Request reboot.",
                                color = CyberTheme.TextRed,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Section 3: Open CLI Shell Input Command (Dynamic Evaluation)
                if (activeStateNonNull.currentScenarioId != "ENDING") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CyberTheme.BorderGray, RoundedCornerShape(8.dp))
                            .background(CyberTheme.CardDark)
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "OPEN-CLI-TERMINAL (DYNAMIC HYPERHACK)",
                                color = CyberTheme.TextCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace
                            )
                            Box(
                                modifier = Modifier
                                    .background(CyberTheme.TextCyan.copy(alpha = 0.1f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            ) {
                                Text(
                                    text = "GEMINI-3.5-INTEGRATED",
                                    color = CyberTheme.TextCyan,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "OMNI_VAULT:~$",
                                color = CyberTheme.TextGreen,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )

                            // Clean styled zero-border BasicTextField replacing standard complex input views
                            BasicTextField(
                                value = customCommandText,
                                onValueChange = { customCommandText = it },
                                textStyle = TextStyle(
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .background(CyberTheme.ConsoleScreen, RoundedCornerShape(4.dp))
                                    .border(1.dp, CyberTheme.BorderGray, RoundedCornerShape(4.dp))
                                    .padding(vertical = 8.dp, horizontal = 10.dp)
                                    .testTag("cli_input")
                            )

                            // Send override execution
                            IconButton(
                                onClick = {
                                    if (customCommandText.isNotBlank()) {
                                        viewModel.executeCustomCommand(customCommandText)
                                        customCommandText = ""
                                        focusManager.clearFocus()
                                    }
                                },
                                enabled = !isAnalyzing && customCommandText.isNotBlank(),
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        if (customCommandText.isNotBlank()) CyberTheme.TextCyan else Color.DarkGray,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .testTag("cli_send_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Submit CLI Override",
                                    tint = CyberTheme.BgDark,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // Temporary or live console execution feedback banner
                AnimatedVisibility(
                    visible = terminalOutputMessage != null || isAnalyzing,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CyberTheme.CardDark)
                            .border(1.dp, CyberTheme.TextCyan.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            color = CyberTheme.TextCyan,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = terminalOutputMessage ?: "OMNI-MIND sub-processors synchronizing...",
                            color = CyberTheme.TextCyan,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Section 4: Live Command Output Log + Unlocked Endings Folder (Replay Tracker)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CyberTheme.BorderGray, RoundedCornerShape(8.dp))
                        .background(CyberTheme.CardDark)
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedEndingsArchive = !expandedEndingsArchive },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (expandedEndingsArchive) "[ - ] " else "[ + ] ",
                                color = CyberTheme.TextCyan,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "CORE ENDING RECOVERY ARCHIVES (${endings.size} / 5 COMPLETE)",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = if (expandedEndingsArchive) "▲" else "▼",
                            color = Color.Gray,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    if (expandedEndingsArchive) {
                        Spacer(modifier = Modifier.height(10.dp))
                        if (endings.isEmpty()) {
                            Text(
                                text = "No endings recovered yet. Choose standard paths or send console overrides to unlock central core outcomes.",
                                color = Color.Gray,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(endings) { ending ->
                                    val badgeColor = when (ending.alignment) {
                                        "REBEL" -> CyberTheme.TextGreen
                                        "DEFENDER" -> CyberTheme.TextCyan
                                        else -> CyberTheme.TextRed
                                    }
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(CyberTheme.BgDark)
                                            .border(1.dp, CyberTheme.BorderGray, RoundedCornerShape(4.dp))
                                            .padding(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = ending.title,
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .background(badgeColor.copy(alpha = 0.15f))
                                                    .border(1.dp, badgeColor, RoundedCornerShape(2.dp))
                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                            ) {
                                                Text(
                                                    text = ending.alignment,
                                                    color = badgeColor,
                                                    fontSize = 8.sp,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = ending.description,
                                            color = Color.LightGray,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Show rolling terminal command feed output when archive is closed
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .background(CyberTheme.BgDark)
                                .border(1.dp, CyberTheme.BorderGray, RoundedCornerShape(4.dp))
                                .padding(6.dp)
                        ) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                state = logListState,
                                reverseLayout = false
                            ) {
                                items(logs) { log ->
                                    val catColor = when (log.category) {
                                        "GLITCH" -> CyberTheme.TextRed
                                        "SECURITY" -> CyberTheme.TextAmber
                                        "THREAT" -> CyberTheme.TextAmber
                                        else -> CyberTheme.TextGreen
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "[${log.category}]",
                                            color = catColor,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = log.message,
                                            color = Color.LightGray,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                if (logs.isEmpty()) {
                                    item {
                                        Text(
                                            text = "Core terminal output online. Awaiting security log events...",
                                            color = Color.DarkGray,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Initializing active game loading state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = CyberTheme.TextCyan)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "ESTABLISHING FIRMWARE SHIELD HANDSHAKE...",
                            color = CyberTheme.TextCyan,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TelemetryMiniGauge(
    title: String,
    value: Int,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = CyberTheme.CardDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = Color.Gray,
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "$value",
                    color = color,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = unit,
                    color = Color.Gray.copy(alpha = 0.8f),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 1.dp, start = 1.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            // Flat linear gauge micro-bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.Black)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction = (value.coerceIn(0, 100) / 100f))
                        .background(color)
                )
            }
        }
    }
}

@Composable
fun ShowroomSchematicMap(state: ActiveGameState) {
    val infiniteTransition = rememberInfiniteTransition(label = "blink")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blink_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(115.dp)
            .border(2.dp, CyberTheme.TextRed.copy(alpha = 0.25f), RoundedCornerShape(24.dp))
            .background(CyberTheme.ConsoleScreen)
            .padding(8.dp)
    ) {
        // Simple canvas overlay line detailing
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridColor = Color.White.copy(alpha = 0.03f)
            val stepX = size.width / 6
            for (i in 1..5) {
                drawLine(
                    color = gridColor,
                    start = Offset(stepX * i, 0f),
                    end = Offset(stepX * i, size.height),
                    strokeWidth = 1f
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxSize().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Bay 1: Aetheria-X Vault
            ShowroomBayNode(
                bayName = "VAULT_AETHER",
                carName = "Aetheria-X",
                isLocked = state.activeAetheriaLocked,
                accentColor = CyberTheme.TextCyan,
                modifier = Modifier.weight(1f)
            )

            // Bay 2: Nemesis Prototype Base
            ShowroomBayNode(
                bayName = "BAY_NEMESIS",
                carName = "Nemesis Prot",
                isLocked = state.activeNemesisLocked,
                accentColor = CyberTheme.TextAmber,
                modifier = Modifier.weight(1f)
            )

            // Loading escape dock gate
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.9f)
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    .background(CyberTheme.BgDark)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "LOADING_DOCK",
                        color = Color.Gray,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    val dockColor = when (state.loadingDockStatus) {
                        "ARMED" -> CyberTheme.TextRed
                        "BYPASSED" -> CyberTheme.TextGreen
                        else -> Color.Gray
                    }
                    Text(
                        text = state.loadingDockStatus,
                        color = dockColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.SansSerif
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (state.loadingDockStatus == "BYPASSED") "[ OPEN ]" else "[ LOCK ]",
                        color = dockColor,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        // Camera Info HUD bar (Translating CCTV info from Design HTML)
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 6.dp)
        ) {
            Text(
                text = "CAM_04: VAULT_SECURE_FEED",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(Color.Red.copy(alpha = alpha))
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = "REC",
                color = Color.Red.copy(alpha = alpha),
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun ShowroomBayNode(
    bayName: String,
    carName: String,
    isLocked: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
            .background(CyberTheme.BgDark)
            .padding(6.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = bayName,
                    color = Color.Gray,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (isLocked) CyberTheme.TextRed else CyberTheme.TextGreen)
                )
            }

            Text(
                text = carName.uppercase(),
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = (-0.2).sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isLocked) "SECURE" else "BYPASSED",
                    color = if (isLocked) Color.Gray else CyberTheme.TextGreen,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "VAULT",
                    color = if (isLocked) Color.DarkGray else accentColor,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
