package com.ivy.ui.modal

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
import com.ivy.ui.theme.colors.IvyGradients

internal object ChoosePeriodModalTheme {
    val colors: ChoosePeriodModalColors
        @Composable
        @ReadOnlyComposable
        get() = choosePeriodModalColors(
            theme = LocalThemeState.current.theme,
            isDarkModeEnabled = isSystemInDarkTheme(),
        )

    val typo: ChoosePeriodModalTypography = choosePeriodModalTypography()

    val shapes: ChoosePeriodModalShapes = choosePeriodModalShapes()
}

internal interface ChoosePeriodModalColors {
    val pureInverse: Color
    val gray: Color
    val medium: Color
    val green: Color
    val white: Color
}

internal interface ChoosePeriodModalTypography {
    val b1: TextStyle
    val b2: TextStyle
    val c: TextStyle
    val nB2: TextStyle
}

internal interface ChoosePeriodModalShapes {
    val rFull: CornerBasedShape
}

internal val PeriodGradientIvy = IvyGradients.Ivy

private val White = Color(0xFFFAFAFA)
private val Black = Color(0xFF111114)
private val Gray = Color(0xFF939199)
private val MediumBlack = Color(0xFF2B2C2D)
private val MediumWhite = Color(0xFFEFEEF0)
private val Green = Color(0xFF14CC9E)

private fun choosePeriodModalColors(
    theme: Theme,
    isDarkModeEnabled: Boolean,
): ChoosePeriodModalColors {
    return when (theme) {
        Theme.LIGHT -> object : ChoosePeriodModalColors {
            override val pureInverse = Black
            override val gray = Gray
            override val medium = MediumWhite
            override val green = Green
            override val white = White
        }

        Theme.DARK,
        Theme.AMOLED_DARK -> object : ChoosePeriodModalColors {
            override val pureInverse = White
            override val gray = Gray
            override val medium = MediumBlack
            override val green = Green
            override val white = White
        }

        Theme.AUTO -> if (isDarkModeEnabled) {
            choosePeriodModalColors(Theme.DARK, true)
        } else {
            choosePeriodModalColors(Theme.LIGHT, false)
        }
    }
}

private fun choosePeriodModalTypography(): ChoosePeriodModalTypography {
    val openSans = FontFamily(
        Font(R.font.opensans_regular, FontWeight.Normal),
        Font(R.font.opensans_regular, FontWeight.Medium),
        Font(R.font.opensans_bold, FontWeight.Black),
        Font(R.font.opensans_semibold, FontWeight.SemiBold),
        Font(R.font.opensans_bold, FontWeight.Bold),
        Font(R.font.opensans_extrabold, FontWeight.ExtraBold),
    )
    val raleWay = FontFamily(
        Font(R.font.raleway_regular, FontWeight.Normal),
        Font(R.font.raleway_medium, FontWeight.Medium),
        Font(R.font.raleway_black, FontWeight.Black),
        Font(R.font.raleway_light, FontWeight.Light),
        Font(R.font.raleway_semibold, FontWeight.SemiBold),
        Font(R.font.raleway_bold, FontWeight.Bold),
        Font(R.font.raleway_extrabold, FontWeight.ExtraBold),
    )

    return object : ChoosePeriodModalTypography {
        override val b1 = TextStyle(
            fontFamily = raleWay,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            baselineShift = BaselineShift(0.2f),
        )
        override val b2 = TextStyle(
            fontFamily = raleWay,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            baselineShift = BaselineShift(0.2f),
        )
        override val c = TextStyle(
            fontFamily = raleWay,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 12.sp,
            baselineShift = BaselineShift(0.2f),
        )
        override val nB2 = TextStyle(
            fontFamily = openSans,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            baselineShift = BaselineShift(0.075f),
        )
    }
}

private fun choosePeriodModalShapes(): ChoosePeriodModalShapes {
    return object : ChoosePeriodModalShapes {
        override val rFull = RoundedCornerShape(percent = 50)
    }
}
