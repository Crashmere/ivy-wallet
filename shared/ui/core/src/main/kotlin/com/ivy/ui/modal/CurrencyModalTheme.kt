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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivy.data.model.Theme
import com.ivy.ui.R
import com.ivy.ui.theme.LocalThemeState
import com.ivy.ui.theme.colors.IvyGradients

internal object CurrencyModalTheme {
    val colors: CurrencyModalColors
        @Composable
        @ReadOnlyComposable
        get() = currencyModalColors(
            theme = LocalThemeState.current.theme,
            isDarkModeEnabled = isSystemInDarkTheme(),
        )

    val typo: CurrencyModalTypography = currencyModalTypography()

    val shapes: CurrencyModalShapes = currencyModalShapes()
}

internal interface CurrencyModalColors {
    val pureInverse: Color
    val gray: Color
    val medium: Color
    val mediumInverse: Color
    val ivy: Color
    val white: Color
}

internal interface CurrencyModalTypography {
    val b1: TextStyle
    val b2: TextStyle
    val c: TextStyle
}

internal interface CurrencyModalShapes {
    val rFull: CornerBasedShape
    val r3: CornerBasedShape
    val r4: CornerBasedShape
}

internal val CurrencyGradientGreen = IvyGradients.Green
internal val CurrencyGradientIvy = IvyGradients.Ivy

private val White = Color(0xFFFAFAFA)
private val Black = Color(0xFF111114)
private val Gray = Color(0xFF939199)
private val MediumBlack = Color(0xFF2B2C2D)
private val MediumWhite = Color(0xFFEFEEF0)
private val Ivy = Color(0xFF6B4DFF)

private fun currencyModalColors(
    theme: Theme,
    isDarkModeEnabled: Boolean,
): CurrencyModalColors {
    return when (theme) {
        Theme.LIGHT -> object : CurrencyModalColors {
            override val pureInverse = Black
            override val gray = Gray
            override val medium = MediumWhite
            override val mediumInverse = MediumBlack
            override val ivy = Ivy
            override val white = White
        }

        Theme.DARK,
        Theme.AMOLED_DARK -> object : CurrencyModalColors {
            override val pureInverse = White
            override val gray = Gray
            override val medium = MediumBlack
            override val mediumInverse = MediumWhite
            override val ivy = Ivy
            override val white = White
        }

        Theme.AUTO -> if (isDarkModeEnabled) {
            currencyModalColors(Theme.DARK, true)
        } else {
            currencyModalColors(Theme.LIGHT, false)
        }
    }
}

private fun currencyModalTypography(): CurrencyModalTypography {
    val raleWay = FontFamily(
        Font(R.font.raleway_regular, FontWeight.Normal),
        Font(R.font.raleway_medium, FontWeight.Medium),
        Font(R.font.raleway_black, FontWeight.Black),
        Font(R.font.raleway_light, FontWeight.Light),
        Font(R.font.raleway_semibold, FontWeight.SemiBold),
        Font(R.font.raleway_bold, FontWeight.Bold),
        Font(R.font.raleway_extrabold, FontWeight.ExtraBold),
    )

    return object : CurrencyModalTypography {
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
    }
}

private fun currencyModalShapes(): CurrencyModalShapes {
    return object : CurrencyModalShapes {
        override val rFull = RoundedCornerShape(percent = 50)
        override val r3 = RoundedCornerShape(20.dp)
        override val r4 = RoundedCornerShape(16.dp)
    }
}
