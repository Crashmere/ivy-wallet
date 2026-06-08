package com.ivy.ui.modal

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.unit.sp
import com.ivy.data.model.Theme
import com.ivy.ui.R
import com.ivy.ui.theme.LocalThemeState

internal object ReorderModalTheme {
    val colors: ReorderModalColors
        @Composable
        @ReadOnlyComposable
        get() = reorderModalColors(
            theme = LocalThemeState.current.theme,
            isDarkModeEnabled = isSystemInDarkTheme(),
        )

    val typo: ReorderModalTypography = reorderModalTypography()
}

internal interface ReorderModalColors {
    val pureInverse: Color
    val medium: Color
    val gray: Color
}

internal interface ReorderModalTypography {
    val b1: TextStyle
}

private val White = Color(0xFFFAFAFA)
private val Black = Color(0xFF111114)
private val Gray = Color(0xFF939199)
private val MediumBlack = Color(0xFF2B2C2D)
private val MediumWhite = Color(0xFFEFEEF0)

private fun reorderModalColors(
    theme: Theme,
    isDarkModeEnabled: Boolean,
): ReorderModalColors {
    return when (theme) {
        Theme.LIGHT -> object : ReorderModalColors {
            override val pureInverse = Black
            override val medium = MediumWhite
            override val gray = Gray
        }

        Theme.DARK,
        Theme.AMOLED_DARK -> object : ReorderModalColors {
            override val pureInverse = White
            override val medium = MediumBlack
            override val gray = Gray
        }

        Theme.AUTO -> if (isDarkModeEnabled) {
            reorderModalColors(Theme.DARK, true)
        } else {
            reorderModalColors(Theme.LIGHT, false)
        }
    }
}

private fun reorderModalTypography(): ReorderModalTypography {
    val raleWay = FontFamily(
        Font(R.font.raleway_regular, FontWeight.Normal),
        Font(R.font.raleway_medium, FontWeight.Medium),
        Font(R.font.raleway_black, FontWeight.Black),
        Font(R.font.raleway_light, FontWeight.Light),
        Font(R.font.raleway_semibold, FontWeight.SemiBold),
        Font(R.font.raleway_bold, FontWeight.Bold),
        Font(R.font.raleway_extrabold, FontWeight.ExtraBold),
    )

    return object : ReorderModalTypography {
        override val b1 = TextStyle(
            fontFamily = raleWay,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            baselineShift = BaselineShift(0.2f),
        )
    }
}
