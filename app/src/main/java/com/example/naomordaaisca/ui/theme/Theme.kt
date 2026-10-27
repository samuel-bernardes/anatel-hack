package com.example.naomordaaisca.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/** O jogo simula um celular com tema escuro fixo, como no Figma. */
@Composable
fun NaoMordaAIscaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Moss,
            onPrimary = MossInk,
            secondary = SendCyan,
            tertiary = BrowserBlue,
            background = Night,
            onBackground = Ink,
            surface = Panel,
            onSurface = Ink,
            surfaceVariant = Field,
            onSurfaceVariant = InkSoft,
            outline = Line,
        ),
        content = content,
    )
}
