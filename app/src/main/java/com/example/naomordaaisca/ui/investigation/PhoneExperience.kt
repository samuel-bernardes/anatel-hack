package com.example.naomordaaisca.ui.investigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.naomordaaisca.R
import com.example.naomordaaisca.game.EvidenceStatus
import com.example.naomordaaisca.game.GameAction
import com.example.naomordaaisca.game.InvestigationCatalog
import com.example.naomordaaisca.game.InvestigationDefinition
import com.example.naomordaaisca.game.InvestigationDialog
import com.example.naomordaaisca.game.InvestigationSession
import com.example.naomordaaisca.game.InvestigationStage
import com.example.naomordaaisca.game.InvestigationTrack
import com.example.naomordaaisca.game.SimulatedAppId
import com.example.naomordaaisca.ui.kit.ConfirmOverlay
import com.example.naomordaaisca.ui.kit.CountBadge
import com.example.naomordaaisca.ui.kit.InitialsAvatar
import com.example.naomordaaisca.ui.kit.InvestigationMenuButton
import com.example.naomordaaisca.ui.kit.PHONE_OWNER
import com.example.naomordaaisca.ui.kit.PHONE_OWNER_INITIALS
import com.example.naomordaaisca.ui.kit.PhoneScaffold
import com.example.naomordaaisca.ui.kit.AppHeader
import com.example.naomordaaisca.ui.kit.clock
import com.example.naomordaaisca.ui.kit.contactName
import com.example.naomordaaisca.ui.kit.longDate
import com.example.naomordaaisca.ui.kit.panel
import com.example.naomordaaisca.ui.kit.rememberNow
import com.example.naomordaaisca.ui.kit.shortDate
import com.example.naomordaaisca.ui.kit.statusOf
import com.example.naomordaaisca.ui.theme.AppTint
import com.example.naomordaaisca.ui.theme.ConfiguracoesTint
import com.example.naomordaaisca.ui.theme.ContatosTint
import com.example.naomordaaisca.ui.theme.GaleriaTint
import com.example.naomordaaisca.ui.theme.Ink
import com.example.naomordaaisca.ui.theme.InkSoft
import com.example.naomordaaisca.ui.theme.Line
import com.example.naomordaaisca.ui.theme.LojaTint
import com.example.naomordaaisca.ui.theme.Moss
import com.example.naomordaaisca.ui.theme.MusicasTint
import com.example.naomordaaisca.ui.theme.NavegadorTint
import com.example.naomordaaisca.ui.theme.SendTint
import com.example.naomordaaisca.ui.theme.SmsTint
import com.example.naomordaaisca.ui.theme.SocialTint
import com.example.naomordaaisca.ui.theme.TelefoneTint
import com.example.naomordaaisca.ui.theme.TmailTint
import com.example.naomordaaisca.ui.theme.inter
import com.example.naomordaaisca.ui.theme.plexMono
import kotlin.math.roundToInt

/** O que o celular mostra. É estado de apresentação: o progresso do caso fica na sessão do reducer. */
private enum class PhoneView { LOCK, HOME, APP }

enum class PhoneApp(val title: String, val icon: Int, val tint: AppTint, val simulatedApp: SimulatedAppId? = null) {
    NAVEGADOR("Navegador", R.drawable.ic_compass, NavegadorTint, SimulatedAppId.BROWSER),
    CONTATOS("Contatos", R.drawable.ic_contact, ContatosTint),
    SMS("SMS", R.drawable.ic_message_square, SmsTint),
    TMAIL("Tmail", R.drawable.ic_mail_home, TmailTint),
    GALERIA("Galeria", R.drawable.ic_images, GaleriaTint),
    SOCIAL("Social", R.drawable.ic_orbit, SocialTint),
    SEND("Send", R.drawable.ic_send_home, SendTint, SimulatedAppId.MESSAGES),
    CONFIGURACOES("Configurações", R.drawable.ic_settings, ConfiguracoesTint),
    LOJA("Loja", R.drawable.ic_shopping_bag, LojaTint),
    MUSICAS("Músicas", R.drawable.ic_music, MusicasTint),
    TELEFONE("Telefone", R.drawable.ic_phone, TelefoneTint),
}

