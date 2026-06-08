package com.ivy.settings

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

internal object SettingsTheme {
    val colors: SettingsColors
        @Composable
        @ReadOnlyComposable
        get() = settingsColors(
            theme = LocalThemeState.current.theme,
            isDarkModeEnabled = isSystemInDarkTheme(),
        )

    val typo: SettingsTypography = settingsTypography()
    val shapes: SettingsShapes = settingsShapes()
}

internal interface SettingsColors {
    val pure: Color
    val pureInverse: Color
    val gray: Color
    val medium: Color
    val green: Color
    val red: Color
}

internal interface SettingsTypography {
    val h2: TextStyle
    val b1: TextStyle
    val b2: TextStyle
    val nB2: TextStyle
    val nC: TextStyle
}

internal interface SettingsShapes {
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
private val Red = Color(0xFFFF4060)

private fun settingsColors(
    theme: Theme,
    isDarkModeEnabled: Boolean,
): SettingsColors {
    return when (theme) {
        Theme.LIGHT -> object : SettingsColors {
            override val pure = White
            override val pureInverse = Black
            override val gray = Gray
            override val medium = MediumWhite
            override val green = Green
            override val red = Red
        }

        Theme.DARK -> object : SettingsColors {
            override val pure = Black
            override val pureInverse = White
            override val gray = Gray
            override val medium = MediumBlack
            override val green = Green
            override val red = Red
        }

        Theme.AMOLED_DARK -> object : SettingsColors {
            override val pure = TrueBlack
            override val pureInverse = White
            override val gray = Gray
            override val medium = MediumBlack
            override val green = Green
            override val red = Red
        }

        Theme.AUTO -> if (isDarkModeEnabled) {
            settingsColors(Theme.DARK, true)
        } else {
            settingsColors(Theme.LIGHT, false)
        }
    }
}

private fun settingsTypography(): SettingsTypography {
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

    return object : SettingsTypography {
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
        override val nB2 = TextStyle(
            fontFamily = openSans,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            baselineShift = BaselineShift(0.075f),
        )
        override val nC = TextStyle(
            fontFamily = openSans,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            baselineShift = BaselineShift(0.075f),
        )
    }
}

private fun settingsShapes(): SettingsShapes {
    return object : SettingsShapes {
        override val r4 = RoundedCornerShape(16.dp)
        override val rFull = RoundedCornerShape(percent = 50)
    }
}
