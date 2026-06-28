package com.ivy.balance

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

internal object BalanceTheme {
    val colors: BalanceColors
        @Composable
        @ReadOnlyComposable
        get() = balanceColors(
            theme = LocalThemeState.current.theme,
            isDarkModeEnabled = isSystemInDarkTheme(),
        )

    val typo: BalanceTypography = balanceTypography()
}

internal interface BalanceColors {
    val pureInverse: Color
    val gray: Color
    val medium: Color
    val orange: Color
}

internal interface BalanceTypography {
    val b2: TextStyle
    val nC: TextStyle
}

private val White = Color(0xFFFAFAFA)
private val Black = Color(0xFF111114)
private val Gray = Color(0xFF939199)
private val MediumBlack = Color(0xFF2B2C2D)
private val MediumWhite = Color(0xFFEFEEF0)
private val Orange = Color(0xFFF29F30)

private fun balanceColors(
    theme: Theme,
    isDarkModeEnabled: Boolean,
): BalanceColors {
    return when (theme) {
        Theme.LIGHT -> object : BalanceColors {
            override val pureInverse = Black
            override val gray = Gray
            override val medium = MediumWhite
            override val orange = Orange
        }

        Theme.DARK,
        Theme.AMOLED_DARK -> object : BalanceColors {
            override val pureInverse = White
            override val gray = Gray
            override val medium = MediumBlack
            override val orange = Orange
        }

        Theme.AUTO -> if (isDarkModeEnabled) {
            balanceColors(Theme.DARK, true)
        } else {
            balanceColors(Theme.LIGHT, false)
        }
    }
}

private fun balanceTypography(): BalanceTypography {
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

    return object : BalanceTypography {
        override val b2 = TextStyle(
            fontFamily = raleWay,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            baselineShift = BaselineShift(0.2f),
        )
        override val nC = TextStyle(
            fontFamily = openSans,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            baselineShift = BaselineShift(0.075f),
        )
    }
}
