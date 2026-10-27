package com.example.naomordaaisca.game

/** Pure mission and session rules. No Android, Compose, storage, or graphics dependencies. */
object GameReducer {
    fun reduce(state: GameState, action: GameAction): GameState {
        val safe = recover(state)
        if (safe != state) return safe
        if (action is GameAction.OpenInvestigationApp && state.screen == GameScreen.HUB && state.investigation != null) {
            val opened = openInvestigation(state, state.investigation.missionId)
            return reduceInvestigation(opened, action)
        }
        if (state.screen == GameScreen.INVESTIGATION) return reduceInvestigation(state, action)
        return when (action) {
            is GameAction.SelectAgeBand -> if (state.screen == GameScreen.HUB) state.copy(selectedAgeBand = action.ageBand) else state
            is GameAction.OpenInvestigation -> openInvestigation(state, action.investigationId)
            is GameAction.OpenInvestigationApp, GameAction.NavigateBack, is GameAction.DiscoverEvidence,
            is GameAction.UseEvidence, is GameAction.MakeInvestigationChoice, GameAction.ContinueInvestigation,
            GameAction.RecoverFromInvestigationChoice, GameAction.RequestInvestigationExit,
            GameAction.RequestInvestigationRestart, GameAction.ConfirmInvestigationExit,
            GameAction.ConfirmInvestigationRestart, GameAction.CancelInvestigationConfirmation -> state
            is GameAction.OpenMission -> open(state, action.missionId)
            GameAction.BeginMission -> begin(state)
            GameAction.PresentCurrentStep -> presentStep(state)
            is GameAction.InspectDetail -> inspect(state, action.detailId)
            GameAction.AskCapiForHint -> updateActive(state) { active ->
                if (state.screen == GameScreen.MISSION && active.status == MissionStatus.AWAITING_CHOICE) active.copy(hintRevealed = true) else active
            }
            is GameAction.Choose -> choose(state, action.choiceId)
            GameAction.Continue -> continueMission(state)
            GameAction.ReturnToHub -> state.copy(screen = GameScreen.HUB, activeMission = null)
            GameAction.ReplayMission -> replay(state)
        }
    }

    fun recover(state: GameState): GameState {
        if (isConsistent(state)) return state
        val knownIds = MissionCatalog.missions.map { it.id }.toSet()
        return state.copy(screen = GameScreen.HUB, activeMission = null,
            completedMissionIds = state.completedMissionIds intersect knownIds,
            investigation = state.investigation?.takeIf { isInvestigationConsistent(it, state.completedMissionIds intersect knownIds) })
    }

