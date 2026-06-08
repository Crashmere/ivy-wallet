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

internal object ChooseIconModalTheme {
    val colors: ChooseIconModalColors
        @Composable
        @ReadOnlyComposable
        get() = chooseIconModalColors(
            theme = LocalThemeState.current.theme,
            isDarkModeEnabled = isSystemInDarkTheme(),
        )

    val typo: ChooseIconModalTypography = chooseIconModalTypography()

    val shapes: ChooseIconModalShapes = chooseIconModalShapes()
}

internal interface ChooseIconModalColors {
    val pureInverse: Color
    val gray: Color
    val medium: Color
    val mediumInverse: Color
}

internal interface ChooseIconModalTypography {
    val b1: TextStyle
}

internal interface ChooseIconModalShapes {
    val rFull: CornerBasedShape
}

private val White = Color(0xFFFAFAFA)
private val Black = Color(0xFF111114)
private val Gray = Color(0xFF939199)
private val MediumBlack = Color(0xFF2B2C2D)
private val MediumWhite = Color(0xFFEFEEF0)

private fun chooseIconModalColors(
    theme: Theme,
    isDarkModeEnabled: Boolean,
): ChooseIconModalColors {
    return when (theme) {
        Theme.LIGHT -> object : ChooseIconModalColors {
            override val pureInverse = Black
            override val gray = Gray
            override val medium = MediumWhite
            override val mediumInverse = MediumBlack
        }

        Theme.DARK,
        Theme.AMOLED_DARK -> object : ChooseIconModalColors {
            override val pureInverse = White
            override val gray = Gray
            override val medium = MediumBlack
            override val mediumInverse = MediumWhite
        }

        Theme.AUTO -> if (isDarkModeEnabled) {
            chooseIconModalColors(Theme.DARK, true)
        } else {
            chooseIconModalColors(Theme.LIGHT, false)
        }
    }
}

private fun chooseIconModalTypography(): ChooseIconModalTypography {
    val raleWay = FontFamily(
        Font(R.font.raleway_regular, FontWeight.Normal),
        Font(R.font.raleway_medium, FontWeight.Medium),
        Font(R.font.raleway_black, FontWeight.Black),
        Font(R.font.raleway_light, FontWeight.Light),
        Font(R.font.raleway_semibold, FontWeight.SemiBold),
        Font(R.font.raleway_bold, FontWeight.Bold),
        Font(R.font.raleway_extrabold, FontWeight.ExtraBold),
    )

    return object : ChooseIconModalTypography {
        override val b1 = TextStyle(
            fontFamily = raleWay,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            baselineShift = BaselineShift(0.2f),
        )
    }
}

private fun chooseIconModalShapes(): ChooseIconModalShapes {
    return object : ChooseIconModalShapes {
        override val rFull = RoundedCornerShape(percent = 50)
    }
}
