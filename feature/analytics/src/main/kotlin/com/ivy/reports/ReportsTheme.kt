package com.ivy.reports

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

internal object ReportsTheme {
    val colors: ReportsColors
        @Composable
        @ReadOnlyComposable
        get() = reportsColors(
            theme = LocalThemeState.current.theme,
            isDarkModeEnabled = isSystemInDarkTheme(),
        )

    val typo: ReportsTypography = reportsTypography()
    val shapes: ReportsShapes = reportsShapes()
}

internal interface ReportsColors {
    val pure: Color
    val pureInverse: Color
    val gray: Color
    val medium: Color
    val mediumInverse: Color
    val green: Color
    val orange: Color
    val red: Color
}

internal interface ReportsTypography {
    val h2: TextStyle
    val b1: TextStyle
    val b2: TextStyle
    val c: TextStyle
    val nB1: TextStyle
}

internal interface ReportsShapes {
    val r4: CornerBasedShape
    val rFull: CornerBasedShape
}

private val White = Color(0xFFFAFAFA)
private val Black = Color(0xFF111114)
private val TrueBlack = Color(0xFF000000)
private val Gray = Color(0xFF939199)
private val MediumBlack = Color(0xFF2B2C2D)
private val MediumWhite = Color(0xFFEFEEF0)
private val Green = Color(0xFF14CC9E)
private val Orange = Color(0xFFF29F30)
private val Red = Color(0xFFFF4060)

private fun reportsColors(
    theme: Theme,
    isDarkModeEnabled: Boolean,
): ReportsColors {
    return when (theme) {
        Theme.LIGHT -> object : ReportsColors {
            override val pure = White
            override val pureInverse = Black
            override val gray = Gray
            override val medium = MediumWhite
            override val mediumInverse = MediumBlack
            override val green = Green
            override val orange = Orange
            override val red = Red
        }

        Theme.DARK -> object : ReportsColors {
            override val pure = Black
            override val pureInverse = White
            override val gray = Gray
            override val medium = MediumBlack
            override val mediumInverse = MediumWhite
            override val green = Green
            override val orange = Orange
            override val red = Red
        }

        Theme.AMOLED_DARK -> object : ReportsColors {
            override val pure = TrueBlack
            override val pureInverse = White
            override val gray = Gray
            override val medium = MediumBlack
            override val mediumInverse = MediumWhite
            override val green = Green
            override val orange = Orange
            override val red = Red
        }

        Theme.AUTO -> if (isDarkModeEnabled) {
            reportsColors(Theme.DARK, true)
        } else {
            reportsColors(Theme.LIGHT, false)
        }
    }
}

private fun reportsTypography(): ReportsTypography {
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

    return object : ReportsTypography {
        override val h2 = TextStyle(
            fontFamily = raleWay,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 32.sp,
            baselineShift = BaselineShift(0.2f),
        )
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
        override val nB1 = TextStyle(
            fontFamily = openSans,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            baselineShift = BaselineShift(0.075f),
        )
    }
}

private fun reportsShapes(): ReportsShapes {
    return object : ReportsShapes {
        override val r4 = RoundedCornerShape(16.dp)
        override val rFull = RoundedCornerShape(percent = 50)
    }
}
