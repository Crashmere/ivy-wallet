package com.ivy.ui.money

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

internal object MoneyDisplayTheme {
    val colors: MoneyDisplayColors
        @Composable
        @ReadOnlyComposable
        get() = moneyDisplayColors(
            theme = LocalThemeState.current.theme,
            isDarkModeEnabled = isSystemInDarkTheme(),
        )

    val typo: MoneyDisplayTypography = moneyDisplayTypography()
}

internal interface MoneyDisplayColors {
    val pureInverse: Color
}

internal interface MoneyDisplayTypography {
    val h1: TextStyle
    val nH1: TextStyle
    val nB1: TextStyle
}

private val White = Color(0xFFFAFAFA)
private val Black = Color(0xFF111114)

private fun moneyDisplayColors(
    theme: Theme,
    isDarkModeEnabled: Boolean,
): MoneyDisplayColors {
    return when (theme) {
        Theme.LIGHT -> object : MoneyDisplayColors {
            override val pureInverse = Black
        }

        Theme.DARK,
        Theme.AMOLED_DARK -> object : MoneyDisplayColors {
            override val pureInverse = White
        }

        Theme.AUTO -> if (isDarkModeEnabled) {
            moneyDisplayColors(Theme.DARK, true)
        } else {
            moneyDisplayColors(Theme.LIGHT, false)
        }
    }
}

private fun moneyDisplayTypography(): MoneyDisplayTypography {
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

    return object : MoneyDisplayTypography {
        override val h1 = TextStyle(
            fontFamily = raleWay,
            fontWeight = FontWeight.Black,
            fontSize = 40.sp,
            baselineShift = BaselineShift(0.2f),
        )
        override val nH1 = TextStyle(
            fontFamily = openSans,
            fontWeight = FontWeight.Bold,
            fontSize = 40.sp,
            baselineShift = BaselineShift(0.075f),
        )
        override val nB1 = TextStyle(
            fontFamily = openSans,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            baselineShift = BaselineShift(0.075f),
        )
    }
}
