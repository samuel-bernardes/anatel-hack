package com.example.naomordaaisca.ui.investigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.naomordaaisca.game.CharacterMood
import com.example.naomordaaisca.game.GameAction
import com.example.naomordaaisca.game.InvestigationSession
import com.example.naomordaaisca.game.InvestigationTrack
import com.example.naomordaaisca.ui.kit.CapiAvatar
import com.example.naomordaaisca.ui.kit.InfoCard
import com.example.naomordaaisca.ui.kit.PhoneScaffold
import com.example.naomordaaisca.ui.kit.PrimaryButton
import com.example.naomordaaisca.ui.kit.SecondaryButton
import com.example.naomordaaisca.ui.kit.label
import com.example.naomordaaisca.ui.theme.Alert
import com.example.naomordaaisca.ui.theme.Moss
import com.example.naomordaaisca.ui.theme.inter
import com.example.naomordaaisca.ui.theme.plexMono

@Composable
fun ConsequenceScreen(track: InvestigationTrack, session: InvestigationSession, dispatch: (GameAction) -> Unit) {
    val choice = track.choices.firstOrNull { it.id == session.selectedChoiceId } ?: return
    OutcomeLayout(choice.capiMood, "O que aconteceu") {
        InfoCard("Sua escolha", choice.label)
        InfoCard("Consequência", choice.consequence, border = if (choice.recoveryRequired) Alert.copy(alpha = 0.5f) else Moss.copy(alpha = 0.2f))
        InfoCard("Capi explica", choice.feedback, border = Moss.copy(alpha = 0.5f), labelColor = Moss)
        Spacer(Modifier.height(4.dp))
        PrimaryButton("Continuar", { dispatch(GameAction.ContinueInvestigation) })
        SecondaryButton("Voltar à conversa e reconsiderar", { dispatch(GameAction.NavigateBack) })
    }
}

@Composable
fun RecoveryScreen(track: InvestigationTrack, session: InvestigationSession, dispatch: (GameAction) -> Unit) {
    val mood = track.choices.firstOrNull { it.id == session.selectedChoiceId }?.capiMood ?: CharacterMood.EXPLAINING
    OutcomeLayout(mood, "Ainda dá para agir") {
        InfoCard(
            "O que fazer agora",
            "Feche a página e peça ajuda a uma pessoa de confiança. Se informou senha ou código, proteja a conta pelo aplicativo oficial com essa pessoa.",
            border = Moss.copy(alpha = 0.5f), labelColor = Moss,
        )
        Spacer(Modifier.height(4.dp))
        PrimaryButton("Fechei a página e pedi ajuda", { dispatch(GameAction.RecoverFromInvestigationChoice) })
    }
}

@Composable
fun CompletionScreen(track: InvestigationTrack, dispatch: (GameAction) -> Unit) {
    OutcomeLayout(CharacterMood.EXPLAINING, "Investigação concluída") {
        InfoCard("O que descobrimos", track.conclusion, border = Moss.copy(alpha = 0.5f), labelColor = Moss)
        Spacer(Modifier.height(4.dp))
        PrimaryButton("Voltar ao início", { dispatch(GameAction.ConfirmInvestigationExit) })
        SecondaryButton("Reiniciar investigação", { dispatch(GameAction.RequestInvestigationRestart) })
    }
}

@Composable
private fun OutcomeLayout(mood: CharacterMood, title: String, content: @Composable ColumnScope.() -> Unit) {
    PhoneScaffold(wallpaper = true) {
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                CapiAvatar(40.dp)
                Column {
                    Text("Capi", style = inter(13f, FontWeight.SemiBold))
                    Text(mood.label(), style = plexMono(10f))
                }
            }
            Text(title, style = inter(30f, FontWeight.Light, lineHeight = 1.2f))
            content()
        }
    }
}
