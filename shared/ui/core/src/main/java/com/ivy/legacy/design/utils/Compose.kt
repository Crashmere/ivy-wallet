package com.ivy.legacy.design.utils

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

@Deprecated("Old design system. Use `:ivy-design` and Material3")
@Composable
fun <T> densityScope(densityScope: @Composable Density.() -> T): T {
    return with(LocalDensity.current) { densityScope() }
}

@Deprecated("Old design system. Use `:ivy-design` and Material3")
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

@Deprecated("Old design system. Use `:ivy-design` and Material3")
fun Modifier.thenWhen(
    logic: Modifier.() -> Modifier?
): Modifier {
    return this.logic() ?: this
}

@Composable
fun rememberInteractionSource(): MutableInteractionSource = remember { MutableInteractionSource() }
