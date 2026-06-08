package com.ivy.legacy.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.ivy.legacy.ui.theme.system.LegacyTheme as SystemLegacyTheme

object LegacyTheme {
    val colors: LegacyColors
        @Composable
        @ReadOnlyComposable
        get() = SystemLegacyTheme.colors

    val typo: LegacyTypography
        @Composable
        @ReadOnlyComposable
        get() = SystemLegacyTheme.typo

    val shapes: LegacyShapes
        @Composable
        @ReadOnlyComposable
        get() = SystemLegacyTheme.shapes
}

interface LegacyColors {
    val pure: Color
    val pureInverse: Color

    val gray: Color
    val medium: Color
    val mediumInverse: Color

    val primary: Color
    val primary1: Color

    val green: Color
    val green1: Color

    val orange: Color
    val orange1: Color

    val red: Color
    val red1: Color
    val red1Inverse: Color

    val isLight: Boolean
}

interface LegacyTypography {
    val h1: TextStyle
    val h2: TextStyle
    val b1: TextStyle
    val b2: TextStyle
    val c: TextStyle

    val nH1: TextStyle
    val nH2: TextStyle
    val nB1: TextStyle
    val nB2: TextStyle
    val nC: TextStyle
}

interface LegacyShapes {
    val r1: CornerBasedShape
    val r1Top: CornerBasedShape
    val r1Bot: CornerBasedShape

    val r2: CornerBasedShape
    val r2Top: CornerBasedShape
    val r2Bot: CornerBasedShape

    val r3: CornerBasedShape
    val r3Top: CornerBasedShape
    val r3Bot: CornerBasedShape

    val r4: CornerBasedShape
    val r4Top: CornerBasedShape
    val r4Bot: CornerBasedShape

    val rFull: CornerBasedShape
        get() = RoundedCornerShape(percent = 50)

    val circle: CornerBasedShape
        get() = CircleShape
}
