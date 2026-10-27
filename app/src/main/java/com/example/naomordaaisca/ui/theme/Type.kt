package com.example.naomordaaisca.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.naomordaaisca.R

val Inter = FontFamily(
    Font(R.font.inter_light, FontWeight.Light),
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
)

val PlexMono = FontFamily(
    Font(R.font.ibm_plex_mono_regular, FontWeight.Normal),
    Font(R.font.ibm_plex_mono_semibold, FontWeight.SemiBold),
)

/** Texto corrido e títulos usam Inter; o Figma usa altura de linha 1,45. */
fun inter(size: Float, weight: FontWeight = FontWeight.Normal, color: Color = Ink, lineHeight: Float = 1.45f) =
    TextStyle(fontFamily = Inter, fontWeight = weight, fontSize = size.sp, lineHeight = (size * lineHeight).sp, color = color)

/** Rótulos, horários e metadados usam IBM Plex Mono. */
fun plexMono(size: Float, weight: FontWeight = FontWeight.Normal, color: Color = InkMuted) =
    TextStyle(fontFamily = PlexMono, fontWeight = weight, fontSize = size.sp, lineHeight = (size * 1.45f).sp, color = color)
