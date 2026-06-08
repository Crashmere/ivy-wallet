package com.ivy.legacy.ui.theme

import androidx.compose.runtime.Composable
import com.ivy.ui.theme.colors.Gradient

@Composable
internal fun mediumBlur() = LegacyTheme.colors.medium.copy(alpha = 0.95f)

@Composable
internal fun gradientExpenses() = Gradient(LegacyTheme.colors.pureInverse, LegacyTheme.colors.gray)

@Composable
internal fun gradientBlack() = Gradient(LegacyTheme.colors.gray, LegacyTheme.colors.pureInverse)
