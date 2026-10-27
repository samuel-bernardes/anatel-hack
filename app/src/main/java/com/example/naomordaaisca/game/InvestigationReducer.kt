package com.example.naomordaaisca.game

object InvestigationReducer {
    /** Decision availability is a game rule; presentation may render this list but must not recreate its gate. */
    fun availableChoices(session: InvestigationSession): List<InvestigationChoice> {
        if (!isValid(session) || session.stage != InvestigationStage.INVESTIGATING ||
            session.dialog != null || session.navigation.currentApp != SimulatedAppId.MESSAGES
        ) return emptyList()
        val track = InvestigationCatalog.track(session.missionId, session.ageBand) ?: return emptyList()
        return track.choices.takeIf { session.usedEvidenceIds.containsAll(track.evidenceRequiredForDecision) }.orEmpty()
    }

    fun isValid(session: InvestigationSession): Boolean {
        val definition = InvestigationCatalog.definition(session.missionId) ?: return false
        val track = InvestigationCatalog.track(session.missionId, session.ageBand) ?: return false
        if (InvestigationCatalog.validate(definition).isNotEmpty()) return false
        val clueIds = track.clues.map { it.id }.toSet()
        if (!clueIds.containsAll(session.discoveredEvidenceIds) || !clueIds.containsAll(session.usedEvidenceIds)) return false
        if (!session.discoveredEvidenceIds.containsAll(session.usedEvidenceIds)) return false
        val choiceIds = track.choices.map { it.id }.toSet()
        if (!choiceIds.containsAll(session.choicesMade) ||
            session.choicesMade.distinct().size != session.choicesMade.size ||
            (session.selectedChoiceId != null && session.choicesMade.lastOrNull() != session.selectedChoiceId)) return false
        if (!clueIds.containsAll(track.evidenceRequiredForDecision) || track.choices.isEmpty() ||
            track.choices.map { it.id }.distinct().size != track.choices.size
        ) return false
        if ((session.choicesMade.isNotEmpty() || session.selectedChoiceId != null) &&
            !session.usedEvidenceIds.containsAll(track.evidenceRequiredForDecision)) return false
        if (session.selectedChoiceId != null && track.choices.none { it.id == session.selectedChoiceId }) return false
        val navigation = session.navigation
        if (navigation.appHistory.size > 1 || navigation.appHistory.lastOrNull() == navigation.currentApp) return false
        if (session.dialog == InvestigationDialog.CONFIRM_EXIT && session.stage != InvestigationStage.INVESTIGATING) return false
        when (session.stage) {
            InvestigationStage.INVESTIGATING -> if (session.selectedChoiceId != null) return false
            InvestigationStage.PRESENTING_CONSEQUENCE -> if (session.selectedChoiceId == null) return false
            InvestigationStage.RECOVERY -> if (track.choices.firstOrNull { it.id == session.selectedChoiceId }?.recoveryRequired != true) return false
            InvestigationStage.COMPLETED -> if (session.choicesMade.isEmpty() || session.selectedChoiceId != null) return false
        }
        return true
    }

