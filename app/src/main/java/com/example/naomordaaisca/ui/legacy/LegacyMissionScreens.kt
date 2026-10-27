package com.example.naomordaaisca.ui.legacy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.naomordaaisca.game.ConclusionStep
import com.example.naomordaaisca.game.DecisionStep
import com.example.naomordaaisca.game.EvidenceStatus
import com.example.naomordaaisca.game.GameAction
import com.example.naomordaaisca.game.GamePresentation
import com.example.naomordaaisca.game.InformationStep
import com.example.naomordaaisca.game.InteractableId
import com.example.naomordaaisca.game.MissionStatus
import com.example.naomordaaisca.ui.kit.AppHeader
import com.example.naomordaaisca.ui.kit.CapiNote
import com.example.naomordaaisca.ui.kit.CenteredLabel
import com.example.naomordaaisca.ui.kit.EvidenceCard
import com.example.naomordaaisca.ui.kit.IncomingBubble
import com.example.naomordaaisca.ui.kit.InfoCard
import com.example.naomordaaisca.ui.kit.PhoneScaffold
import com.example.naomordaaisca.ui.kit.PrimaryButton
import com.example.naomordaaisca.ui.kit.ReplyOption
import com.example.naomordaaisca.ui.kit.SecondaryButton
import com.example.naomordaaisca.ui.kit.label
import com.example.naomordaaisca.ui.theme.BrowserBlue
import com.example.naomordaaisca.ui.theme.BrowserTile
import com.example.naomordaaisca.ui.theme.InkMuted
import com.example.naomordaaisca.ui.theme.InkSoft
import com.example.naomordaaisca.ui.theme.Moss
import com.example.naomordaaisca.ui.theme.SendBubble
import com.example.naomordaaisca.ui.theme.SendCyan
import com.example.naomordaaisca.ui.theme.inter
import com.example.naomordaaisca.ui.theme.plexMono

/** Missões ainda no fluxo por etapas, apresentadas com a mesma linguagem visual do celular. */
@Composable
fun LegacyMissionScreen(presentation: GamePresentation, dispatch: (GameAction) -> Unit) {
    if (presentation.mission == null) return
    val track = presentation.track ?: return
    val step = presentation.step
    val (accent, bubble) = presentation.interactable.colors()
    LegacyScaffold(presentation, dispatch) {
        if (presentation.stepNumber == 1) CapiNote("Capi · contexto", "${track.context}\n\nObjetivo: ${track.objective}")
        when {
            step is InformationStep && presentation.missionStatus == MissionStatus.AWAITING_INTERACTION -> {
                InfoCard(step.heading, step.body)
                if (step.details.isNotEmpty()) CenteredLabel("Detalhes para examinar")
                step.details.forEach { detail ->
                    EvidenceCard(
                        title = detail.label,
                        content = detail.information,
                        status = if (detail.id in presentation.revealedDetailIds) EvidenceStatus.USED else EvidenceStatus.AVAILABLE,
                        accent = accent,
                        actionColor = bubble,
                        onDiscover = { dispatch(GameAction.InspectDetail(detail.id)) },
                        onUse = null,
                    )
                }
                val ready = presentation.revealedDetailIds.containsAll(step.requiredDetailIds) &&
                    step.details.count { it.id in presentation.revealedDetailIds } >= step.minimumDetailsToInspect
                if (!ready) Text("Examine os detalhes para continuar.", style = inter(12f, color = InkMuted))
                PrimaryButton("Continuar", { dispatch(GameAction.Continue) }, enabled = ready)
            }
            step is DecisionStep && presentation.missionStatus == MissionStatus.AWAITING_CHOICE -> {
                val message = step.message
                IncomingBubble(message.text, message.context) {
                    Text(message.senderDisplayName, style = inter(13f, FontWeight.SemiBold, accent))
                    if (message.senderIdentity.isNotBlank() && message.senderIdentity != message.senderDisplayName) {
                        Text("Identificação apresentada: ${message.senderIdentity}", style = plexMono(9f))
                    }
                }
                val examined = track.steps.filterIsInstance<InformationStep>().flatMap { it.details }.filter { it.id in presentation.revealedDetailIds }
                if (examined.isNotEmpty()) {
                    CenteredLabel("Informações examinadas")
                    examined.forEach { detail -> InfoCard(detail.label, detail.information) }
                }
                if (presentation.hintRevealed) CapiNote("Pergunta da Capi", step.capiPrompt)
                else SecondaryButton("Pedir ajuda à Capi", { dispatch(GameAction.AskCapiForHint) }, color = Moss)
                CenteredLabel("Como você quer agir?", Moss)
                step.choices.forEach { choice -> ReplyOption(choice.label, accent, bubble) { dispatch(GameAction.Choose(choice.id)) } }
            }
            else -> Text("Preparando a próxima etapa…", style = inter(12f, color = InkSoft))
        }
    }
}

@Composable
fun LegacyFeedbackScreen(presentation: GamePresentation, dispatch: (GameAction) -> Unit) {
    val step = presentation.step as? DecisionStep ?: return
    val selected = step.choices.firstOrNull { it.id == presentation.selectedChoiceId }
    val outcome = presentation.outcome ?: return
    LegacyScaffold(presentation, dispatch) {
        InfoCard("Sua escolha", selected?.label.orEmpty())
        InfoCard("Consequência", outcome.consequence)
        InfoCard("Por que isso importa", outcome.educationalFeedback, border = Moss.copy(alpha = 0.5f), labelColor = Moss)
        Spacer(Modifier.height(4.dp))
        PrimaryButton("Continuar", { dispatch(GameAction.Continue) })
        SecondaryButton("Voltar ao início", { dispatch(GameAction.ReturnToHub) })
    }
}

@Composable
fun LegacyCompletionScreen(presentation: GamePresentation, dispatch: (GameAction) -> Unit) {
    val conclusion = presentation.step as? ConclusionStep
    LegacyScaffold(presentation, dispatch) {
        Text(conclusion?.title ?: "Missão concluída!", style = inter(28f, FontWeight.Light, lineHeight = 1.2f))
        CapiNote("Capi", conclusion?.body ?: "Você investigou a situação. Continue conferindo informações por canais conhecidos.")
        Text(
            "${presentation.completedMissionCount}/${presentation.missionCount} missões concluídas".uppercase(),
            style = plexMono(10f, color = Moss),
        )
        PrimaryButton("Jogar esta missão de novo", { dispatch(GameAction.ReplayMission) })
        SecondaryButton("Voltar ao início", { dispatch(GameAction.ReturnToHub) })
    }
}

@Composable
private fun LegacyScaffold(presentation: GamePresentation, dispatch: (GameAction) -> Unit, content: @Composable ColumnScope.() -> Unit) {
    val (accent, _) = presentation.interactable.colors()
    val stepInfo = if (presentation.stepCount > 0) " · etapa ${presentation.stepNumber} de ${presentation.stepCount}" else ""
    PhoneScaffold {
        AppHeader(
            title = presentation.mission?.title.orEmpty(),
            subtitle = "${presentation.interactable?.label() ?: "Missão"}$stepInfo",
            titleColor = accent,
            titleSize = 18f,
            onBack = { dispatch(GameAction.ReturnToHub) },
        )
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
    }
}

private fun InteractableId?.colors(): Pair<Color, Color> = when (this) {
    InteractableId.COMPUTER -> BrowserBlue to BrowserTile
    else -> SendCyan to SendBubble
}