    fun isConsistent(state: GameState): Boolean {
        if (!MissionCatalog.missions.map { it.id }.toSet().containsAll(state.completedMissionIds)) return false
        if (state.screen == GameScreen.HUB) return state.activeMission == null &&
            (state.investigation == null || isInvestigationConsistent(state.investigation, state.completedMissionIds))
        if (state.screen == GameScreen.INVESTIGATION) return state.activeMission == null &&
            state.investigation?.let { isInvestigationConsistent(it, state.completedMissionIds) } == true
        val active = state.activeMission ?: return false
        if (state.investigation != null && !InvestigationReducer.isValid(state.investigation)) return false
        val mission = MissionCatalog.missions.firstOrNull { it.id == active.missionId } ?: return false
        if (!MissionValidator.isValid(mission)) return false
        val track = mission.ageTracks[active.ageBand] ?: return false
        val current = track.step(active.currentStepId)
        if (active.status == MissionStatus.NOT_STARTED) {
            if (active.currentStepId != null || active.choicesMade.isNotEmpty() || active.revealedDetailIds.isNotEmpty() || active.currentChoiceId != null) return false
        } else if (current == null) return false

        if (active.choicesMade.map { it.stepId }.distinct().size != active.choicesMade.size) return false
        if (active.choicesMade.any { record ->
                val step = track.step(record.stepId) as? DecisionStep ?: return@any true
                step.choices.none { it.id == record.choiceId }
            }
        ) return false

        val knownDetailIds = track.steps.filterIsInstance<InformationStep>().flatMap { it.details }.map { it.id }.toSet()
        if (!knownDetailIds.containsAll(active.revealedDetailIds)) return false
        if (active.status != MissionStatus.AWAITING_CHOICE && active.hintRevealed) return false
        when (active.status) {
            MissionStatus.NOT_STARTED -> Unit
            MissionStatus.IN_PROGRESS -> if (active.currentChoiceId != null) return false
            MissionStatus.AWAITING_INTERACTION -> if (current !is InformationStep || active.currentChoiceId != null) return false
            MissionStatus.AWAITING_CHOICE -> if (current !is DecisionStep || active.currentChoiceId != null) return false
            MissionStatus.PRESENTING_CONSEQUENCE -> {
                if (current !is DecisionStep || active.currentChoiceId == null ||
                    active.choicesMade.lastOrNull() != MadeChoice(current.id, active.currentChoiceId)
                ) return false
            }
            MissionStatus.COMPLETED -> {
                if (current !is ConclusionStep || active.currentChoiceId != null || active.missionId !in state.completedMissionIds) return false
            }
        }
        return when (state.screen) {
            GameScreen.HUB -> false
            GameScreen.MISSION -> active.status in setOf(MissionStatus.NOT_STARTED, MissionStatus.IN_PROGRESS, MissionStatus.AWAITING_INTERACTION, MissionStatus.AWAITING_CHOICE)
            GameScreen.FEEDBACK -> active.status == MissionStatus.PRESENTING_CONSEQUENCE
            GameScreen.MISSION_COMPLETE -> active.status == MissionStatus.COMPLETED
            GameScreen.INVESTIGATION -> false
        }
    }

    fun present(state: GameState): GamePresentation {
        val safe = recover(state)
        val active = safe.activeMission
        val mission = active?.let { MissionCatalog.missions.firstOrNull { definition -> definition.id == it.missionId } }
        val track = active?.let { mission?.ageTracks?.get(it.ageBand) }
        val step = active?.let { track?.step(it.currentStepId) }
        val selected = (step as? DecisionStep)?.choices?.firstOrNull { it.id == active?.currentChoiceId }
        val outcome = if (active?.status == MissionStatus.PRESENTING_CONSEQUENCE) selected?.outcome else null
        val characters = when (safe.screen) {
            GameScreen.HUB -> listOf(CharacterCue(CharacterId.CAPI, CharacterMood.NEUTRAL))
            GameScreen.MISSION -> buildList {
                add(CharacterCue(CharacterId.CAPI, if (active?.hintRevealed == true) CharacterMood.EXPLAINING else CharacterMood.THINKING))
                ((step as? DecisionStep)?.message?.senderCharacter)?.let { add(CharacterCue(it, CharacterMood.PRESENTING_OFFER)) }
            }
            GameScreen.FEEDBACK -> listOf(CharacterCue(CharacterId.CAPI, outcome?.capiMood ?: CharacterMood.EXPLAINING))
            GameScreen.MISSION_COMPLETE -> listOf(CharacterCue(CharacterId.CAPI, CharacterMood.EXPLAINING))
            GameScreen.INVESTIGATION -> listOf(CharacterCue(CharacterId.CAPI, CharacterMood.THINKING))
        }
        val index = track?.steps?.indexOfFirst { it.id == step?.id } ?: -1
        return GamePresentation(
            screen = safe.screen, selectedAgeBand = safe.selectedAgeBand, mission = mission, track = track,
            missionStatus = active?.status, step = step, outcome = outcome,
            selectedChoiceId = active?.currentChoiceId, choicesMade = active?.choicesMade.orEmpty(),
            revealedDetailIds = active?.revealedDetailIds.orEmpty(), hintRevealed = active?.hintRevealed ?: false,
            interactable = if (safe.screen == GameScreen.MISSION || safe.screen == GameScreen.FEEDBACK) mission?.interactable else null,
            characters = characters, completedMissionIds = safe.completedMissionIds,
            completedMissionCount = safe.completedMissionIds.size, missionCount = MissionCatalog.missions.size,
            stepNumber = (index + 1).coerceAtLeast(0), stepCount = track?.steps?.size ?: 0,
        )
    }

