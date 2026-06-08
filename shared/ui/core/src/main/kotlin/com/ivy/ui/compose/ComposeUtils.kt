package com.ivy.ui.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun <T> densityScope(densityScope: @Composable Density.() -> T): T {
    return with(LocalDensity.current) { densityScope() }
}

fun Modifier.thenIf(
    condition: Boolean,
    otherModifier: Modifier.() -> Modifier
): Modifier {
    // Cannot use Modifier#then() because it stacks the previous modifiers multiple times
    return if (condition) {
        this.otherModifier()
    } else {
        this
    }
}

fun Modifier.thenWhen(
    logic: Modifier.() -> Modifier?
): Modifier {
    return this.logic() ?: this
}

@Composable
fun rememberInteractionSource(): MutableInteractionSource = remember { MutableInteractionSource() }

fun Modifier.consumeClicks(interactionSource: MutableInteractionSource) =
    clickableNoIndication(interactionSource) {
        // consume click
    }

fun Modifier.clickableNoIndication(
    interactionSource: MutableInteractionSource,
    onClick: () -> Unit
): Modifier {
    return this.clickable(
        interactionSource = interactionSource,
        onClick = onClick,
        role = null,
        indication = null
    )
}

fun selectEndTextFieldValue(text: String?) = TextFieldValue(
    text = text ?: "",
    selection = TextRange(text?.length ?: 0)
)

fun Modifier.drawColoredShadow(
    color: Color,
    alpha: Float = 0.15f,
    borderRadius: Dp = 0.dp,
    shadowRadius: Dp = 16.dp,
    offsetX: Dp = 0.dp,
    offsetY: Dp = 8.dp
) = this.drawBehind {
    val transparentColor = android.graphics.Color.toArgb(color.copy(alpha = 0.0f).value.toLong())
    val shadowColor = android.graphics.Color.toArgb(color.copy(alpha = alpha).value.toLong())
    this.drawIntoCanvas {
        val paint = Paint()
        val frameworkPaint = paint.asFrameworkPaint()
        frameworkPaint.color = transparentColor
        frameworkPaint.setShadowLayer(
            shadowRadius.toPx(),
            offsetX.toPx(),
            offsetY.toPx(),
            shadowColor
        )
        it.drawRoundRect(
            0f,
            0f,
            this.size.width,
            this.size.height,
            borderRadius.toPx(),
            borderRadius.toPx(),
            paint
        )
    }
}
