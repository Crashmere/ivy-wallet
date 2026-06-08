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
import androidx.compose.ui.unit.sp
import com.ivy.data.model.Theme
import com.ivy.ui.R
import com.ivy.ui.theme.LocalThemeState

internal object ModalStatusTheme {
    val colors: ModalStatusColors
        @Composable
        @ReadOnlyComposable
        get() = modalStatusColors(
            theme = LocalThemeState.current.theme,
            isDarkModeEnabled = isSystemInDarkTheme(),
        )

    val typo: ModalStatusTypography = modalStatusTypography()

    val shapes: ModalStatusShapes = modalStatusShapes()
}

internal interface ModalStatusColors {
    val pureInverse: Color
    val gray: Color
    val red: Color
    val orange: Color
    val white: Color
}

internal interface ModalStatusTypography {
    val b1: TextStyle
    val b2: TextStyle
}

internal interface ModalStatusShapes {
    val rFull: CornerBasedShape
}

private val White = Color(0xFFFAFAFA)
private val Black = Color(0xFF111114)
private val Gray = Color(0xFF939199)
private val Red = Color(0xFFFF4060)
private val Orange = Color(0xFFF29F30)

private fun modalStatusColors(
    theme: Theme,
    isDarkModeEnabled: Boolean,
): ModalStatusColors {
    return when (theme) {
        Theme.LIGHT -> object : ModalStatusColors {
            override val pureInverse = Black
            override val gray = Gray
            override val red = Red
            override val orange = Orange
            override val white = White
        }

        Theme.DARK,
        Theme.AMOLED_DARK -> object : ModalStatusColors {
            override val pureInverse = White
            override val gray = Gray
            override val red = Red
            override val orange = Orange
            override val white = White
        }

        Theme.AUTO -> if (isDarkModeEnabled) {
            modalStatusColors(Theme.DARK, true)
        } else {
            modalStatusColors(Theme.LIGHT, false)
        }
    }
}

private fun modalStatusTypography(): ModalStatusTypography {
    val raleWay = FontFamily(
        Font(R.font.raleway_regular, FontWeight.Normal),
        Font(R.font.raleway_medium, FontWeight.Medium),
        Font(R.font.raleway_black, FontWeight.Black),
        Font(R.font.raleway_light, FontWeight.Light),
        Font(R.font.raleway_semibold, FontWeight.SemiBold),
        Font(R.font.raleway_bold, FontWeight.Bold),
        Font(R.font.raleway_extrabold, FontWeight.ExtraBold),
    )

    return object : ModalStatusTypography {
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

private fun modalStatusShapes(): ModalStatusShapes {
    return object : ModalStatusShapes {
        override val rFull = RoundedCornerShape(percent = 50)
    }
}