@Composable
fun InvestigationExperience(session: InvestigationSession, dispatch: (GameAction) -> Unit) {
    val definition = InvestigationCatalog.definition(session.missionId) ?: return
    val track = InvestigationCatalog.track(session.missionId, session.ageBand) ?: return
    var view by rememberSaveable { mutableStateOf(PhoneView.LOCK) }
    var app by rememberSaveable { mutableStateOf(PhoneApp.SEND) }
    var chatOpen by rememberSaveable { mutableStateOf(false) }

    fun open(target: PhoneApp, withChat: Boolean = false) {
        target.simulatedApp?.let { dispatch(GameAction.OpenInvestigationApp(it)) }
        app = target
        chatOpen = withChat
        view = PhoneView.APP
    }
    fun goHome() {
        view = PhoneView.HOME
        chatOpen = false
    }

    BackHandler {
        when {
            session.dialog != null -> dispatch(GameAction.CancelInvestigationConfirmation)
            session.stage == InvestigationStage.PRESENTING_CONSEQUENCE || session.stage == InvestigationStage.RECOVERY ->
                dispatch(GameAction.NavigateBack)
            session.stage == InvestigationStage.COMPLETED -> dispatch(GameAction.ConfirmInvestigationExit)
            view == PhoneView.APP && chatOpen -> chatOpen = false
            view == PhoneView.APP -> goHome()
            else -> dispatch(GameAction.RequestInvestigationExit)
        }
    }

    Box(Modifier.fillMaxSize()) {
        when (session.stage) {
            InvestigationStage.INVESTIGATING -> when (view) {
                PhoneView.LOCK -> LockScreen(track, onUnlock = { view = PhoneView.HOME }, onOpenNotification = { open(PhoneApp.SEND, withChat = true) })
                PhoneView.HOME -> HomeScreen(track, definition, session, onOpen = { open(it) }, dispatch = dispatch)
                PhoneView.APP -> when (app) {
                    PhoneApp.SEND -> SendApp(
                        track = track, session = session, chatOpen = chatOpen,
                        onOpenChat = { chatOpen = true }, onCloseChat = { chatOpen = false }, onHome = ::goHome, dispatch = dispatch,
                    )
                    PhoneApp.NAVEGADOR -> BrowserApp(
                        track = track, session = session,
                        onBack = {
                            if (session.navigation.appHistory.lastOrNull() == SimulatedAppId.MESSAGES) open(PhoneApp.SEND, withChat = true) else goHome()
                        },
                        onHome = ::goHome, dispatch = dispatch,
                    )
                    else -> PlaceholderApp(app, onHome = ::goHome)
                }
            }
            InvestigationStage.PRESENTING_CONSEQUENCE -> ConsequenceScreen(track, session, dispatch)
            InvestigationStage.RECOVERY -> RecoveryScreen(track, session, dispatch)
            InvestigationStage.COMPLETED -> CompletionScreen(track, dispatch)
        }
        when (session.dialog) {
            InvestigationDialog.CONFIRM_EXIT -> ConfirmOverlay(
                title = "Sair da investigação?",
                body = "O progresso desta tentativa será descartado.",
                confirmLabel = "Sair",
                cancelLabel = "Continuar investigação",
                onConfirm = { dispatch(GameAction.ConfirmInvestigationExit) },
                onCancel = { dispatch(GameAction.CancelInvestigationConfirmation) },
            )
            InvestigationDialog.CONFIRM_RESTART -> ConfirmOverlay(
                title = "Reiniciar a missão?",
                body = "As pistas e escolhas desta tentativa serão apagadas.",
                confirmLabel = "Reiniciar",
                cancelLabel = "Continuar investigação",
                onConfirm = {
                    dispatch(GameAction.ConfirmInvestigationRestart)
                    view = PhoneView.LOCK
                    chatOpen = false
                },
                onCancel = { dispatch(GameAction.CancelInvestigationConfirmation) },
            )
            null -> Unit
        }
    }
}

