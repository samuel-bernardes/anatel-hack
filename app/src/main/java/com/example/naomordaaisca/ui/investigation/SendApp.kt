package com.example.naomordaaisca.ui.investigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.naomordaaisca.R
import com.example.naomordaaisca.game.EvidenceStatus
import com.example.naomordaaisca.game.GameAction
import com.example.naomordaaisca.game.InvestigationReducer
import com.example.naomordaaisca.game.InvestigationSession
import com.example.naomordaaisca.game.InvestigationTrack
import com.example.naomordaaisca.game.SimulatedAppId
import com.example.naomordaaisca.ui.kit.AppHeader
import com.example.naomordaaisca.ui.kit.CapiNote
import com.example.naomordaaisca.ui.kit.CenteredLabel
import com.example.naomordaaisca.ui.kit.CountBadge
import com.example.naomordaaisca.ui.kit.EvidenceCard
import com.example.naomordaaisca.ui.kit.HorizontalLine
import com.example.naomordaaisca.ui.kit.IncomingBubble
import com.example.naomordaaisca.ui.kit.InvestigationMenuButton
import com.example.naomordaaisca.ui.kit.PHONE_OWNER
import com.example.naomordaaisca.ui.kit.PhoneScaffold
import com.example.naomordaaisca.ui.kit.PhotoAvatar
import com.example.naomordaaisca.ui.kit.ReplyOption
import com.example.naomordaaisca.ui.kit.SearchField
import com.example.naomordaaisca.ui.kit.clock
import com.example.naomordaaisca.ui.kit.contactName
import com.example.naomordaaisca.ui.kit.contactRole
import com.example.naomordaaisca.ui.kit.minutesAgo
import com.example.naomordaaisca.ui.kit.rememberNow
import com.example.naomordaaisca.ui.kit.statusOf
import com.example.naomordaaisca.ui.theme.Field
import com.example.naomordaaisca.ui.theme.InkMuted
import com.example.naomordaaisca.ui.theme.InkSoft
import com.example.naomordaaisca.ui.theme.Moss
import com.example.naomordaaisca.ui.theme.Night
import com.example.naomordaaisca.ui.theme.Panel
import com.example.naomordaaisca.ui.theme.SendBubble
import com.example.naomordaaisca.ui.theme.SendCyan
import com.example.naomordaaisca.ui.theme.inter
import com.example.naomordaaisca.ui.theme.plexMono

@Composable
fun SendApp(
    track: InvestigationTrack,
    session: InvestigationSession,
    chatOpen: Boolean,
    onOpenChat: () -> Unit,
    onCloseChat: () -> Unit,
    onHome: () -> Unit,
    dispatch: (GameAction) -> Unit,
) {
    if (chatOpen) SendConversation(track, session, onCloseChat, onHome, dispatch)
    else SendInbox(track, session, onOpenChat, onHome, dispatch)
}

@Composable
private fun SendInbox(
    track: InvestigationTrack,
    session: InvestigationSession,
    onOpenChat: () -> Unit,
    onHome: () -> Unit,
    dispatch: (GameAction) -> Unit,
) {
    val time = rememberNow().minutesAgo(2).clock()
    val unread = track.clues.count { it.appId == SimulatedAppId.MESSAGES && session.statusOf(it.id) == EvidenceStatus.AVAILABLE }
    PhoneScaffold(onHome = onHome) {
        AppHeader("Send", PHONE_OWNER, SendCyan, trailing = { InvestigationMenuButton(R.drawable.ic_ellipsis_vertical, dispatch) })
        Column(Modifier.weight(1f).fillMaxWidth()) {
            Box(Modifier.padding(20.dp)) { SearchField("Buscar conversa") }
            Tabs("Todas", if (unread > 0) "Não lidas · $unread" else "Não lidas", "Grupos")
            Row(
                modifier = Modifier.fillMaxWidth().clickable(onClickLabel = "Abrir conversa", onClick = onOpenChat)
                    .padding(horizontal = 18.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                PhotoAvatar(R.drawable.avatar_lia)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(track.contactName, style = inter(14f, FontWeight.SemiBold))
                        Text(time, style = plexMono(9f, color = SendCyan))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        Text(
                            track.message, style = inter(12f, color = InkSoft), maxLines = 2, overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        if (unread > 0) CountBadge(unread, color = SendCyan, textColor = Night, size = 18.dp)
                    }
                }
            }
            HorizontalLine()
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(9.dp, Alignment.Bottom),
            ) {
                Image(painterResource(R.drawable.ic_lock_keyhole), contentDescription = null)
                Text("Suas conversas ficam entre vocês.", style = inter(10f, color = InkMuted))
            }
            Tabs("Conversas", "Chamadas", "Você")
        }
    }
}

