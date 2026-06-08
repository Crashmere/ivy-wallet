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

internal object ModalAmountSectionTheme {
    val colors: ModalAmountSectionColors
        @Composable
        @ReadOnlyComposable
        get() = modalAmountSectionColors(
            theme = LocalThemeState.current.theme,
            isDarkModeEnabled = isSystemInDarkTheme(),
        )

    val typo: ModalAmountSectionTypography = modalAmountSectionTypography()
}

internal interface ModalAmountSectionColors {
    val gray: Color
    val medium: Color
}

internal interface ModalAmountSectionTypography {
    val c: TextStyle
}

private val Gray = Color(0xFF939199)
private val MediumWhite = Color(0xFFEFEEF0)
private val MediumBlack = Color(0xFF2B2C2D)

private fun modalAmountSectionColors(
    theme: Theme,
    isDarkModeEnabled: Boolean,
): ModalAmountSectionColors {
    return when (theme) {
        Theme.LIGHT -> object : ModalAmountSectionColors {
            override val gray = Gray
            override val medium = MediumWhite
        }

        Theme.DARK,
        Theme.AMOLED_DARK -> object : ModalAmountSectionColors {
            override val gray = Gray
            override val medium = MediumBlack
        }

        Theme.AUTO -> if (isDarkModeEnabled) {
            modalAmountSectionColors(Theme.DARK, true)
        } else {
            modalAmountSectionColors(Theme.LIGHT, false)
        }
    }
}

private fun modalAmountSectionTypography(): ModalAmountSectionTypography {
    val raleWay = FontFamily(
        Font(R.font.raleway_regular, FontWeight.Normal),
        Font(R.font.raleway_medium, FontWeight.Medium),
        Font(R.font.raleway_black, FontWeight.Black),
        Font(R.font.raleway_light, FontWeight.Light),
        Font(R.font.raleway_semibold, FontWeight.SemiBold),
        Font(R.font.raleway_bold, FontWeight.Bold),
        Font(R.font.raleway_extrabold, FontWeight.ExtraBold),
    )

    return object : ModalAmountSectionTypography {
        override val c = TextStyle(
            fontFamily = raleWay,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 12.sp,
            baselineShift = BaselineShift(0.2f),
        )
    }
}
