package com.example.naomordaaisca.ui.kit

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.naomordaaisca.R
import com.example.naomordaaisca.game.EvidenceStatus
import com.example.naomordaaisca.game.GameAction
import com.example.naomordaaisca.ui.theme.Alert
import com.example.naomordaaisca.ui.theme.Field
import com.example.naomordaaisca.ui.theme.Ink
import com.example.naomordaaisca.ui.theme.InkMuted
import com.example.naomordaaisca.ui.theme.InkSoft
import com.example.naomordaaisca.ui.theme.Line
import com.example.naomordaaisca.ui.theme.Moss
import com.example.naomordaaisca.ui.theme.MossDeep
import com.example.naomordaaisca.ui.theme.MossInk
import com.example.naomordaaisca.ui.theme.Night
import com.example.naomordaaisca.ui.theme.Panel
import com.example.naomordaaisca.ui.theme.PanelTranslucent
import com.example.naomordaaisca.ui.theme.inter
import com.example.naomordaaisca.ui.theme.plexMono
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Superfície com borda fina usada por cartões, notificações e campos do Figma. */
fun Modifier.panel(radius: Dp = 16.dp, color: Color = PanelTranslucent, border: Color = Line): Modifier {
    val shape = RoundedCornerShape(radius)
    return clip(shape).background(color, shape).border(1.dp, border, shape)
}

/** Moldura do celular simulado: barra de estado, conteúdo e indicador de início. */
@Composable
fun PhoneScaffold(
    wallpaper: Boolean = false,
    onHome: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(Modifier.fillMaxSize().background(Night)) {
        if (wallpaper) {
            Image(
                painter = painterResource(R.drawable.bg_wallpaper_mare_noturna),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
            StatusBar()
            Column(Modifier.weight(1f).fillMaxWidth(), content = content)
            HomeIndicator(onHome)
        }
    }
}

@Composable
fun StatusBar() {
    Row(
        modifier = Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("elo • 4G", style = plexMono(11f, color = InkSoft))
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(painterResource(R.drawable.ic_signal), contentDescription = null)
            Image(painterResource(R.drawable.ic_wifi), contentDescription = null)
            Text("68%", style = plexMono(11f, color = InkSoft))
            Image(painterResource(R.drawable.ic_battery), contentDescription = null)
        }
    }
}

/** O indicador de início funciona como o gesto de voltar à tela inicial. */
@Composable
fun HomeIndicator(onHome: (() -> Unit)?) {
    val clickable = if (onHome != null) Modifier.clickable(onClickLabel = "Tela inicial", onClick = onHome) else Modifier
    Box(Modifier.fillMaxWidth().height(28.dp).then(clickable), contentAlignment = Alignment.Center) {
        Box(Modifier.width(112.dp).height(3.dp).clip(CircleShape).background(InkSoft))
    }
}

@Composable
fun AppHeader(
    title: String,
    subtitle: String,
    titleColor: Color,
    titleSize: Float = 25f,
    onBack: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(76.dp).padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) IconButton(R.drawable.ic_chevron_left, "Voltar", onBack)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, style = inter(titleSize, FontWeight.SemiBold, titleColor), maxLines = 1)
            Text(subtitle, style = plexMono(10f), maxLines = 1)
        }
        trailing?.invoke()
    }
    HorizontalLine()
}

@Composable
fun HorizontalLine() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(Line))
}

@Composable
fun IconButton(icon: Int, description: String?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(32.dp).clip(CircleShape).clickable(onClickLabel = description, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Image(painterResource(icon), contentDescription = description)
    }
}

