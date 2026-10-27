package com.example.naomordaaisca.ui.investigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.naomordaaisca.R
import com.example.naomordaaisca.game.EvidenceStatus
import com.example.naomordaaisca.game.GameAction
import com.example.naomordaaisca.game.InvestigationSession
import com.example.naomordaaisca.game.InvestigationTrack
import com.example.naomordaaisca.game.SimulatedAppId
import com.example.naomordaaisca.ui.kit.AppHeader
import com.example.naomordaaisca.ui.kit.EvidenceCard
import com.example.naomordaaisca.ui.kit.IconButton
import com.example.naomordaaisca.ui.kit.InvestigationMenuButton
import com.example.naomordaaisca.ui.kit.PHONE_OWNER
import com.example.naomordaaisca.ui.kit.PhoneScaffold
import com.example.naomordaaisca.ui.kit.SearchField
import com.example.naomordaaisca.ui.kit.greeting
import com.example.naomordaaisca.ui.kit.rememberNow
import com.example.naomordaaisca.ui.kit.statusOf
import com.example.naomordaaisca.ui.theme.BrowserBlue
import com.example.naomordaaisca.ui.theme.BrowserTile
import com.example.naomordaaisca.ui.theme.InkMuted
import com.example.naomordaaisca.ui.theme.InkSoft
import com.example.naomordaaisca.ui.theme.Panel
import com.example.naomordaaisca.ui.theme.inter
import com.example.naomordaaisca.ui.theme.plexMono

@Composable
fun BrowserApp(
    track: InvestigationTrack,
    session: InvestigationSession,
    onBack: () -> Unit,
    onHome: () -> Unit,
    dispatch: (GameAction) -> Unit,
) {
    val clues = track.clues.filter { it.appId == SimulatedAppId.BROWSER }
    val noted = clues.count { session.statusOf(it.id) == EvidenceStatus.USED }
    PhoneScaffold(onHome = onHome) {
        AppHeader("Navegador", PHONE_OWNER, BrowserBlue, trailing = { InvestigationMenuButton(R.drawable.ic_ellipsis_vertical, dispatch) })
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SearchField("Pesquisar ou digitar endereço")
            Row(Modifier.fillMaxWidth().padding(top = 7.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("${rememberNow().greeting()}, ${PHONE_OWNER.substringBefore(' ')}.", style = inter(22f))
                    Text("Seu canto da internet.", style = inter(11f, color = InkMuted))
                }
                Image(painterResource(R.drawable.ic_cloud), contentDescription = null)
            }
            Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Shortcut(R.drawable.ic_landmark, "Arquivo")
                Shortcut(R.drawable.ic_mail_nav, "Tmail")
                Shortcut(R.drawable.ic_newspaper, "Notícias")
                Shortcut(R.drawable.ic_plus_nav, "Adicionar")
            }
            Section("Para conferir", "$noted de ${clues.size} anotadas")
            Text("Confira as informações sem usar o link encaminhado como única fonte.", style = inter(11f, color = InkMuted))
            clues.forEach { clue ->
                EvidenceCard(
                    title = clue.title,
                    content = clue.content,
                    status = session.statusOf(clue.id),
                    accent = BrowserBlue,
                    actionColor = BrowserTile,
                    onDiscover = { dispatch(GameAction.DiscoverEvidence(clue.id)) },
                    onUse = { dispatch(GameAction.UseEvidence(clue.id)) },
                )
            }
            Section("Para ler com calma", "Personalizar")
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Panel)) {
                Image(
                    painterResource(R.drawable.photo_river), contentDescription = null, contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(140.dp),
                )
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("CIDADE · CULTURA", style = plexMono(9f, color = BrowserBlue))
                    Text("O rio também guarda a memória da cidade", style = inter(17f, FontWeight.SemiBold))
                    Text("Caminhadas, histórias e lugares para redescobrir no fim de semana.", style = inter(11f, color = InkSoft))
                    Text("Jornal da margem · 5 min de leitura", style = plexMono(9f))
                }
            }
            Text("Na sua região", style = inter(13f, FontWeight.SemiBold), modifier = Modifier.padding(top = 4.dp))
            Image(
                painterResource(R.drawable.photo_cafes), contentDescription = null, contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(12.dp)),
            )
            Text("Pequenos cafés para dias de chuva", style = inter(16f))
        }
        Row(
            modifier = Modifier.fillMaxWidth().height(49.dp).background(Panel).padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(R.drawable.ic_chevron_left_nav, "Voltar", onBack)
            DecorativeIcon(R.drawable.ic_chevron_right_nav)
            DecorativeIcon(R.drawable.ic_share)
            IconButton(R.drawable.ic_square, "Tela inicial", onHome)
            DecorativeIcon(R.drawable.ic_menu)
        }
    }
}

/** Botão só visual da barra do navegador: não recebe foco do controle remoto, porque não faz nada. */
@Composable
private fun DecorativeIcon(icon: Int) {
    Box(Modifier.size(32.dp), contentAlignment = Alignment.Center) {
        Image(painterResource(icon), contentDescription = null)
    }
}

@Composable
private fun Shortcut(icon: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Box(Modifier.size(46.dp).clip(RoundedCornerShape(14.dp)).background(BrowserTile), contentAlignment = Alignment.Center) {
            Image(painterResource(icon), contentDescription = null)
        }
        Text(label, style = inter(10f, color = InkSoft))
    }
}

@Composable
private fun Section(title: String, trailing: String) {
    Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(title, style = inter(13f, FontWeight.SemiBold))
        Text(trailing, style = inter(11f, color = BrowserBlue))
    }
}
