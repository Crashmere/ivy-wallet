package com.ivy.ui.modal

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

internal object ModalTitleTheme {
    val colors: ModalTitleColors
        @Composable
        @ReadOnlyComposable
        get() = modalTitleColors(
            theme = LocalThemeState.current.theme,
            isDarkModeEnabled = isSystemInDarkTheme(),
        )

    val typo: ModalTitleTypography = modalTitleTypography()
}

internal interface ModalTitleColors {
    val pureInverse: Color
}

internal interface ModalTitleTypography {
    val b1: TextStyle
}

private val White = Color(0xFFFAFAFA)
private val Black = Color(0xFF111114)

private fun modalTitleColors(
    theme: Theme,
    isDarkModeEnabled: Boolean,
): ModalTitleColors {
    return when (theme) {
        Theme.LIGHT -> object : ModalTitleColors {
            override val pureInverse = Black
        }

        Theme.DARK,
        Theme.AMOLED_DARK -> object : ModalTitleColors {
            override val pureInverse = White
        }

        Theme.AUTO -> if (isDarkModeEnabled) {
            modalTitleColors(Theme.DARK, true)
        } else {
            modalTitleColors(Theme.LIGHT, false)
        }
    }
}

private fun modalTitleTypography(): ModalTitleTypography {
    val raleWay = FontFamily(
        Font(R.font.raleway_regular, FontWeight.Normal),
        Font(R.font.raleway_medium, FontWeight.Medium),
        Font(R.font.raleway_black, FontWeight.Black),
        Font(R.font.raleway_light, FontWeight.Light),
        Font(R.font.raleway_semibold, FontWeight.SemiBold),
        Font(R.font.raleway_bold, FontWeight.Bold),
        Font(R.font.raleway_extrabold, FontWeight.ExtraBold),
    )

    return object : ModalTitleTypography {
        override val b1 = TextStyle(
            fontFamily = raleWay,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            baselineShift = BaselineShift(0.2f),
        )
    }
}
