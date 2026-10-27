package com.example.naomordaaisca.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InvestigationTest {
    @Test
    fun catalogDefinitionsAreUniqueAndCompleteAndUnknownInvestigationCannotOpen() {
        val definitions = InvestigationCatalog.definitions
        assertEquals(definitions.map { it.id }.distinct(), definitions.map { it.id })
        definitions.forEach { definition ->
            val errors = InvestigationCatalog.validate(definition)
            assertTrue(errors.joinToString(), errors.isEmpty())
        }

        val initial = GameState()
        assertEquals(initial, GameReducer.reduce(initial, GameAction.OpenInvestigation(MissionId.PHONE_MESSAGES)))
        assertFalse(InvestigationReducer.isValid(InvestigationSession(missionId = MissionId.PHONE_MESSAGES, ageBand = AgeBand.EXPLORER_8_10)))
    }

    @Test
    fun bothAgeTracksHaveDistinctCompleteContentAndValidEvidenceReferences() {
        AgeBand.entries.forEach { band ->
            val track = InvestigationCatalog.track(band)
            assertTrue(InvestigationCatalog.validate(track).joinToString(), InvestigationCatalog.validate(track).isEmpty())
            assertEquals(SimulatedAppId.entries.toSet(), track.clues.map { it.appId }.toSet())
            assertTrue(track.evidenceRequiredForDecision.all { required -> track.clues.any { it.id == required } })
        }
        assertNotEquals(InvestigationCatalog.track(AgeBand.EXPLORER_8_10).objective, InvestigationCatalog.track(AgeBand.AGENT_11_14).objective)
    }

    @Test
    fun investigationOpensFromDeviceHubAndResumesWithoutResettingAppOrEvidence() {
        var state = GameReducer.reduce(GameState(), GameAction.OpenInvestigation())
        assertEquals(GameScreen.INVESTIGATION, state.screen)
        assertEquals(InvestigationStage.INVESTIGATING, state.investigation?.stage)
        state = GameReducer.reduce(state, GameAction.DiscoverEvidence("offer-request"))
        state = GameReducer.reduce(state, GameAction.OpenInvestigationApp(SimulatedAppId.BROWSER))
        assertEquals(InvestigationStage.INVESTIGATING, state.investigation?.stage)
        state = GameReducer.reduce(state, GameAction.DiscoverEvidence("offer-official-news"))
        state = GameReducer.reduce(state, GameAction.OpenInvestigationApp(SimulatedAppId.MESSAGES))
        assertEquals(listOf("offer-request", "offer-official-news"), state.investigation?.discoveredEvidenceIds?.toList())
        assertEquals(emptyList<SimulatedAppId>(), state.investigation?.navigation?.appHistory)
        state = GameReducer.reduce(state, GameAction.ReturnToHub)
        assertEquals(InvestigationDialog.CONFIRM_EXIT, state.investigation?.dialog)
        state = GameReducer.reduce(state, GameAction.CancelInvestigationConfirmation)
        assertTrue(GameReducer.isConsistent(state))
        val recoveredAfterInterruption = GameState().withProgress(state.toProgress())
        val resumed = GameReducer.reduce(recoveredAfterInterruption, GameAction.OpenInvestigation())
        assertEquals(InvestigationStage.INVESTIGATING, resumed.investigation?.stage)
        assertEquals(setOf("offer-request", "offer-official-news"), resumed.investigation?.discoveredEvidenceIds)
    }

    @Test
    fun decisionRequiresTheRelevantMessageAndIndependentChannelEvidenceToBeUsed() {
        var state = GameReducer.reduce(GameState(), GameAction.OpenInvestigation())
        assertEquals(state, GameReducer.reduce(state, GameAction.MakeInvestigationChoice("verify-known-channel")))
        assertTrue(InvestigationReducer.availableChoices(state.investigation!!).isEmpty())
        state = GameReducer.reduce(state, GameAction.DiscoverEvidence("offer-request"))
        assertEquals(state, GameReducer.reduce(state, GameAction.MakeInvestigationChoice("verify-known-channel")))
        state = GameReducer.reduce(state, GameAction.UseEvidence("offer-request"))
        state = GameReducer.reduce(state, GameAction.OpenInvestigationApp(SimulatedAppId.BROWSER))
        state = GameReducer.reduce(state, GameAction.DiscoverEvidence("offer-official-news"))
        state = GameReducer.reduce(state, GameAction.UseEvidence("offer-official-news"))
        state = GameReducer.reduce(state, GameAction.OpenInvestigationApp(SimulatedAppId.MESSAGES))
        assertEquals(InvestigationCatalog.track(AgeBand.EXPLORER_8_10).choices, InvestigationReducer.availableChoices(state.investigation!!))
        val before = state
        state = GameReducer.reduce(state, GameAction.MakeInvestigationChoice("verify-known-channel"))
        assertEquals(InvestigationStage.PRESENTING_CONSEQUENCE, state.investigation?.stage)
        assertNotEquals(before, state)
        assertEquals(state, GameReducer.reduce(state, GameAction.MakeInvestigationChoice("enter-credentials")))
        assertTrue(GameReducer.isConsistent(state))
    }

    @Test
    fun playerCanReturnToMessagesFromFeedbackAndReconsiderWithoutLosingEvidenceOrHistory() {
        var state = readyToDecide(AgeBand.EXPLORER_8_10)
        state = GameReducer.reduce(state, GameAction.MakeInvestigationChoice("verify-known-channel"))
        state = GameReducer.reduce(state, GameAction.NavigateBack)
        assertEquals(InvestigationStage.INVESTIGATING, state.investigation?.stage)
        assertNull(state.investigation?.selectedChoiceId)
        assertEquals(listOf("verify-known-channel"), state.investigation?.choicesMade)
        assertTrue(state.investigation!!.usedEvidenceIds.containsAll(InvestigationCatalog.track(AgeBand.EXPLORER_8_10).evidenceRequiredForDecision))
        state = GameReducer.reduce(state, GameAction.MakeInvestigationChoice("ask-trusted-adult"))
        assertEquals(InvestigationStage.PRESENTING_CONSEQUENCE, state.investigation?.stage)
        assertEquals(listOf("verify-known-channel", "ask-trusted-adult"), state.investigation?.choicesMade)
    }

    @Test
    fun everyChoiceInBothAgeTracksHasAuthoredFeedbackAndReachesCompletion() {
        AgeBand.entries.forEach { band ->
            val track = InvestigationCatalog.track(band)
            track.choices.forEach { choice ->
                var state = readyToDecide(band)
                state = GameReducer.reduce(state, GameAction.MakeInvestigationChoice(choice.id))
                assertEquals(InvestigationStage.PRESENTING_CONSEQUENCE, state.investigation?.stage)
                assertTrue(choice.consequence.isNotBlank())
                assertTrue(choice.feedback.isNotBlank())
                state = GameReducer.reduce(state, GameAction.ContinueInvestigation)
                if (choice.recoveryRequired) {
                    assertEquals(InvestigationStage.RECOVERY, state.investigation?.stage)
                    assertEquals(state, GameReducer.reduce(state, GameAction.ContinueInvestigation))
                    state = GameReducer.reduce(state, GameAction.RecoverFromInvestigationChoice)
                }
                assertEquals(InvestigationStage.COMPLETED, state.investigation?.stage)
                assertTrue(MissionId.DIGITAL_OFFERS in state.completedMissionIds)
                assertTrue(GameReducer.isConsistent(state))
            }
        }
    }

    @Test
    fun backAndConfirmationPreserveAttemptUntilExplicitExitOrRestart() {
        var state = GameReducer.reduce(GameState(), GameAction.OpenInvestigation())
        state = GameReducer.reduce(state, GameAction.DiscoverEvidence("offer-peer-context"))
        state = GameReducer.reduce(state, GameAction.RequestInvestigationExit)
        assertEquals(InvestigationDialog.CONFIRM_EXIT, state.investigation?.dialog)
        state = GameReducer.reduce(state, GameAction.CancelInvestigationConfirmation)
        assertEquals(setOf("offer-peer-context"), state.investigation?.discoveredEvidenceIds)
        state = GameReducer.reduce(state, GameAction.NavigateBack)
        assertEquals(InvestigationDialog.CONFIRM_EXIT, state.investigation?.dialog)
        state = GameReducer.reduce(state, GameAction.ConfirmInvestigationExit)
        assertEquals(GameScreen.HUB, state.screen)
        assertNull(state.investigation)

        var restart = readyToDecide(AgeBand.EXPLORER_8_10)
        restart = GameReducer.reduce(restart, GameAction.RequestInvestigationRestart)
        assertEquals(InvestigationDialog.CONFIRM_RESTART, restart.investigation?.dialog)
        restart = GameReducer.reduce(restart, GameAction.CancelInvestigationConfirmation)
        assertEquals(2, restart.investigation?.usedEvidenceIds?.size)
        restart = GameReducer.reduce(restart, GameAction.RequestInvestigationRestart)
        restart = GameReducer.reduce(restart, GameAction.ConfirmInvestigationRestart)
        assertEquals(InvestigationSession(ageBand = AgeBand.EXPLORER_8_10), restart.investigation)
        assertTrue(GameReducer.isConsistent(restart))
    }

    @Test
    fun interruptionSnapshotRoundTripsAndRejectsMissingOrCorruptData() {
        val initial = readyToDecide(AgeBand.AGENT_11_14)
        val choiceState = GameReducer.reduce(initial, GameAction.MakeInvestigationChoice("agent-verify"))
        val encoded = InvestigationSnapshotCodec.encode(choiceState.investigation!!)
        val restored = InvestigationSnapshotCodec.decode(encoded)
        assertEquals(choiceState.investigation, restored)
        assertEquals(null, InvestigationSnapshotCodec.decode(null))
        assertEquals(null, InvestigationSnapshotCodec.decode("not-a-snapshot"))
        assertEquals(null, InvestigationSnapshotCodec.decode(encoded?.replace("offer-agent-news", "unknown-evidence")))

        val legacyBrowserSnapshot = "1|DIGITAL_OFFERS|AGENT_11_14|BROWSER|BROWSER|MESSAGES|offer-agent-context||||"
        val migrated = InvestigationSnapshotCodec.decode(legacyBrowserSnapshot)
        assertEquals(InvestigationStage.INVESTIGATING, migrated?.stage)
        assertEquals(SimulatedAppId.BROWSER, migrated?.navigation?.currentApp)
        assertEquals(listOf(SimulatedAppId.MESSAGES), migrated?.navigation?.appHistory)

        val progress = GameProgress(setOf(MissionId.PHONE_MESSAGES), restored)
        assertEquals(progress, progress.copy(activeInvestigation = InvestigationSnapshotCodec.decode(InvestigationSnapshotCodec.encode(progress.activeInvestigation!!))))
    }

    @Test
    fun invalidEvidenceAndImpossibleSessionStatesAreRejectedSafely() {
        val initial = InvestigationSession(ageBand = AgeBand.EXPLORER_8_10)
        assertFalse(InvestigationReducer.isValid(initial.copy(discoveredEvidenceIds = setOf("missing"))))
        assertFalse(InvestigationReducer.isValid(initial.copy(usedEvidenceIds = setOf("offer-request"))))
        assertFalse(InvestigationReducer.isValid(initial.copy(stage = InvestigationStage.PRESENTING_CONSEQUENCE)))
        assertEquals(initial, InvestigationReducer.reduce(initial, GameAction.UseEvidence("offer-request")))

        val corruptCompleted = GameState(
            screen = GameScreen.HUB,
            completedMissionIds = setOf(MissionId.PHONE_MESSAGES),
            investigation = InvestigationSession(
                ageBand = AgeBand.EXPLORER_8_10,
                stage = InvestigationStage.COMPLETED,
                choicesMade = listOf("verify-known-channel"),
            ),
        )
        val recovered = GameReducer.recover(corruptCompleted)
        assertNull(recovered.investigation)
        assertEquals(setOf(MissionId.PHONE_MESSAGES), recovered.completedMissionIds)
        assertTrue(GameReducer.isConsistent(recovered))
    }

    private fun readyToDecide(band: AgeBand): GameState {
        var state = GameReducer.reduce(GameState(selectedAgeBand = band), GameAction.OpenInvestigation())
        val track = InvestigationCatalog.track(band)
        track.evidenceRequiredForDecision.forEach { id ->
            val clue = track.clues.first { it.id == id }
            if (state.investigation!!.navigation.currentApp != clue.appId) {
                state = GameReducer.reduce(state, GameAction.OpenInvestigationApp(clue.appId))
            }
            state = GameReducer.reduce(state, GameAction.DiscoverEvidence(id))
            state = GameReducer.reduce(state, GameAction.UseEvidence(id))
        }
        if (state.investigation!!.navigation.currentApp != SimulatedAppId.MESSAGES) {
            state = GameReducer.reduce(state, GameAction.OpenInvestigationApp(SimulatedAppId.MESSAGES))
        }
        return state
    }
}
