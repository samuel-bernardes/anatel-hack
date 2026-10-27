package com.example.naomordaaisca.game

enum class MissionId { DIGITAL_OFFERS, PHONE_MESSAGES, GROUP_CHAT_SUPPORT }
enum class AgeBand { EXPLORER_8_10, AGENT_11_14 }
enum class GameScreen { HUB, MISSION, FEEDBACK, MISSION_COMPLETE, INVESTIGATION }
enum class MissionStatus { NOT_STARTED, IN_PROGRESS, AWAITING_INTERACTION, AWAITING_CHOICE, PRESENTING_CONSEQUENCE, COMPLETED }
enum class CharacterId { CAPI, PESCADOR }
enum class CharacterMood { NEUTRAL, THINKING, SURPRISED, EXPLAINING, PRESENTING_OFFER }
enum class InteractableId { COMPUTER, PHONE, CAPI }

data class CharacterCue(val characterId: CharacterId, val mood: CharacterMood)
data class MessageContent(
    val senderIdentity: String,
    val senderDisplayName: String,
    val context: String,
    val text: String,
    val senderCharacter: CharacterId? = null,
)
data class InspectableDetail(val id: String, val label: String, val information: String)
data class MissionTrack(
    val context: String,
    val objective: String,
    val startingStepId: String,
    val steps: List<MissionStep>,
)
data class MissionDefinition(
    val id: MissionId,
    val title: String,
    val interactable: InteractableId,
    val ageTracks: Map<AgeBand, MissionTrack>,
)

sealed interface MissionStep { val id: String }
data class InformationStep(
    override val id: String,
    val heading: String,
    val body: String,
    val details: List<InspectableDetail>,
    val requiredDetailIds: Set<String> = emptySet(),
    val nextStepId: String,
    val minimumDetailsToInspect: Int = requiredDetailIds.size,
) : MissionStep
data class DecisionStep(
    override val id: String,
    val message: MessageContent,
    val capiPrompt: String,
    val choices: List<MissionChoice>,
) : MissionStep
data class MissionChoice(val id: String, val label: String, val outcome: ChoiceOutcome)
data class ChoiceOutcome(
    val consequence: String,
    val educationalFeedback: String,
    val capiMood: CharacterMood,
    val nextStepId: String,
)
data class ConclusionStep(override val id: String, val title: String, val body: String) : MissionStep

data class MadeChoice(val stepId: String, val choiceId: String)
data class MissionSession(
    val missionId: MissionId,
    val ageBand: AgeBand,
    val status: MissionStatus = MissionStatus.NOT_STARTED,
    val currentStepId: String? = null,
    val choicesMade: List<MadeChoice> = emptyList(),
    val revealedDetailIds: Set<String> = emptySet(),
    val currentChoiceId: String? = null,
    val hintRevealed: Boolean = false,
)
data class GameState(
    val screen: GameScreen = GameScreen.HUB,
    val selectedAgeBand: AgeBand = AgeBand.EXPLORER_8_10,
    val activeMission: MissionSession? = null,
    val completedMissionIds: Set<MissionId> = emptySet(),
    val investigation: InvestigationSession? = null,
)
data class GamePresentation(
    val screen: GameScreen,
    val selectedAgeBand: AgeBand,
    val mission: MissionDefinition?,
    val track: MissionTrack?,
    val missionStatus: MissionStatus?,
    val step: MissionStep?,
    val outcome: ChoiceOutcome?,
    val selectedChoiceId: String?,
    val choicesMade: List<MadeChoice>,
    val revealedDetailIds: Set<String>,
    val hintRevealed: Boolean,
    val interactable: InteractableId?,
    val characters: List<CharacterCue>,
    val completedMissionIds: Set<MissionId>,
    val completedMissionCount: Int,
    val missionCount: Int,
    val stepNumber: Int,
    val stepCount: Int,
)

sealed interface GameAction {
    data class SelectAgeBand(val ageBand: AgeBand) : GameAction
    data class OpenInvestigation(val investigationId: MissionId = MissionId.DIGITAL_OFFERS) : GameAction
    data class OpenInvestigationApp(val appId: SimulatedAppId) : GameAction
    data object NavigateBack : GameAction
    data class DiscoverEvidence(val evidenceId: String) : GameAction
    data class UseEvidence(val evidenceId: String) : GameAction
    data class MakeInvestigationChoice(val choiceId: String) : GameAction
    data object ContinueInvestigation : GameAction
    data object RecoverFromInvestigationChoice : GameAction
    data object RequestInvestigationExit : GameAction
    data object RequestInvestigationRestart : GameAction
    data object ConfirmInvestigationExit : GameAction
    data object ConfirmInvestigationRestart : GameAction
    data object CancelInvestigationConfirmation : GameAction
    data class OpenMission(val missionId: MissionId) : GameAction
    data object BeginMission : GameAction
    data object PresentCurrentStep : GameAction
    data class InspectDetail(val detailId: String) : GameAction
    data object AskCapiForHint : GameAction
    data class Choose(val choiceId: String) : GameAction
    data object Continue : GameAction
    data object ReturnToHub : GameAction
    data object ReplayMission : GameAction
}

data class GameProgress(
    val completedMissionIds: Set<MissionId> = emptySet(),
    val activeInvestigation: InvestigationSession? = null,
)
interface GameProgressRepository { fun load(): GameProgress; fun save(progress: GameProgress) }
fun GameState.withProgress(progress: GameProgress): GameState = copy(
    completedMissionIds = progress.completedMissionIds,
    investigation = progress.activeInvestigation,
)
fun GameState.toProgress(): GameProgress = GameProgress(completedMissionIds, investigation)

fun MissionTrack.step(stepId: String?): MissionStep? = steps.firstOrNull { it.id == stepId }
