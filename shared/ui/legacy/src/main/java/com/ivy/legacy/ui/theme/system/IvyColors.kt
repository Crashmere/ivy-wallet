package com.ivy.legacy.ui.theme.system

import androidx.compose.ui.graphics.Color

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
interface IvyColors {
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
