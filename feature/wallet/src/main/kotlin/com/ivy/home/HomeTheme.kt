package com.ivy.home

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivy.data.model.Theme
import com.ivy.ui.R
import com.ivy.ui.theme.LocalThemeState

internal object HomeTheme {
    val colors: HomeColors
        @Composable
        @ReadOnlyComposable
        get() = homeColors(
            theme = LocalThemeState.current.theme,
            isDarkModeEnabled = isSystemInDarkTheme(),
        )

    val typo: HomeTypography = homeTypography()
    val shapes: HomeShapes = homeShapes()
}

internal interface HomeColors {
    val pure: Color
    val pureInverse: Color
    val gray: Color
    val medium: Color
    val green: Color
    val orange: Color
    val red: Color
}

internal interface HomeTypography {
    val b1: TextStyle
    val b2: TextStyle
    val c: TextStyle
    val nB2: TextStyle
}

internal interface HomeShapes {
    val r3: CornerBasedShape
    val r4: CornerBasedShape
    val rFull: CornerBasedShape
}

private const val OPEN_SANS_BASELINE_SHIFT = 0.075f
private const val RALEWAY_BASELINE_SHIFT = 0.2f

private val White = Color(0xFFFAFAFA)
private val Black = Color(0xFF111114)
private val TrueBlack = Color(0xFF000000)
private val Gray = Color(0xFF939199)
private val MediumBlack = Color(0xFF2B2C2D)
private val MediumWhite = Color(0xFFEFEEF0)
private val Green = Color(0xFF14CC9E)
private val Orange = Color(0xFFF29F30)
private val Red = Color(0xFFFF4060)

private fun homeColors(
    theme: Theme,
    isDarkModeEnabled: Boolean,
): HomeColors {
    return when (theme) {
        Theme.LIGHT -> object : HomeColors {
            override val pure = White
            override val pureInverse = Black
            override val gray = Gray
            override val medium = MediumWhite
            override val green = Green
            override val orange = Orange
            override val red = Red
        }

        Theme.DARK -> object : HomeColors {
            override val pure = Black
            override val pureInverse = White
            override val gray = Gray
            override val medium = MediumBlack
            override val green = Green
            override val orange = Orange
            override val red = Red
        }

        Theme.AMOLED_DARK -> object : HomeColors {
            override val pure = TrueBlack
            override val pureInverse = White
            override val gray = Gray
            override val medium = MediumBlack
            override val green = Green
            override val orange = Orange
            override val red = Red
        }

        Theme.AUTO -> if (isDarkModeEnabled) {
            homeColors(Theme.DARK, true)
        } else {
            homeColors(Theme.LIGHT, false)
        }
    }
}

private fun homeTypography(): HomeTypography {
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

    return object : HomeTypography {
        override val b1 = TextStyle(
            fontFamily = raleWay,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            baselineShift = BaselineShift(RALEWAY_BASELINE_SHIFT),
        )
        override val b2 = TextStyle(
            fontFamily = raleWay,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            baselineShift = BaselineShift(RALEWAY_BASELINE_SHIFT),
        )
        override val c = TextStyle(
            fontFamily = raleWay,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 12.sp,
            baselineShift = BaselineShift(RALEWAY_BASELINE_SHIFT),
        )
        override val nB2 = TextStyle(
            fontFamily = openSans,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            baselineShift = BaselineShift(OPEN_SANS_BASELINE_SHIFT),
        )
    }
}

private fun homeShapes(): HomeShapes {
    return object : HomeShapes {
        override val r3 = RoundedCornerShape(20.dp)
        override val r4 = RoundedCornerShape(16.dp)
        override val rFull = RoundedCornerShape(percent = 50)
    }
}
