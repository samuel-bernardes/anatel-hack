package com.example.naomordaaisca.ui.hub

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.naomordaaisca.R
import com.example.naomordaaisca.game.AgeBand
import com.example.naomordaaisca.game.GameAction
import com.example.naomordaaisca.game.GamePresentation
import com.example.naomordaaisca.game.InteractableId
import com.example.naomordaaisca.game.InvestigationCatalog
import com.example.naomordaaisca.game.InvestigationSession
import com.example.naomordaaisca.game.InvestigationStage
import com.example.naomordaaisca.game.MissionCatalog
import com.example.naomordaaisca.game.MissionId
import com.example.naomordaaisca.game.label
import com.example.naomordaaisca.ui.investigation.AppIcon
import com.example.naomordaaisca.ui.investigation.PhoneApp
import com.example.naomordaaisca.ui.kit.CapiNote
import com.example.naomordaaisca.ui.kit.PrimaryButton
import com.example.naomordaaisca.ui.kit.SectionLabel
import com.example.naomordaaisca.ui.kit.label
import com.example.naomordaaisca.ui.kit.panel
import com.example.naomordaaisca.ui.theme.Field
import com.example.naomordaaisca.ui.theme.Ink
import com.example.naomordaaisca.ui.theme.InkMuted
import com.example.naomordaaisca.ui.theme.InkSoft
import com.example.naomordaaisca.ui.theme.Line
import com.example.naomordaaisca.ui.theme.Moss
import com.example.naomordaaisca.ui.theme.MossInk
import com.example.naomordaaisca.ui.theme.Night
import com.example.naomordaaisca.ui.theme.inter
import com.example.naomordaaisca.ui.theme.plexMono

@Composable
fun HubScreen(presentation: GamePresentation, investigation: InvestigationSession?, dispatch: (GameAction) -> Unit) {
    val definition = InvestigationCatalog.definition(MissionId.DIGITAL_OFFERS)
    val track = InvestigationCatalog.track(MissionId.DIGITAL_OFFERS, investigation?.ageBand ?: presentation.selectedAgeBand)
    Box(Modifier.fillMaxSize().background(Night)) {
        Image(
            painterResource(R.drawable.bg_wallpaper_mare_noturna), contentDescription = null,
            contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize(),
        )
        Column(
            modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SectionLabel("Jogo de investigação digital", Moss)
                Text("Não Morda a Isca!", style = inter(34f, FontWeight.SemiBold, lineHeight = 1.15f))
            }
            CapiNote("Capi", "Abra os dispositivos e investigue o que acontece. Nem toda oferta é golpe, e nem toda mensagem é confiável.")

            SectionLabel("Faixa etária")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AgeBand.entries.forEach { band ->
                    AgeChip(band, selected = presentation.selectedAgeBand == band, modifier = Modifier.weight(1f)) {
                        dispatch(GameAction.SelectAgeBand(band))
                    }
                }
            }

            if (definition != null && track != null) {
                Column(
                    modifier = Modifier.fillMaxWidth().panel(20.dp).padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        AppIcon(PhoneApp.SEND)
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text("CELULAR · SEND E NAVEGADOR", style = plexMono(10f))
                            Text(definition.title, style = inter(18f, FontWeight.SemiBold))
                        }
                    }
                    Text(track.objective, style = inter(12.5f, color = InkSoft))
                    PrimaryButton(
                        text = when {
                            investigation == null -> "Pegar o celular"
                            investigation.stage == InvestigationStage.COMPLETED -> "Rever investigação"
                            else -> "Continuar investigação"
                        },
                        onClick = { dispatch(GameAction.OpenInvestigation(MissionId.DIGITAL_OFFERS)) },
                    )
                    if (investigation != null) Text("Tentativa salva · ${investigation.ageBand.label()}", style = plexMono(10f))
                }
            }
            Text(
                "${presentation.completedMissionCount}/${presentation.missionCount} missões concluídas".uppercase(),
                style = plexMono(10f, color = Moss),
            )

            SectionLabel("Outras experiências")
            MissionCatalog.missions.filter { it.id != MissionId.DIGITAL_OFFERS }.forEach { mission ->
                val missionTrack = mission.ageTracks[presentation.selectedAgeBand] ?: return@forEach
                val done = mission.id in presentation.completedMissionIds
                Row(
                    modifier = Modifier.fillMaxWidth().panel(16.dp).clickable(onClickLabel = "Abrir missão") { dispatch(GameAction.OpenMission(mission.id)) }
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AppIcon(if (mission.interactable == InteractableId.COMPUTER) PhoneApp.NAVEGADOR else PhoneApp.SMS)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(mission.title, style = inter(14f, FontWeight.SemiBold))
                        Text(missionTrack.objective, style = inter(12f, color = InkSoft))
                        Text(
                            "${mission.interactable.label()}${if (done) " · ✓ concluída" else ""}".uppercase(),
                            style = plexMono(9f, color = if (done) Moss else InkMuted),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AgeChip(band: AgeBand, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = modifier.heightIn(min = 52.dp).clip(shape).background(if (selected) Moss else Field)
            .border(1.dp, if (selected) Moss else Line, shape).clickable(onClick = onClick).padding(horizontal = 10.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            (if (selected) "✓ " else "") + band.label(),
            style = inter(13f, FontWeight.SemiBold, if (selected) MossInk else Ink),
            textAlign = TextAlign.Center,
        )
    }
}
