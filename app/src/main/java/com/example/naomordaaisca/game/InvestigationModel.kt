package com.example.naomordaaisca.game

/** Device/app identifiers are content-facing; presentation can later map them to assets. */
enum class SimulatedDeviceId { PHONE, COMPUTER }
enum class SimulatedAppId { MESSAGES, BROWSER }
enum class EvidenceStatus { AVAILABLE, DISCOVERED, USED }
enum class InvestigationStage { INVESTIGATING, PRESENTING_CONSEQUENCE, RECOVERY, COMPLETED }
enum class InvestigationDialog { CONFIRM_EXIT, CONFIRM_RESTART }

data class SimulatedApp(val id: SimulatedAppId, val device: SimulatedDeviceId, val title: String)

object SimulatedApps {
    val all = listOf(
        SimulatedApp(SimulatedAppId.MESSAGES, SimulatedDeviceId.PHONE, "Mensagens"),
        SimulatedApp(SimulatedAppId.BROWSER, SimulatedDeviceId.COMPUTER, "Navegador"),
    )

    fun get(id: SimulatedAppId): SimulatedApp = all.first { it.id == id }
}

data class InvestigationClue(
    val id: String,
    val appId: SimulatedAppId,
    val title: String,
    val content: String,
)

data class InvestigationChoice(
    val id: String,
    val label: String,
    val consequence: String,
    val feedback: String,
    val capiMood: CharacterMood = CharacterMood.EXPLAINING,
    val recoveryRequired: Boolean = false,
)

data class InvestigationTrack(
    val context: String,
    val objective: String,
    val sender: String,
    val message: String,
    val clues: List<InvestigationClue>,
    val evidenceRequiredForDecision: Set<String>,
    val choices: List<InvestigationChoice>,
    val conclusion: String,
)

data class InvestigationDefinition(
    val id: MissionId,
    val title: String,
    val ageTracks: Map<AgeBand, InvestigationTrack>,
)

data class InvestigationSession(
    val missionId: MissionId = MissionId.DIGITAL_OFFERS,
    val ageBand: AgeBand,
    val stage: InvestigationStage = InvestigationStage.INVESTIGATING,
    val navigation: InvestigationNavigation = InvestigationNavigation(),
    val discoveredEvidenceIds: Set<String> = emptySet(),
    val usedEvidenceIds: Set<String> = emptySet(),
    val choicesMade: List<String> = emptyList(),
    val selectedChoiceId: String? = null,
    val dialog: InvestigationDialog? = null,
)

/** App navigation is kept separate from the investigation's narrative stage. */
data class InvestigationNavigation(
    val currentApp: SimulatedAppId = SimulatedAppId.MESSAGES,
    val appHistory: List<SimulatedAppId> = emptyList(),
)

