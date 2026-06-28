package com.ivy.accounts

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

internal object AccountsTheme {
    val colors: AccountsColors
        @Composable
        @ReadOnlyComposable
        get() = accountsColors(
            theme = LocalThemeState.current.theme,
            isDarkModeEnabled = isSystemInDarkTheme(),
        )

    val typo: AccountsTypography = accountsTypography()
    val shapes: AccountsShapes = accountsShapes()
}

internal interface AccountsColors {
    val pure: Color
    val pureInverse: Color
    val gray: Color
    val medium: Color
}

internal interface AccountsTypography {
    val b1: TextStyle
    val b2: TextStyle
    val c: TextStyle
}

internal interface AccountsShapes {
    val r4: CornerBasedShape
    val r4Top: CornerBasedShape
    val rFull: CornerBasedShape
}

private val White = Color(0xFFFAFAFA)
private val Black = Color(0xFF111114)
private val TrueBlack = Color(0xFF000000)
private val Gray = Color(0xFF939199)
private val MediumBlack = Color(0xFF2B2C2D)
private val MediumWhite = Color(0xFFEFEEF0)

private fun accountsColors(
    theme: Theme,
    isDarkModeEnabled: Boolean,
): AccountsColors {
    return when (theme) {
        Theme.LIGHT -> object : AccountsColors {
            override val pure = White
            override val pureInverse = Black
            override val gray = Gray
            override val medium = MediumWhite
        }

        Theme.DARK -> object : AccountsColors {
            override val pure = Black
            override val pureInverse = White
            override val gray = Gray
            override val medium = MediumBlack
        }

        Theme.AMOLED_DARK -> object : AccountsColors {
            override val pure = TrueBlack
            override val pureInverse = White
            override val gray = Gray
            override val medium = MediumBlack
        }

        Theme.AUTO -> if (isDarkModeEnabled) {
            accountsColors(Theme.DARK, true)
        } else {
            accountsColors(Theme.LIGHT, false)
        }
    }
}

private fun accountsTypography(): AccountsTypography {
    val raleWay = FontFamily(
        Font(R.font.raleway_regular, FontWeight.Normal),
        Font(R.font.raleway_medium, FontWeight.Medium),
        Font(R.font.raleway_black, FontWeight.Black),
        Font(R.font.raleway_light, FontWeight.Light),
        Font(R.font.raleway_semibold, FontWeight.SemiBold),
        Font(R.font.raleway_bold, FontWeight.Bold),
        Font(R.font.raleway_extrabold, FontWeight.ExtraBold),
    )

    return object : AccountsTypography {
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

private fun accountsShapes(): AccountsShapes {
    return object : AccountsShapes {
        override val r4 = RoundedCornerShape(16.dp)
        override val r4Top = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        override val rFull = RoundedCornerShape(percent = 50)
    }
}
