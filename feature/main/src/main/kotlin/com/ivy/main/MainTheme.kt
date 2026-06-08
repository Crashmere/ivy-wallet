package com.ivy.main

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.CircleShape
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

internal object MainTheme {
    val colors: MainColors
        @Composable
        @ReadOnlyComposable
        get() = mainColors(
            theme = LocalThemeState.current.theme,
            isDarkModeEnabled = isSystemInDarkTheme(),
        )

    val typo: MainTypography = mainTypography()

    val shapes: MainShapes = mainShapes()
}

internal interface MainColors {
    val pure: Color
    val pureInverse: Color
    val gray: Color
    val medium: Color
    val green: Color
}

internal interface MainTypography {
    val b2: TextStyle
    val c: TextStyle
}

internal interface MainShapes {
    val rFull: CornerBasedShape
        get() = RoundedCornerShape(percent = 50)

    val circle: CornerBasedShape
        get() = CircleShape
}

private val White = Color(0xFFFAFAFA)
private val Black = Color(0xFF111114)
private val TrueBlack = Color(0xFF000000)
private val Green = Color(0xFF14CC9E)
private val Gray = Color(0xFF939199)
private val MediumBlack = Color(0xFF2B2C2D)
private val MediumWhite = Color(0xFFEFEEF0)

private fun mainColors(
    theme: Theme,
    isDarkModeEnabled: Boolean,
): MainColors {
    return when (theme) {
        Theme.LIGHT -> object : MainColors {
            override val pure = White
            override val pureInverse = Black
            override val gray = Gray
            override val medium = MediumWhite
            override val green = Green
        }

        Theme.DARK -> object : MainColors {
            override val pure = Black
            override val pureInverse = White
            override val gray = Gray
            override val medium = MediumBlack
            override val green = Green
        }

        Theme.AMOLED_DARK -> object : MainColors {
            override val pure = TrueBlack
            override val pureInverse = White
            override val gray = Gray
            override val medium = MediumBlack
            override val green = Green
        }

        Theme.AUTO -> if (isDarkModeEnabled) {
            mainColors(Theme.DARK, true)
        } else {
            mainColors(Theme.LIGHT, false)
        }
    }
}

private fun mainTypography(): MainTypography {
    val raleWay = FontFamily(
        Font(R.font.raleway_regular, FontWeight.Normal),
        Font(R.font.raleway_medium, FontWeight.Medium),
        Font(R.font.raleway_black, FontWeight.Black),
        Font(R.font.raleway_light, FontWeight.Light),
        Font(R.font.raleway_semibold, FontWeight.SemiBold),
        Font(R.font.raleway_bold, FontWeight.Bold),
        Font(R.font.raleway_extrabold, FontWeight.ExtraBold),
    )

    return object : MainTypography {
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

private fun mainShapes(): MainShapes {
    return object : MainShapes {}
}
