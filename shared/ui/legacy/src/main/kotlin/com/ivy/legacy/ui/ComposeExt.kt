package com.ivy.legacy.ui

import android.annotation.SuppressLint
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.Insets
import androidx.core.view.WindowInsetsCompat
import com.ivy.ui.compose.densityScope
import com.ivy.ui.navigation.navigation

@Composable
fun windowInsets(): WindowInsetsCompat {
    val rootView = LocalView.current
    return WindowInsetsCompat.toWindowInsetsCompat(rootView.rootWindowInsets, rootView)
}

@Composable
fun statusBarInset(): Int {
    val windowInsets = windowInsets()
    return windowInsets.getInsets(WindowInsetsCompat.Type.statusBars()).top
}

@Composable
fun navigationBarInset(): Int {
    return navigationBarInsets().bottom
}

@Composable
fun navigationBarInsets(): Insets {
    val windowInsets = windowInsets()
    return windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars())
}

@Composable
fun keyboardOnlyWindowInsets(): Insets {
    val windowInsets = windowInsets()
    return windowInsets.getInsets(
        WindowInsetsCompat.Type.ime()
    )
}

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

fun selectEndTextFieldValue(text: String?) = TextFieldValue(
    text = text ?: "",
    selection = TextRange(text?.length ?: 0)
)

@Composable
fun Dp.toDensityPx() = densityScope { toPx() }

@Composable
fun Int.toDensityDp() = densityScope { toDp() }

@Composable
fun Float.toDensityDp() = densityScope { toDp() }