    private fun open(state: GameState, id: MissionId): GameState {
        if (state.screen != GameScreen.HUB) return state
        val mission = MissionCatalog.missions.firstOrNull { it.id == id } ?: return state
        if (!MissionValidator.isValid(mission)) return state
        return state.copy(screen = GameScreen.MISSION, activeMission = MissionSession(id, state.selectedAgeBand))
    }

    private fun openInvestigation(state: GameState, investigationId: MissionId): GameState {
        if (state.screen != GameScreen.HUB) return state
        val definition = InvestigationCatalog.definition(investigationId) ?: return state
        if (InvestigationCatalog.validate(definition).isNotEmpty()) return state
        val existing = state.investigation?.takeIf(InvestigationReducer::isValid)
        if (existing != null && existing.missionId != investigationId) return state
        val session = existing ?: InvestigationSession(missionId = investigationId, ageBand = state.selectedAgeBand)
        return state.copy(screen = GameScreen.INVESTIGATION, activeMission = null, investigation = session)
    }

    private fun isInvestigationConsistent(session: InvestigationSession, completed: Set<MissionId>): Boolean =
        InvestigationReducer.isValid(session) &&
            (session.stage != InvestigationStage.COMPLETED || session.missionId in completed)

    private fun reduceInvestigation(state: GameState, action: GameAction): GameState {
        val session = state.investigation ?: return recover(state.copy(screen = GameScreen.HUB))
        if (action == GameAction.NavigateBack && session.stage == InvestigationStage.COMPLETED) {
            return state.copy(screen = GameScreen.HUB)
        }
        if (action == GameAction.ReturnToHub) {
            if (session.stage == InvestigationStage.COMPLETED) return state.copy(screen = GameScreen.HUB)
            if (session.dialog != null) return state
            return state.copy(investigation = session.copy(
                stage = InvestigationStage.INVESTIGATING,
                selectedChoiceId = null,
                dialog = InvestigationDialog.CONFIRM_EXIT,
            ))
        }
        val updated = when (action) {
            GameAction.ConfirmInvestigationExit -> {
                if (session.stage == InvestigationStage.COMPLETED) return state.copy(screen = GameScreen.HUB)
                if (session.dialog != InvestigationDialog.CONFIRM_EXIT) return state
                return state.copy(screen = GameScreen.HUB, investigation = null)
            }
            else -> InvestigationReducer.reduce(session, action)
        }
        val completed = if (session.stage != InvestigationStage.COMPLETED && updated.stage == InvestigationStage.COMPLETED)
            state.completedMissionIds + session.missionId else state.completedMissionIds
        return if (updated == session && completed == state.completedMissionIds) state
        else state.copy(investigation = updated, completedMissionIds = completed)
    }

    private fun begin(state: GameState): GameState {
        val active = state.activeMission ?: return state
        if (state.screen != GameScreen.MISSION || active.status != MissionStatus.NOT_STARTED) return state
        val mission = MissionCatalog.mission(active.missionId)
        val start = mission.ageTracks[active.ageBand]?.startingStepId ?: return recover(state)
        return state.copy(activeMission = active.copy(status = MissionStatus.IN_PROGRESS, currentStepId = start))
    }

