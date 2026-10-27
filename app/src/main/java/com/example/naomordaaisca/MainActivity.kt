package com.example.naomordaaisca

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.naomordaaisca.data.SharedPreferencesGameProgressRepository
import com.example.naomordaaisca.game.AgeBand
import com.example.naomordaaisca.game.GameAction
import com.example.naomordaaisca.game.GameProgressRepository
import com.example.naomordaaisca.game.GameReducer
import com.example.naomordaaisca.game.GameScreen
import com.example.naomordaaisca.game.GameState
import com.example.naomordaaisca.game.InvestigationSnapshotCodec
import com.example.naomordaaisca.game.MadeChoice
import com.example.naomordaaisca.game.MissionId
import com.example.naomordaaisca.game.MissionSession
import com.example.naomordaaisca.game.MissionStatus
import com.example.naomordaaisca.game.toProgress
import com.example.naomordaaisca.game.withProgress
import com.example.naomordaaisca.ui.hub.HubScreen
import com.example.naomordaaisca.ui.investigation.InvestigationExperience
import com.example.naomordaaisca.ui.legacy.LegacyCompletionScreen
import com.example.naomordaaisca.ui.legacy.LegacyFeedbackScreen
import com.example.naomordaaisca.ui.legacy.LegacyMissionScreen
import com.example.naomordaaisca.ui.theme.NaoMordaAIscaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        // O jogo desenha a própria barra de estado do celular simulado.
        WindowCompat.getInsetsController(window, window.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }
        setContent {
            NaoMordaAIscaTheme { GameApp() }
        }
    }
}

@Composable
private fun GameApp() {
    val context = LocalContext.current
    val progressRepository: GameProgressRepository = remember(context) { SharedPreferencesGameProgressRepository(context) }
    var state by rememberSaveable(stateSaver = gameStateSaver) { mutableStateOf(GameState().withProgress(progressRepository.load())) }
    val presentation = GameReducer.present(state)
    val dispatch: (GameAction) -> Unit = { action ->
        val updated = GameReducer.reduce(state, action)
        if (updated != state) progressRepository.save(updated.toProgress())
        state = updated
    }

    LaunchedEffect(presentation.mission?.id, presentation.missionStatus, presentation.step?.id) {
        when (presentation.missionStatus) {
            MissionStatus.NOT_STARTED -> dispatch(GameAction.BeginMission)
            MissionStatus.IN_PROGRESS -> dispatch(GameAction.PresentCurrentStep)
            else -> Unit
        }
    }
    // A investigação trata o "voltar" dentro do celular simulado.
    BackHandler(enabled = state.screen != GameScreen.HUB && state.screen != GameScreen.INVESTIGATION) {
        dispatch(GameAction.ReturnToHub)
    }

    when (presentation.screen) {
        GameScreen.HUB -> HubScreen(presentation, state.investigation, dispatch)
        GameScreen.MISSION -> LegacyMissionScreen(presentation, dispatch)
        GameScreen.FEEDBACK -> LegacyFeedbackScreen(presentation, dispatch)
        GameScreen.MISSION_COMPLETE -> LegacyCompletionScreen(presentation, dispatch)
        GameScreen.INVESTIGATION -> state.investigation?.let { InvestigationExperience(it, dispatch) }
    }
}

private val gameStateSaver: Saver<GameState, Any> = mapSaver(
    save = { state ->
        mapOf(
            "screen" to state.screen.name,
            "age" to state.selectedAgeBand.name,
            "mission" to state.activeMission?.missionId?.name,
            "missionAge" to state.activeMission?.ageBand?.name,
            "status" to state.activeMission?.status?.name,
            "step" to state.activeMission?.currentStepId,
            "choice" to state.activeMission?.currentChoiceId,
            "hint" to (state.activeMission?.hintRevealed ?: false),
            "details" to state.activeMission?.revealedDetailIds.orEmpty().toList(),
            "choices" to state.activeMission?.choicesMade.orEmpty().map { "${it.stepId}|${it.choiceId}" },
            "completed" to state.completedMissionIds.map { it.name },
            "investigation" to state.investigation?.let(InvestigationSnapshotCodec::encode),
        )
    },
    restore = { saved ->
        fun <T : Enum<T>> enumValue(enumClass: Class<T>, key: String, fallback: T): T =
            (saved[key] as? String)?.let { runCatching { java.lang.Enum.valueOf(enumClass, it) }.getOrNull() } ?: fallback
        val missionId = (saved["mission"] as? String)?.let { runCatching { MissionId.valueOf(it) }.getOrNull() }
        val screen = enumValue(GameScreen::class.java, "screen", GameScreen.HUB)
        val choices = (saved["choices"] as? List<*>).orEmpty().filterIsInstance<String>().mapNotNull { record ->
            val pair = record.split('|', limit = 2)
            if (pair.size == 2) MadeChoice(pair[0], pair[1]) else null
        }
        val completed = (saved["completed"] as? List<*>).orEmpty().filterIsInstance<String>()
            .mapNotNull { runCatching { MissionId.valueOf(it) }.getOrNull() }.toSet()
        val active = missionId?.let { id ->
            MissionSession(
                missionId = id,
                ageBand = enumValue(AgeBand::class.java, "missionAge", AgeBand.EXPLORER_8_10),
                status = enumValue(MissionStatus::class.java, "status", MissionStatus.NOT_STARTED),
                currentStepId = saved["step"] as? String,
                choicesMade = choices,
                revealedDetailIds = (saved["details"] as? List<*>).orEmpty().filterIsInstance<String>().toSet(),
                currentChoiceId = saved["choice"] as? String,
                hintRevealed = saved["hint"] as? Boolean ?: false,
            )
        }
        val investigation = InvestigationSnapshotCodec.decode(saved["investigation"] as? String)
        GameReducer.recover(GameState(
            screen = screen,
            selectedAgeBand = enumValue(AgeBand::class.java, "age", AgeBand.EXPLORER_8_10),
            activeMission = active,
            completedMissionIds = completed,
            investigation = investigation,
        ))
    },
)