@Composable
fun InitialsAvatar(initials: String, size: Dp = 32.dp, textSize: Float = 9.6f) {
    Box(
        modifier = Modifier.size(size).clip(CircleShape).background(MossDeep).border(1.dp, Line, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(initials, style = plexMono(textSize, color = Moss))
    }
}

@Composable
fun PhotoAvatar(photo: Int, size: Dp = 42.dp) {
    Image(
        painter = painterResource(photo),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.size(size).clip(CircleShape),
    )
}

@Composable
fun CountBadge(count: Int, modifier: Modifier = Modifier, color: Color = Moss, textColor: Color = MossInk, size: Dp = 19.dp) {
    Box(modifier.size(size).clip(CircleShape).background(color), contentAlignment = Alignment.Center) {
        Text(count.toString(), style = plexMono(10f, FontWeight.SemiBold, textColor))
    }
}

@Composable
fun SearchField(placeholder: String) {
    Row(
        modifier = Modifier.fillMaxWidth().height(44.dp).panel(12.dp, Field).padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(painterResource(R.drawable.ic_search_field), contentDescription = null)
        Text(placeholder, style = inter(13f, color = InkMuted), maxLines = 1)
    }
}

@Composable
fun SectionLabel(text: String, color: Color = InkMuted, modifier: Modifier = Modifier) {
    Text(text.uppercase(PtBr), style = plexMono(10f, color = color), modifier = modifier)
}

@Composable
fun CenteredLabel(text: String, color: Color = InkMuted) {
    Text(
        text.uppercase(PtBr),
        style = plexMono(9f, color = color),
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Box(
        modifier = modifier.fillMaxWidth().heightIn(min = 48.dp).alpha(if (enabled) 1f else 0.4f)
            .clip(RoundedCornerShape(12.dp)).background(Moss)
            .clickable(enabled = enabled, onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = inter(14f, FontWeight.SemiBold, MossInk), textAlign = TextAlign.Center)
    }
}

@Composable
fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, color: Color = Ink) {
    Box(
        modifier = modifier.fillMaxWidth().heightIn(min = 48.dp).panel(12.dp, Field)
            .clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = inter(14f, FontWeight.Medium, color), textAlign = TextAlign.Center)
    }
}

@Composable
fun CapiAvatar(size: Dp = 32.dp) {
    Box(
        modifier = Modifier.size(size).clip(CircleShape).background(MossDeep).border(1.dp, Moss.copy(alpha = 0.5f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text("CP", style = plexMono(size.value * 0.32f, FontWeight.SemiBold, Moss))
    }
}

/** Fala da Capi: a guia do jogo aparece como um cartão destacado em verde. */
@Composable
fun CapiNote(label: String, body: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().panel(16.dp, PanelTranslucent, Moss.copy(alpha = 0.35f)).padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CapiAvatar()
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label.uppercase(PtBr), style = plexMono(10f, color = Moss))
            Text(body, style = inter(12.5f))
        }
    }
}

@Composable
fun InfoCard(label: String, body: String, border: Color = Line, labelColor: Color = InkMuted) {
    Column(
        modifier = Modifier.fillMaxWidth().panel(16.dp, PanelTranslucent, border).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(label.uppercase(PtBr), style = plexMono(10f, color = labelColor))
        Text(body, style = inter(13f))
    }
}

/** Mensagem recebida, com cantos do Figma (canto inferior esquerdo reduzido). */
@Composable
fun IncomingBubble(text: String, time: String, header: (@Composable () -> Unit)? = null) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
        Column(
            modifier = Modifier.widthIn(max = 300.dp)
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomEnd = 12.dp, bottomStart = 3.dp))
                .background(Panel).padding(horizontal = 13.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            header?.invoke()
            Text(text, style = inter(12f))
            Text(time, style = plexMono(9f), modifier = Modifier.align(Alignment.End))
        }
    }
}

/** Resposta que a jogadora pode enviar: tem o visual de mensagem enviada. */
@Composable
fun ReplyOption(label: String, accent: Color, bubble: Color, onClick: () -> Unit) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
        val shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomEnd = 3.dp, bottomStart = 12.dp)
        Column(
            modifier = Modifier.widthIn(max = 310.dp).clip(shape).background(bubble).border(1.dp, accent.copy(alpha = 0.35f), shape)
                .clickable(onClick = onClick).padding(horizontal = 13.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(label, style = inter(12.5f))
            Text("TOQUE PARA ESCOLHER", style = plexMono(9f, color = accent), modifier = Modifier.align(Alignment.End))
        }
    }
}

/**
 * Pista examinável. O estado vem do reducer; o cartão apenas mostra
 * "examinar" (disponível), o conteúdo e "anotar" (descoberta) ou a marcação de anotada.
 */
