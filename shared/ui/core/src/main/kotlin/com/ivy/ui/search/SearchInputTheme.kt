package com.ivy.ui.search

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
import androidx.compose.ui.unit.sp
import com.ivy.data.model.Theme
import com.ivy.ui.R
import com.ivy.ui.theme.LocalThemeState

internal object SearchInputTheme {
    val colors: SearchInputColors
        @Composable
        @ReadOnlyComposable
        get() = searchInputColors(
            theme = LocalThemeState.current.theme,
            isDarkModeEnabled = isSystemInDarkTheme(),
        )

    val typo: SearchInputTypography = searchInputTypography()

    val shapes: SearchInputShapes = searchInputShapes()
}

internal interface SearchInputColors {
    val pure: Color
    val pureInverse: Color
    val gray: Color
}

internal interface SearchInputTypography {
    val b2: TextStyle
}

internal interface SearchInputShapes {
    val rFull: CornerBasedShape
}

private val White = Color(0xFFFAFAFA)
private val Black = Color(0xFF111114)
private val TrueBlack = Color(0xFF000000)
private val Gray = Color(0xFF939199)

private fun searchInputColors(
    theme: Theme,
    isDarkModeEnabled: Boolean,
): SearchInputColors {
    return when (theme) {
        Theme.LIGHT -> object : SearchInputColors {
            override val pure = White
            override val pureInverse = Black
            override val gray = Gray
        }

        Theme.DARK -> object : SearchInputColors {
            override val pure = Black
            override val pureInverse = White
            override val gray = Gray
        }

        Theme.AMOLED_DARK -> object : SearchInputColors {
            override val pure = TrueBlack
            override val pureInverse = White
            override val gray = Gray
        }

        Theme.AUTO -> if (isDarkModeEnabled) {
            searchInputColors(Theme.DARK, true)
        } else {
            searchInputColors(Theme.LIGHT, false)
        }
    }
}

private fun searchInputTypography(): SearchInputTypography {
    val raleWay = FontFamily(
        Font(R.font.raleway_regular, FontWeight.Normal),
        Font(R.font.raleway_medium, FontWeight.Medium),
        Font(R.font.raleway_black, FontWeight.Black),
        Font(R.font.raleway_light, FontWeight.Light),
        Font(R.font.raleway_semibold, FontWeight.SemiBold),
        Font(R.font.raleway_bold, FontWeight.Bold),
        Font(R.font.raleway_extrabold, FontWeight.ExtraBold),
    )

    return object : SearchInputTypography {
        override val b2 = TextStyle(
            fontFamily = raleWay,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            baselineShift = BaselineShift(0.2f),
        )
    }
}

private fun searchInputShapes(): SearchInputShapes {
    return object : SearchInputShapes {
        override val rFull = RoundedCornerShape(percent = 50)
    }
}