    private fun presentStep(state: GameState): GameState {
        val active = state.activeMission ?: return state
        if (state.screen != GameScreen.MISSION || active.status != MissionStatus.IN_PROGRESS) return state
        val step = currentStep(active) ?: return recover(state)
        return when (step) {
            is InformationStep -> state.copy(activeMission = active.copy(status = MissionStatus.AWAITING_INTERACTION))
            is DecisionStep -> state.copy(activeMission = active.copy(status = MissionStatus.AWAITING_CHOICE))
            is ConclusionStep -> state.copy(
                screen = GameScreen.MISSION_COMPLETE,
                activeMission = active.copy(status = MissionStatus.COMPLETED, currentChoiceId = null, hintRevealed = false),
                completedMissionIds = state.completedMissionIds + active.missionId,
            )
        }
    }

    private fun inspect(state: GameState, detailId: String): GameState {
        val active = state.activeMission ?: return state
        if (state.screen != GameScreen.MISSION || active.status != MissionStatus.AWAITING_INTERACTION) return state
        val step = currentStep(active) as? InformationStep ?: return recover(state)
        if (step.details.none { it.id == detailId }) return state
        return state.copy(activeMission = active.copy(revealedDetailIds = active.revealedDetailIds + detailId))
    }

    private fun choose(state: GameState, choiceId: String): GameState {
        val active = state.activeMission ?: return state
        if (state.screen != GameScreen.MISSION || active.status != MissionStatus.AWAITING_CHOICE) return state
        val step = currentStep(active) as? DecisionStep ?: return recover(state)
        if (step.choices.none { it.id == choiceId }) return state
        return state.copy(
            screen = GameScreen.FEEDBACK,
            activeMission = active.copy(status = MissionStatus.PRESENTING_CONSEQUENCE, choicesMade = active.choicesMade + MadeChoice(step.id, choiceId), currentChoiceId = choiceId, hintRevealed = false),
        )
    }

    private fun continueMission(state: GameState): GameState {
        val active = state.activeMission ?: return state
        val current = currentStep(active) ?: return recover(state)
        val nextStepId = when {
            state.screen == GameScreen.MISSION && active.status == MissionStatus.AWAITING_INTERACTION && current is InformationStep -> {
                if (!active.revealedDetailIds.containsAll(current.requiredDetailIds) ||
                    current.details.count { it.id in active.revealedDetailIds } < current.minimumDetailsToInspect
                ) return state
                current.nextStepId
            }
            state.screen == GameScreen.FEEDBACK && active.status == MissionStatus.PRESENTING_CONSEQUENCE && current is DecisionStep -> {
                val choiceId = active.currentChoiceId ?: return recover(state)
                if (active.choicesMade.lastOrNull() != MadeChoice(current.id, choiceId)) return recover(state)
                current.choices.firstOrNull { it.id == choiceId }?.outcome?.nextStepId ?: return recover(state)
            }
            else -> return state
        }
        val track = MissionCatalog.mission(active.missionId).ageTracks[active.ageBand] ?: return recover(state)
        if (track.step(nextStepId) == null) return recover(state)
        return state.copy(screen = GameScreen.MISSION, activeMission = active.copy(
            status = MissionStatus.IN_PROGRESS, currentStepId = nextStepId,
            currentChoiceId = null, hintRevealed = false,
        ))
    }

    private fun replay(state: GameState): GameState {
        val active = state.activeMission ?: return state
        if (state.screen != GameScreen.MISSION_COMPLETE || active.status != MissionStatus.COMPLETED) return state
        return state.copy(screen = GameScreen.MISSION, activeMission = MissionSession(active.missionId, active.ageBand))
    }

    private fun currentStep(active: MissionSession): MissionStep? =
        MissionCatalog.missions.firstOrNull { it.id == active.missionId }?.ageTracks?.get(active.ageBand)?.step(active.currentStepId)

    private inline fun updateActive(state: GameState, transform: (MissionSession) -> MissionSession): GameState {
        val active = state.activeMission ?: return state
        val updated = transform(active)
        return if (updated == active) state else state.copy(activeMission = updated)
    }
}