@Composable
private fun Tabs(selected: String, vararg others: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(selected, style = inter(12f, FontWeight.SemiBold, SendCyan))
        others.forEach { Text(it, style = inter(12f, color = InkMuted)) }
    }
    HorizontalLine()
}

@Composable
private fun SendConversation(
    track: InvestigationTrack,
    session: InvestigationSession,
    onBack: () -> Unit,
    onHome: () -> Unit,
    dispatch: (GameAction) -> Unit,
) {
    val time = rememberNow().minutesAgo(2).clock()
    val choices = InvestigationReducer.availableChoices(session)
    val scroll = rememberScrollState()
    LaunchedEffect(choices.isNotEmpty()) {
        if (choices.isNotEmpty()) scroll.animateScrollTo(scroll.maxValue)
    }
    PhoneScaffold(onHome = onHome) {
        AppHeader(
            title = track.contactName,
            subtitle = listOf("Send", track.contactRole).filter(String::isNotBlank).joinToString(" · "),
            titleColor = SendCyan,
            titleSize = 18f,
            onBack = onBack,
            trailing = { InvestigationMenuButton(R.drawable.ic_ellipsis, dispatch) },
        )
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(scroll).padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            CenteredLabel("Hoje")
            CapiNote("Capi · o que está acontecendo", track.context)
            CenteredLabel("Novas mensagens", SendCyan)
            IncomingBubble(track.message, time)
            CenteredLabel("Pistas nesta conversa")
            track.clues.filter { it.appId == SimulatedAppId.MESSAGES }.forEach { clue ->
                EvidenceCard(
                    title = clue.title,
                    content = clue.content,
                    status = session.statusOf(clue.id),
                    accent = SendCyan,
                    actionColor = SendBubble,
                    onDiscover = { dispatch(GameAction.DiscoverEvidence(clue.id)) },
                    onUse = { dispatch(GameAction.UseEvidence(clue.id)) },
                )
            }
            if (choices.isNotEmpty()) {
                Spacer(Modifier.padding(top = 4.dp))
                CenteredLabel("Com as pistas reunidas, como você quer agir?", Moss)
                choices.forEach { choice ->
                    ReplyOption(choice.label, SendCyan, SendBubble) { dispatch(GameAction.MakeInvestigationChoice(choice.id)) }
                }
            }
        }
        Composer(
            if (choices.isEmpty()) "Anote uma pista desta conversa e uma do Navegador para responder."
            else "Escolha uma resposta acima.",
        )
    }
}

/** Campo de mensagem do Figma; responder só acontece pelas escolhas do caso. */
@Composable
private fun Composer(hint: String) {
    HorizontalLine()
    Row(
        modifier = Modifier.fillMaxWidth().background(Panel).heightIn(min = 70.dp).padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(painterResource(R.drawable.ic_plus_chat), contentDescription = null, modifier = Modifier.alpha(0.5f))
        Box(
            modifier = Modifier.weight(1f).heightIn(min = 42.dp).clip(RoundedCornerShape(12.dp)).background(Field)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(hint, style = inter(12f, color = InkMuted))
        }
        Image(painterResource(R.drawable.ic_send_chat), contentDescription = null, modifier = Modifier.alpha(0.5f))
    }
}
