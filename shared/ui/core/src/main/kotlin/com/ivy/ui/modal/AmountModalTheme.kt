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

internal object AmountModalTheme {
    val colors: AmountModalColors
        @Composable
        @ReadOnlyComposable
        get() = amountModalColors(
            theme = LocalThemeState.current.theme,
            isDarkModeEnabled = isSystemInDarkTheme(),
        )

    val typo: AmountModalTypography = amountModalTypography()
    val shapes: AmountModalShapes = amountModalShapes()
}

internal interface AmountModalColors {
    val pure: Color
    val pureInverse: Color
    val medium: Color
}

internal interface AmountModalTypography {
    val nH2: TextStyle
}

internal interface AmountModalShapes {
    val rFull: CornerBasedShape
}

private val White = Color(0xFFFAFAFA)
private val Black = Color(0xFF111114)
private val TrueBlack = Color(0xFF000000)
private val MediumBlack = Color(0xFF2B2C2D)
private val MediumWhite = Color(0xFFEFEEF0)

private fun amountModalColors(
    theme: Theme,
    isDarkModeEnabled: Boolean,
): AmountModalColors {
    return when (theme) {
        Theme.LIGHT -> object : AmountModalColors {
            override val pure = White
            override val pureInverse = Black
            override val medium = MediumWhite
        }

        Theme.DARK -> object : AmountModalColors {
            override val pure = Black
            override val pureInverse = White
            override val medium = MediumBlack
        }

        Theme.AMOLED_DARK -> object : AmountModalColors {
            override val pure = TrueBlack
            override val pureInverse = White
            override val medium = MediumBlack
        }

        Theme.AUTO -> if (isDarkModeEnabled) {
            amountModalColors(Theme.DARK, true)
        } else {
            amountModalColors(Theme.LIGHT, false)
        }
    }
}

private fun amountModalTypography(): AmountModalTypography {
    val openSans = FontFamily(
        Font(R.font.opensans_regular, FontWeight.Normal),
        Font(R.font.opensans_regular, FontWeight.Medium),
        Font(R.font.opensans_bold, FontWeight.Black),
        Font(R.font.opensans_semibold, FontWeight.SemiBold),
        Font(R.font.opensans_bold, FontWeight.Bold),
        Font(R.font.opensans_extrabold, FontWeight.ExtraBold),
    )

    return object : AmountModalTypography {
        override val nH2 = TextStyle(
            fontFamily = openSans,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            baselineShift = BaselineShift(0.075f),
        )
    }
}

private fun amountModalShapes(): AmountModalShapes {
    return object : AmountModalShapes {
        override val rFull = RoundedCornerShape(percent = 50)
    }
}
