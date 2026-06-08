package com.ivy.exchangerates

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

internal object ExchangeRatesTheme {
    val colors: ExchangeRatesColors
        @Composable
        @ReadOnlyComposable
        get() = exchangeRatesColors(
            theme = LocalThemeState.current.theme,
            isDarkModeEnabled = isSystemInDarkTheme(),
        )

    val typo: ExchangeRatesTypography = exchangeRatesTypography()
    val shapes: ExchangeRatesShapes = exchangeRatesShapes()
}

internal interface ExchangeRatesColors {
    val pure: Color
    val pureInverse: Color
    val gray: Color
    val medium: Color
    val primary: Color
    val orange: Color
    val red: Color
}

internal interface ExchangeRatesTypography {
    val h2: TextStyle
    val b1: TextStyle
    val b2: TextStyle
    val c: TextStyle
    val nH2: TextStyle
    val nB1: TextStyle
    val nB2: TextStyle
}

internal interface ExchangeRatesShapes {
    val r4: CornerBasedShape
    val rFull: CornerBasedShape
}

private val White = Color(0xFFFAFAFA)
private val Black = Color(0xFF111114)
private val TrueBlack = Color(0xFF000000)
private val Gray = Color(0xFF939199)
private val MediumBlack = Color(0xFF2B2C2D)
private val MediumWhite = Color(0xFFEFEEF0)
private val Purple = Color(0xFF6B4DFF)
private val Orange = Color(0xFFF29F30)
private val Red = Color(0xFFFF4060)

private fun exchangeRatesColors(
    theme: Theme,
    isDarkModeEnabled: Boolean,
): ExchangeRatesColors {
    return when (theme) {
        Theme.LIGHT -> object : ExchangeRatesColors {
            override val pure = White
            override val pureInverse = Black
            override val gray = Gray
            override val medium = MediumWhite
            override val primary = Purple
            override val orange = Orange
            override val red = Red
        }

        Theme.DARK -> object : ExchangeRatesColors {
            override val pure = Black
            override val pureInverse = White
            override val gray = Gray
            override val medium = MediumBlack
            override val primary = Purple
            override val orange = Orange
            override val red = Red
        }

        Theme.AMOLED_DARK -> object : ExchangeRatesColors {
            override val pure = TrueBlack
            override val pureInverse = White
            override val gray = Gray
            override val medium = MediumBlack
            override val primary = Purple
            override val orange = Orange
            override val red = Red
        }

        Theme.AUTO -> if (isDarkModeEnabled) {
            exchangeRatesColors(Theme.DARK, true)
        } else {
            exchangeRatesColors(Theme.LIGHT, false)
        }
    }
}

private fun exchangeRatesTypography(): ExchangeRatesTypography {
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

    return object : ExchangeRatesTypography {
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
        override val nH2 = TextStyle(
            fontFamily = openSans,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            baselineShift = BaselineShift(0.075f),
        )
        override val nB1 = TextStyle(
            fontFamily = openSans,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            baselineShift = BaselineShift(0.075f),
        )
        override val nB2 = TextStyle(
            fontFamily = openSans,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            baselineShift = BaselineShift(0.075f),
        )
    }
}

private fun exchangeRatesShapes(): ExchangeRatesShapes {
    return object : ExchangeRatesShapes {
        override val r4 = RoundedCornerShape(16.dp)
        override val rFull = RoundedCornerShape(percent = 50)
    }
}