    fun reduce(session: InvestigationSession, action: GameAction): InvestigationSession {
        if (!isValid(session)) return session
        val track = InvestigationCatalog.track(session.missionId, session.ageBand) ?: return session
        return when (action) {
            is GameAction.OpenInvestigationApp -> {
                val navigation = session.navigation
                if (session.stage != InvestigationStage.INVESTIGATING || session.dialog != null || action.appId == navigation.currentApp) session
                else {
                    val previousIndex = navigation.appHistory.indexOfLast { it == action.appId }
                    val history = if (previousIndex >= 0) navigation.appHistory.take(previousIndex) else navigation.appHistory + navigation.currentApp
                    session.copy(navigation = navigation.copy(currentApp = action.appId, appHistory = history))
                }
            }
            GameAction.NavigateBack -> when {
                session.dialog != null -> session.copy(dialog = null)
                session.stage == InvestigationStage.INVESTIGATING -> {
                    val navigation = session.navigation
                    val previous = navigation.appHistory.lastOrNull()
                    if (previous == null) session.copy(dialog = InvestigationDialog.CONFIRM_EXIT)
                    else session.copy(navigation = navigation.copy(currentApp = previous, appHistory = navigation.appHistory.dropLast(1)))
                }
                session.stage == InvestigationStage.PRESENTING_CONSEQUENCE || session.stage == InvestigationStage.RECOVERY -> session.copy(
                    stage = InvestigationStage.INVESTIGATING, selectedChoiceId = null,
                )
                else -> session
            }
            is GameAction.DiscoverEvidence -> {
                val clue = track.clues.firstOrNull { it.id == action.evidenceId } ?: return session
                if (session.stage != InvestigationStage.INVESTIGATING || session.dialog != null || session.navigation.currentApp != clue.appId) session
                else session.copy(discoveredEvidenceIds = session.discoveredEvidenceIds + clue.id)
            }
            is GameAction.UseEvidence -> {
                val clue = track.clues.firstOrNull { it.id == action.evidenceId } ?: return session
                if (session.stage != InvestigationStage.INVESTIGATING || session.dialog != null || session.navigation.currentApp != clue.appId || clue.id !in session.discoveredEvidenceIds) session
                else session.copy(usedEvidenceIds = session.usedEvidenceIds + clue.id)
            }
            is GameAction.MakeInvestigationChoice -> {
                if (availableChoices(session).none { it.id == action.choiceId }) session
                else session.copy(
                    stage = InvestigationStage.PRESENTING_CONSEQUENCE,
                    selectedChoiceId = action.choiceId,
                    choicesMade = if (action.choiceId in session.choicesMade) session.choicesMade else session.choicesMade + action.choiceId,
                )
            }
            GameAction.ContinueInvestigation -> if (session.stage == InvestigationStage.PRESENTING_CONSEQUENCE && session.dialog == null) {
                val selected = track.choices.firstOrNull { it.id == session.selectedChoiceId } ?: return session
                session.copy(
                    stage = if (selected.recoveryRequired) InvestigationStage.RECOVERY else InvestigationStage.COMPLETED,
                    selectedChoiceId = if (selected.recoveryRequired) session.selectedChoiceId else null,
                )
            } else session
            GameAction.RecoverFromInvestigationChoice -> if (session.stage == InvestigationStage.RECOVERY && session.dialog == null) session.copy(stage = InvestigationStage.COMPLETED, selectedChoiceId = null) else session
            GameAction.RequestInvestigationExit -> if (session.stage == InvestigationStage.INVESTIGATING && session.dialog == null) session.copy(dialog = InvestigationDialog.CONFIRM_EXIT) else session
            GameAction.RequestInvestigationRestart -> if (session.dialog == null) session.copy(dialog = InvestigationDialog.CONFIRM_RESTART) else session
            GameAction.ConfirmInvestigationRestart -> if (session.dialog == InvestigationDialog.CONFIRM_RESTART) {
                InvestigationSession(missionId = session.missionId, ageBand = session.ageBand)
            } else session
            GameAction.CancelInvestigationConfirmation -> if (session.dialog != null) session.copy(dialog = null) else session
            GameAction.ConfirmInvestigationExit -> session
            else -> session
        }.takeIf(::isValid) ?: session
    }
}

/** Versioned, dependency-free persistence format. Unknown or malformed values are rejected. */
object InvestigationSnapshotCodec {
    fun encode(session: InvestigationSession): String? {
        if (!InvestigationReducer.isValid(session)) return null
        val fields = listOf(
            "2", session.missionId.name, session.ageBand.name, session.stage.name, session.navigation.currentApp.name,
            session.navigation.appHistory.joinToString(",") { it.name },
            session.discoveredEvidenceIds.sorted().joinToString(","), session.usedEvidenceIds.sorted().joinToString(","),
            session.choicesMade.joinToString(","), session.selectedChoiceId.orEmpty(), session.dialog?.name.orEmpty(),
        )
        return fields.joinToString("|")
    }

    fun decode(value: String?): InvestigationSession? = runCatching {
        val fields = value?.split('|') ?: return null
        require(fields.size == 11)
        when (fields[0]) {
            "2" -> decodeCurrent(fields)
            "1" -> decodeLegacy(fields)
            else -> null
        }
    }.getOrNull()

