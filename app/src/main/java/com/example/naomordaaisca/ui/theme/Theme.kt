package com.example.naomordaaisca.ui.theme

import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

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
    ) {
        CompositionLocalProvider(LocalIndication provides FocusRingIndication, content = content)
    }
}

/**
 * Na TV Box o jogo é usado com controle remoto: o item focado ganha um contorno claro.
 * No toque não há foco, então só aparece o leve destaque ao pressionar.
 */
private object FocusRingIndication : IndicationNodeFactory {
    override fun create(interactionSource: InteractionSource): DelegatableNode = FocusRingNode(interactionSource)
    override fun equals(other: Any?): Boolean = other === this
    override fun hashCode(): Int = System.identityHashCode(this)
}

private class FocusRingNode(private val interactionSource: InteractionSource) : Modifier.Node(), DrawModifierNode {
    private var focused = false
    private var pressed = false

    override fun onAttach() {
        coroutineScope.launch {
            var focusCount = 0
            var pressCount = 0
            interactionSource.interactions.collect { interaction ->
                when (interaction) {
                    is FocusInteraction.Focus -> focusCount++
                    is FocusInteraction.Unfocus -> focusCount--
                    is PressInteraction.Press -> pressCount++
                    is PressInteraction.Release, is PressInteraction.Cancel -> pressCount--
                }
                if (focused != focusCount > 0 || pressed != pressCount > 0) {
                    focused = focusCount > 0
                    pressed = pressCount > 0
                    invalidateDraw()
                }
            }
        }
    }

    override fun ContentDrawScope.draw() {
        drawContent()
        if (pressed || focused) drawRect(Ink.copy(alpha = 0.12f))
        if (focused) {
            // Contorno claro com filete escuro por dentro: visível sobre fundos escuros e sobre o verde dos botões.
            val outer = 3.dp.toPx()
            val inner = 1.5.dp.toPx()
            val radius = minOf(14.dp.toPx(), size.minDimension / 2)
            drawRoundRect(
                Ink, Offset(outer / 2, outer / 2), Size(size.width - outer, size.height - outer),
                CornerRadius(radius), Stroke(outer),
            )
            drawRoundRect(
                Night, Offset(outer + inner / 2, outer + inner / 2), Size(size.width - 2 * outer - inner, size.height - 2 * outer - inner),
                CornerRadius((radius - outer).coerceAtLeast(0f)), Stroke(inner),
            )
        }
    }
}
