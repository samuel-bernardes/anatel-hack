package com.example.naomordaaisca.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameReducerTest {
    @Test
    fun catalogHasCompleteValidTracksForBothAgeBands() {
        MissionCatalog.missions.forEach { mission ->
            assertTrue(MissionValidator.validate(mission).joinToString(), MissionValidator.isValid(mission))
            assertEquals(AgeBand.entries.toSet(), mission.ageTracks.keys)
            mission.ageTracks.values.forEach { track -> assertTrue(track.steps.any { it is ConclusionStep }) }
        }
    }

    @Test
    fun eachMissionProvidesDistinctContextAndDecisionSupportForBothAgeBands() {
        MissionCatalog.missions.forEach { mission ->
            val explorer = mission.ageTracks.getValue(AgeBand.EXPLORER_8_10)
            val agent = mission.ageTracks.getValue(AgeBand.AGENT_11_14)
            assertNotEquals(explorer.context, agent.context)
            assertNotEquals(explorer.objective, agent.objective)
            val explorerChoices = explorer.steps.filterIsInstance<DecisionStep>().first().choices.map { it.label }
            val agentChoices = agent.steps.filterIsInstance<DecisionStep>().first().choices.map { it.label }
            assertNotEquals(explorerChoices, agentChoices)
        }
    }

    @Test
    fun validatorFindsMissingReferencesUnreachableNodesAndIncompleteContent() {
        val mission = MissionCatalog.mission(MissionId.DIGITAL_OFFERS)
        val track = mission.ageTracks.getValue(AgeBand.EXPLORER_8_10)
        val brokenTrack = track.copy(steps = track.steps + InformationStep("unreachable", "", "", emptyList(), nextStepId = "missing"))
        val broken = mission.copy(ageTracks = mission.ageTracks + (AgeBand.EXPLORER_8_10 to brokenTrack))
        val problems = MissionValidator.validate(broken).joinToString()
        assertTrue(problems.contains("inalcançáveis"))
        assertTrue(problems.contains("não existe"))
        assertTrue(problems.contains("incompleta") || problems.contains("sem título"))
    }

    @Test
    fun ageBandIsSelectedBeforeOpeningAndHasItsOwnTrack() {
        var state = GameReducer.reduce(GameState(), GameAction.SelectAgeBand(AgeBand.AGENT_11_14))
        state = GameReducer.reduce(state, GameAction.OpenMission(MissionId.DIGITAL_OFFERS))
        assertEquals(AgeBand.AGENT_11_14, state.activeMission?.ageBand)
        state = GameReducer.reduce(state, GameAction.BeginMission)
        state = GameReducer.reduce(state, GameAction.PresentCurrentStep)
        assertEquals("agent-discovery", GameReducer.present(state).step?.id)
        assertTrue(GameReducer.present(state).stepCount > 0)
    }

    @Test
    fun missionCannotBeAnsweredBeforeInvestigationAndRequiresOnlySomeUsefulDetails() {
        var state = openingMission(MissionId.DIGITAL_OFFERS)
        assertEquals(MissionStatus.AWAITING_INTERACTION, state.activeMission?.status)
        assertEquals("game-discovery", GameReducer.present(state).step?.id)
        state = GameReducer.reduce(state, GameAction.Continue)
        state = GameReducer.reduce(state, GameAction.PresentCurrentStep)
        assertEquals("game-investigation", GameReducer.present(state).step?.id)
        assertEquals(state, GameReducer.reduce(state, GameAction.Choose("verify-launcher")))
        assertEquals(state, GameReducer.reduce(state, GameAction.Continue))
        state = GameReducer.reduce(state, GameAction.InspectDetail("explorer-sender"))
        assertEquals(state, GameReducer.reduce(state, GameAction.Continue))
        state = GameReducer.reduce(state, GameAction.InspectDetail("explorer-channel"))
        state = GameReducer.reduce(state, GameAction.Continue)
        state = GameReducer.reduce(state, GameAction.PresentCurrentStep)
        assertEquals("game-offer-decision", GameReducer.present(state).step?.id)
        assertEquals(2, state.activeMission?.revealedDetailIds?.size)
        assertEquals(MissionStatus.AWAITING_CHOICE, state.activeMission?.status)
    }

    @Test
    fun examinedEvidenceIsConcreteAndRemainsAvailableAtTheDecision() {
        var state = decisionState(MissionId.DIGITAL_OFFERS, AgeBand.AGENT_11_14)
        val presentation = GameReducer.present(state)
        assertEquals("agent-offer-decision", presentation.step?.id)
        assertEquals(2, presentation.revealedDetailIds.size)
        val examined = presentation.track!!.steps.filterIsInstance<InformationStep>()
            .flatMap { it.details }.filter { it.id in presentation.revealedDetailIds }
        assertEquals(2, examined.size)
        assertTrue(examined.all { it.information.isNotBlank() })
        assertFalse(presentation.revealedDetailIds.contains("agent-credentials"))
    }

    @Test
    fun invalidDetailsChoicesAndRepeatedActionsAreIgnored() {
        var state = openingMission(MissionId.DIGITAL_OFFERS)
        assertEquals(state, GameReducer.reduce(state, GameAction.InspectDetail("unknown")))
        state = GameReducer.reduce(state, GameAction.Continue)
        state = GameReducer.reduce(state, GameAction.PresentCurrentStep)
        state = inspectRequiredDetails(state)
        state = GameReducer.reduce(state, GameAction.PresentCurrentStep)
        assertEquals(state, GameReducer.reduce(state, GameAction.Choose("not-a-choice")))
        state = GameReducer.reduce(state, GameAction.Choose("verify-launcher"))
        assertEquals(GameScreen.FEEDBACK, state.screen)
        assertTrue(GameReducer.present(state).outcome?.consequence?.contains("procurou a promoção") == true)
        assertEquals(state, GameReducer.reduce(state, GameAction.Choose("enter-login")))
        assertTrue(GameReducer.isConsistent(state))
    }

    @Test
    fun duplicateActionsCannotSkipStepsOrProcessAChoiceTwice() {
        var state = GameReducer.reduce(GameState(), GameAction.OpenMission(MissionId.DIGITAL_OFFERS))
        assertEquals(state, GameReducer.reduce(state, GameAction.OpenMission(MissionId.PHONE_MESSAGES)))
        state = GameReducer.reduce(state, GameAction.BeginMission)
        assertEquals(state, GameReducer.reduce(state, GameAction.BeginMission))
        state = GameReducer.reduce(state, GameAction.PresentCurrentStep)
        assertEquals(state, GameReducer.reduce(state, GameAction.PresentCurrentStep))
        state = GameReducer.reduce(state, GameAction.Continue)
        state = GameReducer.reduce(state, GameAction.PresentCurrentStep)
        assertEquals(state, GameReducer.reduce(state, GameAction.Continue))
        assertEquals(state, GameReducer.reduce(state, GameAction.Choose("verify-launcher")))

        state = GameReducer.reduce(state, GameAction.InspectDetail("explorer-sender"))
        val afterFirstInspection = state
        state = GameReducer.reduce(state, GameAction.InspectDetail("explorer-sender"))
        assertEquals(afterFirstInspection, state)
        state = GameReducer.reduce(state, GameAction.InspectDetail("explorer-channel"))
        state = GameReducer.reduce(state, GameAction.Continue)
        assertEquals(state, GameReducer.reduce(state, GameAction.Continue))
        state = GameReducer.reduce(state, GameAction.PresentCurrentStep)
        state = GameReducer.reduce(state, GameAction.Choose("verify-launcher"))
        val feedback = state
        state = GameReducer.reduce(state, GameAction.Choose("enter-login"))
        assertEquals(feedback, state)
        state = GameReducer.reduce(state, GameAction.Continue)
        assertEquals(state, GameReducer.reduce(state, GameAction.Continue))
        assertTrue(GameReducer.isConsistent(state))
    }

    @Test
    fun outcomesRouteToDifferentNarrativeStepsAndConverge() {
        val stateAtDecision = decisionState(MissionId.DIGITAL_OFFERS)
        val verify = GameReducer.reduce(stateAtDecision, GameAction.Choose("verify-launcher"))
        val verifyNext = presentNext(GameReducer.reduce(verify, GameAction.Continue))
        val claim = GameReducer.reduce(stateAtDecision, GameAction.Choose("enter-login"))
        val claimNext = presentNext(GameReducer.reduce(claim, GameAction.Continue))
        assertEquals("official-result", verifyNext.activeMission?.currentStepId)
        assertEquals("game-reconsider", claimNext.activeMission?.currentStepId)
        assertNotEquals(verifyNext.activeMission?.currentStepId, claimNext.activeMission?.currentStepId)
        assertTrue(GameReducer.isConsistent(verifyNext))
    }

    @Test
    fun everyMissionOneAlternativeProducesItsAuthoredOutcomeAndRouteForBothBands() {
        AgeBand.entries.forEach { band ->
            val firstDecisionState = decisionState(MissionId.DIGITAL_OFFERS, band)
            val firstStep = GameReducer.present(firstDecisionState).step as DecisionStep
            firstStep.choices.forEach { choice -> assertChoiceOutcomeAndRoute(firstDecisionState, choice) }

            var recoveryState = GameReducer.reduce(firstDecisionState, GameAction.Choose("enter-login"))
            recoveryState = GameReducer.reduce(recoveryState, GameAction.Continue)
            recoveryState = GameReducer.reduce(recoveryState, GameAction.PresentCurrentStep)
            recoveryState = GameReducer.reduce(recoveryState, GameAction.Continue)
            recoveryState = GameReducer.reduce(recoveryState, GameAction.PresentCurrentStep)
            val expectedRecoveryStep = if (band == AgeBand.AGENT_11_14) "agent-recovery-choice" else "game-recovery-choice"
            assertEquals(expectedRecoveryStep, GameReducer.present(recoveryState).step?.id)
            val recoveryStep = GameReducer.present(recoveryState).step as DecisionStep
            recoveryStep.choices.forEach { choice -> assertChoiceOutcomeAndRoute(recoveryState, choice) }
        }
    }

    @Test
    fun everySchoolMissionAlternativeHasSpecificFeedbackAndAValidContinuation() {
        AgeBand.entries.forEach { band ->
            val firstDecision = decisionState(MissionId.PHONE_MESSAGES, band)
            val step = GameReducer.present(firstDecision).step as DecisionStep
            val expectedDecisionId = if (band == AgeBand.AGENT_11_14) "agent-school-decision" else "school-decision"
            assertEquals(expectedDecisionId, step.id)
            step.choices.forEach { choice -> assertChoiceOutcomeAndRoute(firstDecision, choice) }

            val riskyChoiceId = if (band == AgeBand.AGENT_11_14) "reply-private" else "send-details"
            var recovery = GameReducer.reduce(firstDecision, GameAction.Choose(riskyChoiceId))
            recovery = GameReducer.reduce(recovery, GameAction.Continue)
            recovery = GameReducer.reduce(recovery, GameAction.PresentCurrentStep)
            recovery = inspectRequiredDetails(recovery)
            recovery = GameReducer.reduce(recovery, GameAction.PresentCurrentStep)
            val expectedRecoveryId = if (band == AgeBand.AGENT_11_14) "agent-school-recovery" else "school-recovery-choice"
            assertEquals(expectedRecoveryId, GameReducer.present(recovery).step?.id)
            val recoveryStep = GameReducer.present(recovery).step as DecisionStep
            recoveryStep.choices.forEach { choice -> assertChoiceOutcomeAndRoute(recovery, choice) }
        }
    }

    @Test
    fun groupChatMissionEveryChoiceAndRecoveryCanReachConclusionForBothAgeBands() {
        AgeBand.entries.forEach { band ->
            val firstDecision = decisionState(MissionId.GROUP_CHAT_SUPPORT, band)
            val firstStep = GameReducer.present(firstDecision).step as DecisionStep
            firstStep.choices.forEach { choice ->
                assertChoiceOutcomeAndRoute(firstDecision, choice)
                var afterOutcome = GameReducer.reduce(firstDecision, GameAction.Choose(choice.id))
                afterOutcome = GameReducer.reduce(afterOutcome, GameAction.Continue)
                afterOutcome = GameReducer.reduce(afterOutcome, GameAction.PresentCurrentStep)

                if (GameReducer.present(afterOutcome).step is InformationStep) {
                    afterOutcome = GameReducer.reduce(afterOutcome, GameAction.Continue)
                    afterOutcome = GameReducer.reduce(afterOutcome, GameAction.PresentCurrentStep)
                    val recovery = GameReducer.present(afterOutcome).step as DecisionStep
                    recovery.choices.forEach { recoveryChoice ->
                        assertChoiceOutcomeAndRoute(afterOutcome, recoveryChoice)
                        assertCompletesAfterChoice(afterOutcome, recoveryChoice.id)
                    }
                } else {
                    assertEquals(GameScreen.MISSION_COMPLETE, afterOutcome.screen)
                    assertTrue(GameReducer.isConsistent(afterOutcome))
                }
            }

        }
    }

    @Test
    fun groupChatMissionIsAvailableFromCatalogAndCanBeReplayedWithoutChangingOtherCompletions() {
        AgeBand.entries.forEach { band ->
            var state = GameState(selectedAgeBand = band, completedMissionIds = setOf(MissionId.DIGITAL_OFFERS))
            state = GameReducer.reduce(state, GameAction.OpenMission(MissionId.GROUP_CHAT_SUPPORT))
            state = GameReducer.reduce(state, GameAction.BeginMission)
            state = GameReducer.reduce(state, GameAction.PresentCurrentStep)
            state = GameReducer.reduce(state, GameAction.Continue)
            state = GameReducer.reduce(state, GameAction.PresentCurrentStep)
            val choice = if (band == AgeBand.AGENT_11_14) "agent-adult" else "tell-adult"
            state = assertCompletesAfterChoice(state, choice)
            val replay = GameReducer.reduce(state, GameAction.ReplayMission)
            assertEquals(MissionStatus.NOT_STARTED, replay.activeMission?.status)
            assertTrue(replay.activeMission?.choicesMade.isNullOrEmpty())
            assertEquals(setOf(MissionId.DIGITAL_OFFERS, MissionId.GROUP_CHAT_SUPPORT), replay.completedMissionIds)
            assertTrue(GameReducer.isConsistent(replay))
        }
    }

    @Test
    fun schoolMissionShowsThatTheDateIsTrueButThePrivateDataRequestIsNotOfficial() {
        AgeBand.entries.forEach { band ->
            var state = decisionState(MissionId.PHONE_MESSAGES, band)
            val safeChoice = if (band == AgeBand.AGENT_11_14) "check-app" else "ask-adult"
            state = GameReducer.reduce(state, GameAction.Choose(safeChoice))
            state = GameReducer.reduce(state, GameAction.Continue)
            state = GameReducer.reduce(state, GameAction.PresentCurrentStep)
            val result = GameReducer.present(state).step as InformationStep
            assertTrue(result.body.contains("quinta"))
            assertTrue(result.body.contains("não pediu"))
            state = GameReducer.reduce(state, GameAction.Continue)
            state = GameReducer.reduce(state, GameAction.PresentCurrentStep)
            assertEquals(GameScreen.MISSION_COMPLETE, state.screen)
            val conclusion = GameReducer.present(state).step as ConclusionStep
            assertTrue(conclusion.body.contains("verdadeira") || conclusion.body.contains("verdadeiro"))
            assertTrue(GameReducer.isConsistent(state))
        }
    }

    @Test
    fun schoolMissionCanBeCompletedAndRepeatedInBothAgeBands() {
        AgeBand.entries.forEach { band ->
            var state = GameState(selectedAgeBand = band)
            state = GameReducer.reduce(state, GameAction.OpenMission(MissionId.PHONE_MESSAGES))
            state = GameReducer.reduce(state, GameAction.BeginMission)
            state = GameReducer.reduce(state, GameAction.PresentCurrentStep)
            while (state.screen != GameScreen.MISSION_COMPLETE) {
                when (val step = GameReducer.present(state).step) {
                    is InformationStep -> {
                        state = inspectRequiredDetails(state)
                        state = GameReducer.reduce(state, GameAction.PresentCurrentStep)
                    }
                    is DecisionStep -> {
                        val choiceId = when (step.id) {
                            "school-decision" -> "ask-adult"
                            "agent-school-decision" -> "check-app"
                            else -> error("Unexpected recovery decision in the direct verification path")
                        }
                        state = GameReducer.reduce(state, GameAction.Choose(choiceId))
                        state = GameReducer.reduce(state, GameAction.Continue)
                        state = GameReducer.reduce(state, GameAction.PresentCurrentStep)
                    }
                    else -> error("School mission did not reach a playable step")
                }
            }
            assertTrue(GameReducer.isConsistent(state))
            val replay = GameReducer.reduce(state, GameAction.ReplayMission)
            assertEquals(MissionStatus.NOT_STARTED, replay.activeMission?.status)
            assertEquals(band, replay.activeMission?.ageBand)
        }
    }

    @Test
    fun missionOneCanReachConclusionAndBeRepeatedInBothAgeBands() {
        AgeBand.entries.forEach { band ->
            var state = GameState(selectedAgeBand = band)
            state = GameReducer.reduce(state, GameAction.OpenMission(MissionId.DIGITAL_OFFERS))
            state = GameReducer.reduce(state, GameAction.BeginMission)
            state = GameReducer.reduce(state, GameAction.PresentCurrentStep)
            while (state.screen != GameScreen.MISSION_COMPLETE) {
                when (val step = GameReducer.present(state).step) {
                    is InformationStep -> {
                        state = inspectRequiredDetails(state)
                        state = GameReducer.reduce(state, GameAction.PresentCurrentStep)
                    }
                    is DecisionStep -> {
                        state = GameReducer.reduce(state, GameAction.Choose("verify-launcher"))
                        state = GameReducer.reduce(state, GameAction.Continue)
                        state = GameReducer.reduce(state, GameAction.PresentCurrentStep)
                    }
                    else -> error("Mission did not reach a playable step")
                }
            }
            val conclusionId = if (band == AgeBand.AGENT_11_14) "agent-conclusion" else "game-conclusion"
            assertEquals(conclusionId, GameReducer.present(state).step?.id)
            assertTrue(GameReducer.isConsistent(state))
            val replay = GameReducer.reduce(state, GameAction.ReplayMission)
            assertEquals(MissionStatus.NOT_STARTED, replay.activeMission?.status)
            assertEquals(band, replay.activeMission?.ageBand)
        }
    }

    @Test
    fun capiPromptEncouragesInspectionWithoutRevealingAnswer() {
        var state = decisionState(MissionId.DIGITAL_OFFERS)
        val before = GameReducer.present(state).step as DecisionStep
        state = GameReducer.reduce(state, GameAction.AskCapiForHint)
        val after = GameReducer.present(state).step as DecisionStep
        assertEquals(before.capiPrompt, after.capiPrompt)
        assertTrue(GameReducer.present(state).hintRevealed)
        assertEquals(state, GameReducer.reduce(state, GameAction.Choose("unknown")))
    }

    @Test
    fun reachingConclusionCompletesAndReplayStartsFreshAttempt() {
        var state = decisionState(MissionId.DIGITAL_OFFERS)
        state = GameReducer.reduce(state, GameAction.Choose("verify-launcher"))
        state = presentNext(GameReducer.reduce(state, GameAction.Continue))
        state = GameReducer.reduce(state, GameAction.Continue)
        state = GameReducer.reduce(state, GameAction.PresentCurrentStep)
        assertEquals(GameScreen.MISSION_COMPLETE, state.screen)
        assertEquals(MissionStatus.COMPLETED, state.activeMission?.status)
        assertTrue(MissionId.DIGITAL_OFFERS in state.completedMissionIds)
        val replay = GameReducer.reduce(state, GameAction.ReplayMission)
        assertEquals(MissionStatus.NOT_STARTED, replay.activeMission?.status)
        assertTrue(replay.activeMission?.choicesMade.isNullOrEmpty())
        assertTrue(MissionId.DIGITAL_OFFERS in replay.completedMissionIds)
        assertTrue(GameReducer.isConsistent(replay))
    }

    @Test
    fun returnToHubDropsAttemptAndKeepsNarrativeProgress() {
        var state = decisionState(MissionId.PHONE_MESSAGES)
        state = GameReducer.reduce(state, GameAction.Choose("ask-adult"))
        val hub = GameReducer.reduce(state, GameAction.ReturnToHub)
        assertEquals(GameScreen.HUB, hub.screen)
        assertNull(hub.activeMission)
        assertTrue(GameReducer.isConsistent(hub))
    }

    @Test
    fun returnToHubWorksFromInvestigationDecisionFeedbackAndCompletion() {
        var investigation = openingMission(MissionId.DIGITAL_OFFERS)
        var decision = decisionState(MissionId.PHONE_MESSAGES)
        var feedback = GameReducer.reduce(decision, GameAction.Choose("ask-adult"))
        var completed = decision
        completed = GameReducer.reduce(completed, GameAction.Choose("ask-adult"))
        completed = GameReducer.reduce(completed, GameAction.Continue)
        completed = GameReducer.reduce(completed, GameAction.PresentCurrentStep)
        completed = GameReducer.reduce(completed, GameAction.Continue)
        completed = GameReducer.reduce(completed, GameAction.PresentCurrentStep)
        assertEquals(GameScreen.MISSION_COMPLETE, completed.screen)

        listOf(investigation, decision, feedback, completed).forEach { activeState ->
            val hub = GameReducer.reduce(activeState, GameAction.ReturnToHub)
            assertEquals(GameScreen.HUB, hub.screen)
            assertNull(hub.activeMission)
            assertEquals(activeState.completedMissionIds, hub.completedMissionIds)
            assertTrue(GameReducer.isConsistent(hub))
        }
    }

    @Test
    fun inconsistentRestoredStateRecoversToHub() {
        val valid = decisionState(MissionId.DIGITAL_OFFERS)
        val corrupt = valid.copy(activeMission = valid.activeMission?.copy(currentStepId = "missing"))
        assertFalse(GameReducer.isConsistent(corrupt))
        assertEquals(GameScreen.HUB, GameReducer.recover(corrupt).screen)
        assertEquals(corrupt.completedMissionIds, GameReducer.recover(corrupt).completedMissionIds)
    }

    @Test
    fun absentOrMalformedRestoredSessionFallsBackToHub() {
        val missingSession = GameState(screen = GameScreen.MISSION)
        assertEquals(GameScreen.HUB, GameReducer.recover(missingSession).screen)
        assertNull(GameReducer.recover(missingSession).activeMission)

        val missingStep = GameState(
            screen = GameScreen.MISSION,
            activeMission = MissionSession(
                missionId = MissionId.DIGITAL_OFFERS,
                ageBand = AgeBand.EXPLORER_8_10,
                status = MissionStatus.AWAITING_CHOICE,
            ),
        )
        assertEquals(GameScreen.HUB, GameReducer.recover(missingStep).screen)
    }

    @Test
    fun validatorRejectsCyclesAndMissingRequiredDetails() {
        val mission = MissionCatalog.mission(MissionId.DIGITAL_OFFERS)
        val track = mission.ageTracks.getValue(AgeBand.EXPLORER_8_10)
        val info = track.steps.filterIsInstance<InformationStep>().first()
        val badInfo = info.copy(requiredDetailIds = setOf("missing-detail"), minimumDetailsToInspect = 1)
        val badCycle = InformationStep("cycle", "Ciclo", "Esta etapa retorna a si mesma.", emptyList(), nextStepId = "cycle")
        val brokenTrack = track.copy(
            startingStepId = "cycle",
            steps = track.steps.map { if (it.id == info.id) badInfo else it } + badCycle,
        )
        val problems = MissionValidator.validate(
            mission.copy(ageTracks = mission.ageTracks + (AgeBand.EXPLORER_8_10 to brokenTrack)),
        ).joinToString()
        assertTrue(problems.contains("ciclo"))
        assertTrue(problems.contains("detalhes inexistentes"))
        assertTrue(problems.contains("conclusão alcançável"))
    }

    @Test
    fun completedProgressRemainsSeparateFromNarrativeSession() {
        val progress = GameProgress(setOf(MissionId.PHONE_MESSAGES))
        val state = GameState().withProgress(progress)
        assertEquals(progress, state.toProgress())
        assertNull(state.activeMission)
    }

    private fun openingMission(id: MissionId): GameState {
        var state = GameReducer.reduce(GameState(), GameAction.OpenMission(id))
        state = GameReducer.reduce(state, GameAction.BeginMission)
        return GameReducer.reduce(state, GameAction.PresentCurrentStep)
    }

    private fun inspectRequiredDetails(input: GameState): GameState {
        var state = input
        val step = GameReducer.present(state).step as InformationStep
        val ids = step.requiredDetailIds + step.details.take(step.minimumDetailsToInspect).map { it.id }
        ids.forEach { state = GameReducer.reduce(state, GameAction.InspectDetail(it)) }
        return GameReducer.reduce(state, GameAction.Continue)
    }

    private fun decisionState(id: MissionId, ageBand: AgeBand = AgeBand.EXPLORER_8_10): GameState {
        var state = GameState(selectedAgeBand = ageBand)
        state = GameReducer.reduce(state, GameAction.OpenMission(id))
        state = GameReducer.reduce(state, GameAction.BeginMission)
        state = GameReducer.reduce(state, GameAction.PresentCurrentStep)
        while (GameReducer.present(state).step !is DecisionStep) {
            if (GameReducer.present(state).step is InformationStep) {
                state = inspectRequiredDetails(state)
                state = GameReducer.reduce(state, GameAction.PresentCurrentStep)
            } else error("Mission did not reach a decision step")
        }
        return state
    }

    private fun assertChoiceOutcomeAndRoute(state: GameState, choice: MissionChoice) {
        val step = GameReducer.present(state).step as DecisionStep
        val result = GameReducer.reduce(state, GameAction.Choose(choice.id))
        assertEquals(GameScreen.FEEDBACK, result.screen)
        assertEquals(choice.outcome, GameReducer.present(result).outcome)
        assertTrue(choice.outcome.consequence.isNotBlank())
        assertTrue(choice.outcome.educationalFeedback.isNotBlank())
        val continued = GameReducer.reduce(result, GameAction.Continue)
        assertEquals(choice.outcome.nextStepId, continued.activeMission?.currentStepId)
        assertTrue(GameReducer.isConsistent(continued))
        assertEquals(step.id, result.activeMission?.choicesMade?.lastOrNull()?.stepId)
    }

    private fun assertCompletesAfterChoice(state: GameState, choiceId: String): GameState {
        var next = GameReducer.reduce(state, GameAction.Choose(choiceId))
        next = GameReducer.reduce(next, GameAction.Continue)
        next = GameReducer.reduce(next, GameAction.PresentCurrentStep)
        assertEquals(GameScreen.MISSION_COMPLETE, next.screen)
        assertEquals(MissionStatus.COMPLETED, next.activeMission?.status)
        assertTrue(GameReducer.isConsistent(next))
        return next
    }

    private fun presentNext(input: GameState): GameState = GameReducer.reduce(input, GameAction.PresentCurrentStep)
}
