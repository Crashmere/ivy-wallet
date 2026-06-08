package com.ivy.ui.modal

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ivy.data.model.Theme
import com.ivy.ui.theme.LocalThemeState

internal object IvyModalTheme {
    val colors: IvyModalColors
        @Composable
        @ReadOnlyComposable
        get() = ivyModalColors(
            theme = LocalThemeState.current.theme,
            isDarkModeEnabled = isSystemInDarkTheme(),
        )

    val shapes: IvyModalShapes = ivyModalShapes()
}

internal interface IvyModalColors {
    val pure: Color
    val pureInverse: Color
    val medium: Color
    val mediumBlur: Color
}

internal interface IvyModalShapes {
    val r2Top: CornerBasedShape
}

private val White = Color(0xFFFAFAFA)
private val Black = Color(0xFF111114)
private val TrueBlack = Color(0xFF000000)
private val MediumBlack = Color(0xFF2B2C2D)
private val MediumWhite = Color(0xFFEFEEF0)

private fun ivyModalColors(
    theme: Theme,
    isDarkModeEnabled: Boolean,
): IvyModalColors {
    return when (theme) {
        Theme.LIGHT -> object : IvyModalColors {
            override val pure = White
            override val pureInverse = Black
            override val medium = MediumWhite
            override val mediumBlur = medium.copy(alpha = 0.95f)
        }

        Theme.DARK -> object : IvyModalColors {
            override val pure = Black
            override val pureInverse = White
            override val medium = MediumBlack
            override val mediumBlur = medium.copy(alpha = 0.95f)
        }

        Theme.AMOLED_DARK -> object : IvyModalColors {
            override val pure = TrueBlack
            override val pureInverse = White
            override val medium = MediumBlack
            override val mediumBlur = medium.copy(alpha = 0.95f)
        }

        Theme.AUTO -> if (isDarkModeEnabled) {
            ivyModalColors(Theme.DARK, true)
        } else {
            ivyModalColors(Theme.LIGHT, false)
        }
    }
}

private fun ivyModalShapes(): IvyModalShapes {
    return object : IvyModalShapes {
        override val r2Top = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    }
}