@Composable
fun EvidenceCard(
    title: String,
    content: String,
    status: EvidenceStatus,
    accent: Color,
    actionColor: Color,
    onDiscover: () -> Unit,
    onUse: (() -> Unit)?,
) {
    val border = when (status) {
        EvidenceStatus.AVAILABLE -> accent.copy(alpha = 0.45f)
        EvidenceStatus.DISCOVERED -> Line
        EvidenceStatus.USED -> Moss.copy(alpha = 0.5f)
    }
    Column(
        modifier = Modifier.fillMaxWidth().panel(12.dp, Panel, border)
            .clickable(enabled = status == EvidenceStatus.AVAILABLE, onClickLabel = "Examinar", onClick = onDiscover)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(painterResource(R.drawable.ic_search_field), contentDescription = null)
            Text(title, style = inter(13f, FontWeight.SemiBold), modifier = Modifier.weight(1f))
        }
        when (status) {
            EvidenceStatus.AVAILABLE -> Text("TOQUE PARA EXAMINAR", style = plexMono(9f, color = accent))
            EvidenceStatus.DISCOVERED -> {
                Text(content, style = inter(12f, color = InkSoft))
                if (onUse != null) {
                    Box(
                        modifier = Modifier.clip(CircleShape).background(actionColor).border(1.dp, accent.copy(alpha = 0.4f), CircleShape)
                            .clickable(onClick = onUse).padding(horizontal = 14.dp, vertical = 8.dp),
                    ) {
                        Text("Anotar como pista", style = inter(12f, FontWeight.Medium))
                    }
                }
            }
            EvidenceStatus.USED -> {
                Text(content, style = inter(12f, color = InkSoft))
                Text("✓ PISTA ANOTADA", style = plexMono(9f, FontWeight.SemiBold, Moss))
            }
        }
    }
}

@Composable
fun ConfirmOverlay(title: String, body: String, confirmLabel: String, cancelLabel: String, onConfirm: () -> Unit, onCancel: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xCC050909))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onCancel),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(24.dp).fillMaxWidth().panel(20.dp, Panel)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(title, style = inter(18f, FontWeight.SemiBold))
            Text(body, style = inter(13f, color = InkSoft))
            Spacer(Modifier.height(4.dp))
            PrimaryButton(confirmLabel, onConfirm)
            SecondaryButton(cancelLabel, onCancel)
        }
    }
}

/** Menu "mais opções" dos apps: reiniciar ou sair da investigação. */
@Composable
fun InvestigationMenuButton(icon: Int, dispatch: (GameAction) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        IconButton(icon, "Mais opções", { open = true })
        DropdownMenu(expanded = open, onDismissRequest = { open = false }, containerColor = Panel) {
            DropdownMenuItem(
                text = { Text("Reiniciar investigação", style = inter(13f)) },
                onClick = { open = false; dispatch(GameAction.RequestInvestigationRestart) },
            )
            DropdownMenuItem(
                text = { Text("Sair da investigação", style = inter(13f, color = Alert)) },
                onClick = { open = false; dispatch(GameAction.RequestInvestigationExit) },
            )
        }
    }
}

val PtBr: Locale = Locale.forLanguageTag("pt-BR")

/** Hora do aparelho, atualizada periodicamente para o relógio do celular simulado. */
@Composable
fun rememberNow(): Date {
    var now by remember { mutableStateOf(Date()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(15_000)
            now = Date()
        }
    }
    return now
}

fun Date.clock(): String = SimpleDateFormat("HH:mm", PtBr).format(this)
fun Date.minutesAgo(minutes: Int): Date = Date(time - minutes * 60_000L)
fun Date.longDate(): String = SimpleDateFormat("EEEE, d 'de' MMMM", PtBr).format(this)
fun Date.shortDate(): String = SimpleDateFormat("EEE, d MMM yyyy", PtBr).format(this).replace(".", "").uppercase(PtBr)
fun Date.greeting(): String = when (SimpleDateFormat("H", PtBr).format(this).toInt()) {
    in 5..11 -> "Bom dia"
    in 12..17 -> "Boa tarde"
    else -> "Boa noite"
}
