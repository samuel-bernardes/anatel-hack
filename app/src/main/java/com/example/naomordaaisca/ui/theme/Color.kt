package com.example.naomordaaisca.ui.theme

import androidx.compose.ui.graphics.Color

// Tokens do Figma "Não Morda a Isca" (tema maré noturna).
val Night = Color(0xFF0C1414)
val Panel = Color(0xFF142221)
val PanelTranslucent = Color(0xE6142221)
val Field = Color(0xFF1C2C2A)
val Line = Color(0xFF334641)

val Ink = Color(0xFFE5EAE1)
val InkSoft = Color(0xFFADBAB0)
val InkMuted = Color(0xFF7D9488)

val Moss = Color(0xFFB4CC98)
val MossDeep = Color(0xFF263A2D)
val MossInk = Color(0xFF142017)

val SendCyan = Color(0xFF8DD6E3)
val SendBubble = Color(0xFF20515A)
val BrowserBlue = Color(0xFF83C4F3)
val BrowserTile = Color(0xFF243F57)
val Alert = Color(0xFFF0A3A5)

/** Cor de fundo e de destaque de cada atalho na tela inicial. */
data class AppTint(val tile: Color, val accent: Color)

val NavegadorTint = AppTint(Color(0xFF243F57), Color(0xFF83C4F3))
val ContatosTint = AppTint(Color(0xFF57402B), Color(0xFFEDBF85))
val SmsTint = AppTint(Color(0xFF285044), Color(0xFF9CD8AE))
val TmailTint = AppTint(Color(0xFF513138), Color(0xFFF0A3A5))
val GaleriaTint = AppTint(Color(0xFF493757), Color(0xFFCCA8F0))
val SocialTint = AppTint(Color(0xFF32426B), Color(0xFFA8B9FF))
val SendTint = AppTint(Color(0xFF20515A), Color(0xFF8DD6E3))
val ConfiguracoesTint = AppTint(Color(0xFF3E4955), Color(0xFFBCCBD9))
val LojaTint = AppTint(Color(0xFF4D3F63), Color(0xFFCAB1F1))
val MusicasTint = AppTint(Color(0xFF5B344C), Color(0xFFE9A1C9))
val TelefoneTint = AppTint(Color(0xFF314E38), Color(0xFFADD39A))
