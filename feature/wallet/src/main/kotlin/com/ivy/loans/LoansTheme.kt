package com.ivy.loans

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

internal object LoansTheme {
    val colors: LoansColors
        @Composable
        @ReadOnlyComposable
        get() = loansColors(
            theme = LocalThemeState.current.theme,
            isDarkModeEnabled = isSystemInDarkTheme(),
        )

    val typo: LoansTypography = loansTypography()
    val shapes: LoansShapes = loansShapes()
}

internal interface LoansColors {
    val pure: Color
    val pureInverse: Color
    val gray: Color
    val medium: Color
    val green: Color
    val green1: Color
    val orange: Color
    val red: Color
}

internal interface LoansTypography {
    val h2: TextStyle
    val b1: TextStyle
    val b2: TextStyle
    val c: TextStyle
    val nB1: TextStyle
    val nB2: TextStyle
    val nC: TextStyle
}

internal interface LoansShapes {
    val r1Top: CornerBasedShape
    val r2: CornerBasedShape
    val r4: CornerBasedShape
    val r4Top: CornerBasedShape
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
private val GreenLight = Color(0xFFAAF2E0)
private val GreenDark = Color(0xFF0A664F)
private val Orange = Color(0xFFF29F30)
private val Red = Color(0xFFFF4060)

private fun loansColors(
    theme: Theme,
    isDarkModeEnabled: Boolean,
): LoansColors {
    return when (theme) {
        Theme.LIGHT -> object : LoansColors {
            override val pure = White
            override val pureInverse = Black
            override val gray = Gray
            override val medium = MediumWhite
            override val green = Green
            override val green1 = GreenLight
            override val orange = Orange
            override val red = Red
        }

        Theme.DARK -> object : LoansColors {
            override val pure = Black
            override val pureInverse = White
            override val gray = Gray
            override val medium = MediumBlack
            override val green = Green
            override val green1 = GreenDark
            override val orange = Orange
            override val red = Red
        }

        Theme.AMOLED_DARK -> object : LoansColors {
            override val pure = TrueBlack
            override val pureInverse = White
            override val gray = Gray
            override val medium = MediumBlack
            override val green = Green
            override val green1 = GreenDark
            override val orange = Orange
            override val red = Red
        }

        Theme.AUTO -> if (isDarkModeEnabled) {
            loansColors(Theme.DARK, true)
        } else {
            loansColors(Theme.LIGHT, false)
        }
    }
}

private fun loansTypography(): LoansTypography {
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

    return object : LoansTypography {
        override val h2 = TextStyle(
            fontFamily = raleWay,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 32.sp,
            baselineShift = BaselineShift(RALEWAY_BASELINE_SHIFT),
        )
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
        override val nB1 = TextStyle(
            fontFamily = openSans,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            baselineShift = BaselineShift(OPEN_SANS_BASELINE_SHIFT),
        )
        override val nB2 = TextStyle(
            fontFamily = openSans,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            baselineShift = BaselineShift(OPEN_SANS_BASELINE_SHIFT),
        )
        override val nC = TextStyle(
            fontFamily = openSans,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            baselineShift = BaselineShift(OPEN_SANS_BASELINE_SHIFT),
        )
    }
}

private fun loansShapes(): LoansShapes {
    return object : LoansShapes {
        override val r1Top = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
        override val r2 = RoundedCornerShape(24.dp)
        override val r4 = RoundedCornerShape(16.dp)
        override val r4Top = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        override val rFull = RoundedCornerShape(percent = 50)
    }
}