@Composable
private fun LockScreen(track: InvestigationTrack, onUnlock: () -> Unit, onOpenNotification: () -> Unit) {
    val now = rememberNow()
    PhoneScaffold(wallpaper = true) {
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                InitialsAvatar(PHONE_OWNER_INITIALS, 36.dp, 10.8f)
                Text(PHONE_OWNER, style = inter(13f), modifier = Modifier.weight(1f))
                Image(painterResource(R.drawable.ic_lock_keyhole), contentDescription = "Celular bloqueado")
            }
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(now.clock(), style = inter(70f, FontWeight.Light, lineHeight = 1.05f))
                Text(now.longDate(), style = plexMono(11f, color = InkSoft))
            }
            Column(
                modifier = Modifier.fillMaxWidth().panel().clickable(onClickLabel = "Abrir mensagem", onClick = onOpenNotification).padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
                        Image(painterResource(R.drawable.ic_send_notif), contentDescription = null)
                        Text("Send · ${track.contactName}", style = inter(11f, FontWeight.SemiBold, Moss))
                    }
                    Text("há 2 min", style = plexMono(10f))
                }
                Text(track.message, style = inter(12f), maxLines = 3, overflow = TextOverflow.Ellipsis)
            }
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier.size(56.dp).panel(28.dp).clickable(onClickLabel = "Desbloquear", onClick = onUnlock),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(painterResource(R.drawable.ic_unlock), contentDescription = "Desbloquear")
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Deslize para desbloquear", style = inter(16f, FontWeight.SemiBold), textAlign = TextAlign.Center)
                    Text("Arraste o ponto até o fim ou toque no cadeado", style = plexMono(11f, color = InkSoft), textAlign = TextAlign.Center)
                }
                UnlockTrack(onUnlock)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Emergência", style = inter(11f, color = InkSoft))
                Text("Esqueci a senha", style = inter(11f, color = InkSoft))
            }
        }
    }
}

