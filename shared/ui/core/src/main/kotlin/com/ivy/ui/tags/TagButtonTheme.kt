package com.ivy.ui.tags

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

internal object TagButtonTheme {
    val colors: TagButtonColors
        @Composable
        @ReadOnlyComposable
        get() = tagButtonColors(
            theme = LocalThemeState.current.theme,
            isDarkModeEnabled = isSystemInDarkTheme(),
        )

    val typo: TagButtonTypography = tagButtonTypography()

    val shapes: TagButtonShapes = tagButtonShapes()
}

internal interface TagButtonColors {
    val pure: Color
    val pureInverse: Color
    val gray: Color
    val mediumInverse: Color
    val selectedTag: Color
}

internal interface TagButtonTypography {
    val b2: TextStyle
}

internal interface TagButtonShapes {
    val rFull: CornerBasedShape
}

private val White = Color(0xFFFAFAFA)
private val Black = Color(0xFF111114)
private val TrueBlack = Color(0xFF000000)
private val Gray = Color(0xFF939199)
private val MediumWhite = Color(0xFFEFEEF0)
private val MediumBlack = Color(0xFF2B2C2D)
private val Orange3 = Color(0xFFFFC34C)

private fun tagButtonColors(
    theme: Theme,
    isDarkModeEnabled: Boolean,
): TagButtonColors {
    return when (theme) {
        Theme.LIGHT -> object : TagButtonColors {
            override val pure = White
            override val pureInverse = Black
            override val gray = Gray
            override val mediumInverse = MediumBlack
            override val selectedTag = Orange3
        }

        Theme.DARK -> object : TagButtonColors {
            override val pure = Black
            override val pureInverse = White
            override val gray = Gray
            override val mediumInverse = MediumWhite
            override val selectedTag = Orange3
        }

        Theme.AMOLED_DARK -> object : TagButtonColors {
            override val pure = TrueBlack
            override val pureInverse = White
            override val gray = Gray
            override val mediumInverse = MediumWhite
            override val selectedTag = Orange3
        }

        Theme.AUTO -> if (isDarkModeEnabled) {
            tagButtonColors(Theme.DARK, true)
        } else {
            tagButtonColors(Theme.LIGHT, false)
        }
    }
}

private fun tagButtonTypography(): TagButtonTypography {
    val raleWay = FontFamily(
        Font(R.font.raleway_regular, FontWeight.Normal),
        Font(R.font.raleway_medium, FontWeight.Medium),
        Font(R.font.raleway_black, FontWeight.Black),
        Font(R.font.raleway_light, FontWeight.Light),
        Font(R.font.raleway_semibold, FontWeight.SemiBold),
        Font(R.font.raleway_bold, FontWeight.Bold),
        Font(R.font.raleway_extrabold, FontWeight.ExtraBold),
    )

    return object : TagButtonTypography {
        override val b2 = TextStyle(
            fontFamily = raleWay,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            baselineShift = BaselineShift(0.2f),
        )
    }
}

private fun tagButtonShapes(): TagButtonShapes {
    return object : TagButtonShapes {
        override val rFull = RoundedCornerShape(percent = 50)
    }
}
