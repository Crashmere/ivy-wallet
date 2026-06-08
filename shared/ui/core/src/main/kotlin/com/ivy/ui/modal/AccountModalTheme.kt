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

internal object AccountModalTheme {
    val colors: AccountModalColors
        @Composable
        @ReadOnlyComposable
        get() = accountModalColors(
            theme = LocalThemeState.current.theme,
            isDarkModeEnabled = isSystemInDarkTheme(),
        )

    val typo: AccountModalTypography = accountModalTypography()
    val shapes: AccountModalShapes = accountModalShapes()
}

internal interface AccountModalColors {
    val pureInverse: Color
    val gray: Color
    val medium: Color
}

internal interface AccountModalTypography {
    val b1: TextStyle
    val b2: TextStyle
}

internal interface AccountModalShapes {
    val r4: CornerBasedShape
    val rFull: CornerBasedShape
}

private val White = Color(0xFFFAFAFA)
private val Black = Color(0xFF111114)
private val Gray = Color(0xFF939199)
private val MediumBlack = Color(0xFF2B2C2D)
private val MediumWhite = Color(0xFFEFEEF0)

private fun accountModalColors(
    theme: Theme,
    isDarkModeEnabled: Boolean,
): AccountModalColors {
    return when (theme) {
        Theme.LIGHT -> object : AccountModalColors {
            override val pureInverse = Black
            override val gray = Gray
            override val medium = MediumWhite
        }

        Theme.DARK,
        Theme.AMOLED_DARK -> object : AccountModalColors {
            override val pureInverse = White
            override val gray = Gray
            override val medium = MediumBlack
        }

        Theme.AUTO -> if (isDarkModeEnabled) {
            accountModalColors(Theme.DARK, true)
        } else {
            accountModalColors(Theme.LIGHT, false)
        }
    }
}

private fun accountModalTypography(): AccountModalTypography {
    val raleWay = FontFamily(
        Font(R.font.raleway_regular, FontWeight.Normal),
        Font(R.font.raleway_medium, FontWeight.Medium),
        Font(R.font.raleway_black, FontWeight.Black),
        Font(R.font.raleway_light, FontWeight.Light),
        Font(R.font.raleway_semibold, FontWeight.SemiBold),
        Font(R.font.raleway_bold, FontWeight.Bold),
        Font(R.font.raleway_extrabold, FontWeight.ExtraBold),
    )

    return object : AccountModalTypography {
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
    }
}

private fun accountModalShapes(): AccountModalShapes {
    return object : AccountModalShapes {
        override val r4 = RoundedCornerShape(16.dp)
        override val rFull = RoundedCornerShape(percent = 50)
    }
}