@Composable
private fun UnlockTrack(onUnlock: () -> Unit) {
    val thumb = 28.dp
    BoxWithConstraints(Modifier.fillMaxWidth().height(thumb)) {
        val maxOffset = with(LocalDensity.current) { (maxWidth - thumb).toPx() }
        var offset by remember { mutableFloatStateOf(0f) }
        Box(Modifier.align(Alignment.Center).fillMaxWidth().padding(horizontal = 12.dp).height(2.dp).clip(CircleShape).background(Line))
        Box(Modifier.align(Alignment.CenterEnd).width(thumb), contentAlignment = Alignment.Center) {
            Image(painterResource(R.drawable.ic_ellipse_dot), contentDescription = null)
        }
        Box(
            modifier = Modifier.offset { IntOffset(offset.roundToInt(), 0) }.size(thumb)
                .draggable(
                    state = rememberDraggableState { delta -> offset = (offset + delta).coerceIn(0f, maxOffset) },
                    orientation = Orientation.Horizontal,
                    onDragStopped = { if (offset > maxOffset * 0.7f) onUnlock() else offset = 0f },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Box(Modifier.size(14.dp).clip(CircleShape).background(Moss).border(3.dp, Moss.copy(alpha = 0.35f), CircleShape))
        }
    }
}

@Composable
private fun HomeScreen(
    track: InvestigationTrack,
    definition: InvestigationDefinition,
    session: InvestigationSession,
    onOpen: (PhoneApp) -> Unit,
    dispatch: (GameAction) -> Unit,
) {
    val now = rememberNow()
    val unreadClues = track.clues.count { it.appId == SimulatedAppId.MESSAGES && session.statusOf(it.id) == EvidenceStatus.AVAILABLE }
    PhoneScaffold(wallpaper = true) {
        // Em telas baixas (TV Box em 720p) a tela inicial rola em vez de cortar o dock.
        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).heightIn(min = maxHeight)
                    .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    InitialsAvatar(PHONE_OWNER_INITIALS)
                    Spacer(Modifier.width(10.dp))
                    Text(PHONE_OWNER, style = inter(12f, color = InkSoft), modifier = Modifier.weight(1f))
                    InvestigationMenuButton(R.drawable.ic_sliders, dispatch)
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(now.clock(), style = inter(62f, FontWeight.Light, lineHeight = 1f))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(now.shortDate(), style = plexMono(10f, color = InkSoft))
                        Text("17° · Nublado", style = plexMono(10f, color = InkSoft))
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().panel().padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(painterResource(R.drawable.ic_calendar), contentDescription = null)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(track.objective, style = inter(12f, FontWeight.Medium))
                        Text("CAPI · ${definition.title.uppercase()}", style = plexMono(10f))
                    }
                }
                Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(23.dp)) {
                    listOf(
                        listOf(PhoneApp.NAVEGADOR, PhoneApp.CONTATOS, PhoneApp.SMS),
                        listOf(PhoneApp.TMAIL, PhoneApp.GALERIA, PhoneApp.SOCIAL),
                        listOf(PhoneApp.SEND, PhoneApp.CONFIGURACOES, PhoneApp.LOJA),
                    ).forEach { row ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            row.forEach { app -> AppTile(app, badge = if (app == PhoneApp.SEND) unreadClues else 0, onClick = { onOpen(app) }) }
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().height(34.dp),
                    horizontalArrangement = Arrangement.spacedBy(9.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(painterResource(R.drawable.ic_search_home), contentDescription = null)
                    Text("Buscar no celular", style = inter(11f, color = InkSoft))
                }
                Row(
                    modifier = Modifier.fillMaxWidth().panel(22.dp).padding(horizontal = 38.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    AppTile(PhoneApp.MUSICAS, badge = 0, onClick = { onOpen(PhoneApp.MUSICAS) })
                    AppTile(PhoneApp.TELEFONE, badge = 0, onClick = { onOpen(PhoneApp.TELEFONE) })
                }
            }
        }
    }
}

@Composable
private fun AppTile(app: PhoneApp, badge: Int, onClick: () -> Unit) {
    Column(
        modifier = Modifier.width(100.dp).clip(RoundedCornerShape(12.dp)).clickable(onClickLabel = "Abrir ${app.title}", onClick = onClick)
            .padding(bottom = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Box(Modifier.padding(top = 6.dp)) {
            AppIcon(app)
            if (badge > 0) CountBadge(badge, Modifier.align(Alignment.TopEnd).offset(x = 5.dp, y = (-6).dp))
        }
        Text(app.title, style = inter(11f), maxLines = 1)
    }
}

@Composable
fun AppIcon(app: PhoneApp) {
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier = Modifier.size(56.dp).clip(shape).background(app.tint.tile).border(1.dp, app.tint.accent.copy(alpha = 0.4f), shape),
        contentAlignment = Alignment.Center,
    ) {
        Image(painterResource(app.icon), contentDescription = null)
    }
}

/** Apps sem pistas neste caso: existem para o celular parecer real e incentivar a investigação. */
@Composable
private fun PlaceholderApp(app: PhoneApp, onHome: () -> Unit) {
    PhoneScaffold(onHome = onHome) {
        AppHeader(app.title, PHONE_OWNER, app.tint.accent, onBack = onHome)
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        ) {
            AppIcon(app)
            Text("Nada sobre este caso por aqui.", style = inter(15f, FontWeight.SemiBold, Ink), textAlign = TextAlign.Center)
            Text("As pistas estão no Send e no Navegador.", style = inter(12f, color = InkSoft), textAlign = TextAlign.Center)
        }
    }
}
