package com.ivy.legacy.ui.theme.system

import androidx.compose.ui.text.TextStyle

@Deprecated("Legacy UI. Prefer Material3 and shared:ui:core for new code.")
interface IvyTypography {
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
