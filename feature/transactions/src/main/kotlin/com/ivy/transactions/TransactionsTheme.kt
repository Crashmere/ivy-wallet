package com.ivy.transactions

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

internal object TransactionsTheme {
    val colors: TransactionsColors
        @Composable
        @ReadOnlyComposable
        get() = transactionsColors(
            theme = LocalThemeState.current.theme,
            isDarkModeEnabled = isSystemInDarkTheme(),
        )

    val typo: TransactionsTypography = transactionsTypography()
    val shapes: TransactionsShapes = transactionsShapes()
}

internal interface TransactionsColors {
    val pure: Color
    val pureInverse: Color
    val gray: Color
    val medium: Color
    val red: Color
}

internal interface TransactionsTypography {
    val b1: TextStyle
    val b2: TextStyle
    val c: TextStyle
}

internal interface TransactionsShapes {
    val r1Top: CornerBasedShape
    val rFull: CornerBasedShape
}

private val White = Color(0xFFFAFAFA)
private val Black = Color(0xFF111114)
private val TrueBlack = Color(0xFF000000)
private val Gray = Color(0xFF939199)
private val MediumBlack = Color(0xFF2B2C2D)
private val MediumWhite = Color(0xFFEFEEF0)
private val Red = Color(0xFFFF4060)

private fun transactionsColors(
    theme: Theme,
    isDarkModeEnabled: Boolean,
): TransactionsColors {
    return when (theme) {
        Theme.LIGHT -> object : TransactionsColors {
            override val pure = White
            override val pureInverse = Black
            override val gray = Gray
            override val medium = MediumWhite
            override val red = Red
        }

        Theme.DARK -> object : TransactionsColors {
            override val pure = Black
            override val pureInverse = White
            override val gray = Gray
            override val medium = MediumBlack
            override val red = Red
        }

        Theme.AMOLED_DARK -> object : TransactionsColors {
            override val pure = TrueBlack
            override val pureInverse = White
            override val gray = Gray
            override val medium = MediumBlack
            override val red = Red
        }

        Theme.AUTO -> if (isDarkModeEnabled) {
            transactionsColors(Theme.DARK, true)
        } else {
            transactionsColors(Theme.LIGHT, false)
        }
    }
}

private fun transactionsTypography(): TransactionsTypography {
    val raleWay = FontFamily(
        Font(R.font.raleway_regular, FontWeight.Normal),
        Font(R.font.raleway_medium, FontWeight.Medium),
        Font(R.font.raleway_black, FontWeight.Black),
        Font(R.font.raleway_light, FontWeight.Light),
        Font(R.font.raleway_semibold, FontWeight.SemiBold),
        Font(R.font.raleway_bold, FontWeight.Bold),
        Font(R.font.raleway_extrabold, FontWeight.ExtraBold),
    )

    return object : TransactionsTypography {
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

private fun transactionsShapes(): TransactionsShapes {
    return object : TransactionsShapes {
        override val r1Top = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
        override val rFull = RoundedCornerShape(percent = 50)
    }
}
