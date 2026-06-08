package com.ivy.ui.period

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
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

internal object PeriodSelectorTheme {
    val colors: PeriodSelectorColors
        @Composable
        @ReadOnlyComposable
        get() = periodSelectorColors(
            theme = LocalThemeState.current.theme,
            isDarkModeEnabled = isSystemInDarkTheme(),
        )

    val typo: PeriodSelectorTypography = periodSelectorTypography()

    val shapes: PeriodSelectorShapes = periodSelectorShapes()
}

internal interface PeriodSelectorColors {
    val medium: Color
    val pureInverse: Color
}

internal interface PeriodSelectorTypography {
    val b2: TextStyle
}

internal interface PeriodSelectorShapes {
    val rFull: CornerBasedShape
}

private val White = Color(0xFFFAFAFA)
private val Black = Color(0xFF111114)
private val MediumWhite = Color(0xFFEFEEF0)
private val MediumBlack = Color(0xFF2B2C2D)

private fun periodSelectorColors(
    theme: Theme,
    isDarkModeEnabled: Boolean,
): PeriodSelectorColors {
    return when (theme) {
        Theme.LIGHT -> object : PeriodSelectorColors {
            override val medium = MediumWhite
            override val pureInverse = Black
        }

        Theme.DARK,
        Theme.AMOLED_DARK -> object : PeriodSelectorColors {
            override val medium = MediumBlack
            override val pureInverse = White
        }

        Theme.AUTO -> if (isDarkModeEnabled) {
            periodSelectorColors(Theme.DARK, true)
        } else {
            periodSelectorColors(Theme.LIGHT, false)
        }
    }
}

private fun periodSelectorTypography(): PeriodSelectorTypography {
    val raleWay = FontFamily(
        Font(R.font.raleway_regular, FontWeight.Normal),
        Font(R.font.raleway_medium, FontWeight.Medium),
        Font(R.font.raleway_black, FontWeight.Black),
        Font(R.font.raleway_light, FontWeight.Light),
        Font(R.font.raleway_semibold, FontWeight.SemiBold),
        Font(R.font.raleway_bold, FontWeight.Bold),
        Font(R.font.raleway_extrabold, FontWeight.ExtraBold),
    )

    return object : PeriodSelectorTypography {
        override val b2 = TextStyle(
            fontFamily = raleWay,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            baselineShift = BaselineShift(0.2f),
        )
    }
}

private fun periodSelectorShapes(): PeriodSelectorShapes {
    return object : PeriodSelectorShapes {
        override val rFull = RoundedCornerShape(percent = 50)
    }
}
