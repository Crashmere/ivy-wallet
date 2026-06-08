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

internal object CategoryModalTheme {
    val colors: CategoryModalColors
        @Composable
        @ReadOnlyComposable
        get() = categoryModalColors(
            theme = LocalThemeState.current.theme,
            isDarkModeEnabled = isSystemInDarkTheme(),
        )

    val typo: CategoryModalTypography = categoryModalTypography()
    val shapes: CategoryModalShapes = categoryModalShapes()
}

internal interface CategoryModalColors {
    val pure: Color
    val pureInverse: Color
    val gray: Color
    val medium: Color
    val mediumInverse: Color
}

internal interface CategoryModalTypography {
    val b1: TextStyle
    val b2: TextStyle
}

internal interface CategoryModalShapes {
    val rFull: CornerBasedShape
}

private val White = Color(0xFFFAFAFA)
private val Black = Color(0xFF111114)
private val TrueBlack = Color(0xFF000000)
private val Gray = Color(0xFF939199)
private val MediumBlack = Color(0xFF2B2C2D)
private val MediumWhite = Color(0xFFEFEEF0)

private fun categoryModalColors(
    theme: Theme,
    isDarkModeEnabled: Boolean,
): CategoryModalColors {
    return when (theme) {
        Theme.LIGHT -> object : CategoryModalColors {
            override val pure = White
            override val pureInverse = Black
            override val gray = Gray
            override val medium = MediumWhite
            override val mediumInverse = MediumBlack
        }

        Theme.DARK -> object : CategoryModalColors {
            override val pure = Black
            override val pureInverse = White
            override val gray = Gray
            override val medium = MediumBlack
            override val mediumInverse = MediumWhite
        }

        Theme.AMOLED_DARK -> object : CategoryModalColors {
            override val pure = TrueBlack
            override val pureInverse = White
            override val gray = Gray
            override val medium = MediumBlack
            override val mediumInverse = MediumWhite
        }

        Theme.AUTO -> if (isDarkModeEnabled) {
            categoryModalColors(Theme.DARK, true)
        } else {
            categoryModalColors(Theme.LIGHT, false)
        }
    }
}

private fun categoryModalTypography(): CategoryModalTypography {
    val raleWay = FontFamily(
        Font(R.font.raleway_regular, FontWeight.Normal),
        Font(R.font.raleway_medium, FontWeight.Medium),
        Font(R.font.raleway_black, FontWeight.Black),
        Font(R.font.raleway_light, FontWeight.Light),
        Font(R.font.raleway_semibold, FontWeight.SemiBold),
        Font(R.font.raleway_bold, FontWeight.Bold),
        Font(R.font.raleway_extrabold, FontWeight.ExtraBold),
    )

    return object : CategoryModalTypography {
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

private fun categoryModalShapes(): CategoryModalShapes {
    return object : CategoryModalShapes {
        override val rFull = RoundedCornerShape(percent = 50)
    }
}