    private fun decodeCurrent(fields: List<String>): InvestigationSession? = runCatching {
        fun <T : Enum<T>> enumField(index: Int, type: Class<T>): T = java.lang.Enum.valueOf(type, fields[index])
        fun enumList(index: Int): List<SimulatedAppId> = fields[index].takeIf(String::isNotEmpty)?.split(',')?.map { SimulatedAppId.valueOf(it) }.orEmpty()
        fun set(index: Int) = fields[index].takeIf(String::isNotEmpty)?.split(',')?.toSet().orEmpty()
        val session = InvestigationSession(
            missionId = MissionId.valueOf(fields[1]), ageBand = enumField(2, AgeBand::class.java),
            stage = enumField(3, InvestigationStage::class.java),
            navigation = InvestigationNavigation(currentApp = enumField(4, SimulatedAppId::class.java), appHistory = enumList(5)),
            discoveredEvidenceIds = set(6), usedEvidenceIds = set(7),
            choicesMade = enumListStrings(fields[8]), selectedChoiceId = fields[9].ifEmpty { null },
            dialog = fields[10].ifEmpty { null }?.let { InvestigationDialog.valueOf(it) },
        )
        session.takeIf(InvestigationReducer::isValid)
    }.getOrNull()

    private fun decodeLegacy(fields: List<String>): InvestigationSession? = runCatching {
        fun <T : Enum<T>> enumField(index: Int, type: Class<T>): T = java.lang.Enum.valueOf(type, fields[index])
        fun enumList(index: Int): List<SimulatedAppId> = fields[index].takeIf(String::isNotEmpty)?.split(',')?.map { SimulatedAppId.valueOf(it) }.orEmpty()
        fun set(index: Int) = fields[index].takeIf(String::isNotEmpty)?.split(',')?.toSet().orEmpty()
        val oldReturn = fields[10].ifEmpty { null }
        val stageAndDialog = when (fields[3]) {
            "MESSAGES", "BROWSER" -> InvestigationStage.INVESTIGATING to null
            "FEEDBACK" -> InvestigationStage.PRESENTING_CONSEQUENCE to null
            "RECOVERY" -> InvestigationStage.RECOVERY to null
            "COMPLETE" -> InvestigationStage.COMPLETED to null
            "EXIT_CONFIRMATION" -> InvestigationStage.INVESTIGATING to InvestigationDialog.CONFIRM_EXIT
            "RESTART_CONFIRMATION" -> (stageForLegacyReturn(oldReturn) ?: return null) to InvestigationDialog.CONFIRM_RESTART
            else -> return null
        }
        val choices = enumListStrings(fields[8])
        val selected = fields[9].ifEmpty { null }
        val normalizedSelected = if (stageAndDialog.second == InvestigationDialog.CONFIRM_EXIT) null else selected
        val session = InvestigationSession(
            missionId = MissionId.valueOf(fields[1]), ageBand = enumField(2, AgeBand::class.java),
            stage = stageAndDialog.first,
            navigation = InvestigationNavigation(
                currentApp = enumField(4, SimulatedAppId::class.java),
                appHistory = enumList(5),
            ),
            discoveredEvidenceIds = set(6), usedEvidenceIds = set(7), choicesMade = choices,
            selectedChoiceId = normalizedSelected, dialog = stageAndDialog.second,
        )
        session.takeIf(InvestigationReducer::isValid)
    }.getOrNull()

    private fun stageForLegacyReturn(location: String?): InvestigationStage? = when (location) {
        "MESSAGES", "BROWSER" -> InvestigationStage.INVESTIGATING
        "FEEDBACK" -> InvestigationStage.PRESENTING_CONSEQUENCE
        "RECOVERY" -> InvestigationStage.RECOVERY
        "COMPLETE" -> InvestigationStage.COMPLETED
        else -> null
    }

    private fun enumListStrings(value: String): List<String> = value.takeIf(String::isNotEmpty)?.split(',').orEmpty()
}
