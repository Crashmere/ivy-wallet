package com.ivy.legacy.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.Insets
import androidx.core.view.WindowInsetsCompat
import com.ivy.ui.navigation.navigation

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
@Composable
fun windowInsets(): WindowInsetsCompat {
    val rootView = LocalView.current
    return WindowInsetsCompat.toWindowInsetsCompat(rootView.rootWindowInsets, rootView)
}

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
@Composable
fun statusBarInset(): Int {
    val windowInsets = windowInsets()
    return windowInsets.getInsets(WindowInsetsCompat.Type.statusBars()).top
}

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
@Composable
fun navigationBarInset(): Int {
    return navigationBarInsets().bottom
}

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
@Composable
fun navigationBarInsets(): Insets {
    val windowInsets = windowInsets()
    return windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars())
}

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
@Composable
fun keyboardOnlyWindowInsets(): Insets {
    val windowInsets = windowInsets()
    return windowInsets.getInsets(
        WindowInsetsCompat.Type.ime()
    )
}

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
@Composable
fun <T> densityScope(densityScope: @Composable Density.() -> T): T {
    return with(LocalDensity.current) { densityScope() }
}

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
@SuppressLint("ComposableNaming")
@Composable
fun onScreenStart(
    cleanUp: () -> Unit = {},
    start: () -> Unit
) {
    val latestStart by rememberUpdatedState(start)
    val latestCleanup by rememberUpdatedState(cleanUp)
    DisposableEffect(navigation().currentScreen) {
        latestStart()
        onDispose { latestCleanup() }
    }
}

@Composable
fun rememberInteractionSource(): MutableInteractionSource = remember { MutableInteractionSource() }

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
fun Modifier.consumeClicks(interactionSource: MutableInteractionSource) =
    clickableNoIndication(interactionSource) {
        // consume click
    }

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
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

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
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

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
fun selectEndTextFieldValue(text: String?) = TextFieldValue(
    text = text ?: "",
    selection = TextRange(text?.length ?: 0)
)

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
@Composable
fun Dp.toDensityPx() = densityScope { toPx() }

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
@Composable
fun Int.toDensityDp() = densityScope { toDp() }

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
@Composable
fun Float.toDensityDp() = densityScope { toDp() }
