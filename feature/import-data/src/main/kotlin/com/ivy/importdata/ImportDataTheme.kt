package com.ivy.importdata

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.CircleShape
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

internal object ImportDataTheme {
    val colors: ImportDataColors
        @Composable
        @ReadOnlyComposable
        get() = importDataColors(
            theme = LocalThemeState.current.theme,
            isDarkModeEnabled = isSystemInDarkTheme(),
        )

    val typo: ImportDataTypography = importDataTypography()

    val shapes: ImportDataShapes = importDataShapes()
}

internal interface ImportDataColors {
    val pure: Color
    val pureInverse: Color

    val gray: Color
    val medium: Color
    val mediumInverse: Color

    val primary: Color
    val primary1: Color

    val green: Color
    val green1: Color

    val orange: Color
    val orange1: Color

    val red: Color
    val red1: Color
    val red1Inverse: Color

    val isLight: Boolean
}

internal interface ImportDataTypography {
    val h1: TextStyle
    val h2: TextStyle
    val b1: TextStyle
    val b2: TextStyle
    val c: TextStyle

    val nH1: TextStyle
    val nH2: TextStyle
    val nB1: TextStyle
    val nB2: TextStyle
    val nC: TextStyle
}

internal interface ImportDataShapes {
    val r1: CornerBasedShape
    val r1Top: CornerBasedShape
    val r1Bot: CornerBasedShape

    val r2: CornerBasedShape
    val r2Top: CornerBasedShape
    val r2Bot: CornerBasedShape

    val r3: CornerBasedShape
    val r3Top: CornerBasedShape
    val r3Bot: CornerBasedShape

    val r4: CornerBasedShape
    val r4Top: CornerBasedShape
    val r4Bot: CornerBasedShape

    val rFull: CornerBasedShape
        get() = RoundedCornerShape(percent = 50)

    val circle: CornerBasedShape
        get() = CircleShape
}

private val White = Color(0xFFFAFAFA)
private val Black = Color(0xFF111114)
private val TrueBlack = Color(0xFF000000)
private val Purple = Color(0xFF6B4DFF)
private val IvyDark = Color(0xFF352680)
private val IvyLight = Color(0xFFD5CCFF)
private val Green = Color(0xFF14CC9E)
private val GreenLight = Color(0xFFAAF2E0)
private val GreenDark = Color(0xFF0A664F)
private val Orange = Color(0xFFF29F30)
private val OrangeLight = Color(0xFFFFDEB3)
private val OrangeDark = Color(0xFF734B17)
private val Red = Color(0xFFFF4060)
private val RedLight = Color(0xFFFFCCD5)
private val RedDark = Color(0xFF801919)
private val Gray = Color(0xFF939199)
private val MediumBlack = Color(0xFF2B2C2D)
private val MediumWhite = Color(0xFFEFEEF0)

private fun importDataColors(
    theme: Theme,
    isDarkModeEnabled: Boolean,
): ImportDataColors {
    return when (theme) {
        Theme.LIGHT -> object : ImportDataColors {
            override val pure = White
            override val pureInverse = Black
            override val gray = Gray
            override val medium = MediumWhite
            override val mediumInverse = MediumBlack

            override val primary = Purple
            override val primary1 = IvyDark

            override val green = Green
            override val green1 = GreenLight

            override val orange = Orange
            override val orange1 = OrangeLight

            override val red = Red
            override val red1 = RedLight
            override val red1Inverse = RedDark

            override val isLight = true
        }

        Theme.DARK -> object : ImportDataColors {
            override val pure = Black
            override val pureInverse = White
            override val gray = Gray
            override val medium = MediumBlack
            override val mediumInverse = MediumWhite

            override val primary = Purple
            override val primary1 = IvyLight

            override val green = Green
            override val green1 = GreenDark

            override val orange = Orange
            override val orange1 = OrangeDark

            override val red = Red
            override val red1 = RedDark
            override val red1Inverse = RedLight

            override val isLight = false
        }

        Theme.AMOLED_DARK -> object : ImportDataColors {
            override val pure = TrueBlack
            override val pureInverse = White
            override val gray = Gray
            override val medium = MediumBlack
            override val mediumInverse = MediumWhite

            override val primary = Purple
            override val primary1 = IvyLight

            override val green = Green
            override val green1 = GreenDark

            override val orange = Orange
            override val orange1 = OrangeDark

            override val red = Red
            override val red1 = RedDark
            override val red1Inverse = RedLight

            override val isLight = false
        }

        Theme.AUTO -> if (isDarkModeEnabled) {
            importDataColors(Theme.DARK, true)
        } else {
            importDataColors(Theme.LIGHT, false)
        }
    }
}

private fun importDataTypography(): ImportDataTypography {
    val openSans = FontFamily(
        Font(R.font.opensans_regular, FontWeight.Normal),
        Font(R.font.opensans_regular, FontWeight.Medium),
        Font(R.font.opensans_bold, FontWeight.Black),
        Font(R.font.opensans_semibold, FontWeight.SemiBold),
        Font(R.font.opensans_bold, FontWeight.Bold),
        Font(R.font.opensans_extrabold, FontWeight.ExtraBold),
    )

    val raleWay = FontFamily(
        Font(R.font.raleway_regular, FontWeight.Normal),
        Font(R.font.raleway_medium, FontWeight.Medium),
        Font(R.font.raleway_black, FontWeight.Black),
        Font(R.font.raleway_light, FontWeight.Light),
        Font(R.font.raleway_semibold, FontWeight.SemiBold),
        Font(R.font.raleway_bold, FontWeight.Bold),
        Font(R.font.raleway_extrabold, FontWeight.ExtraBold),
    )

    val h1 = 40.sp
    val h2 = 32.sp
    val b1 = 20.sp
    val b2 = 16.sp
    val c = 12.sp

    return object : ImportDataTypography {
        override val h1 = TextStyle(
            fontFamily = raleWay,
            fontWeight = FontWeight.Black,
            fontSize = h1,
            baselineShift = BaselineShift(0.2f),
        )
        override val h2 = TextStyle(
            fontFamily = raleWay,
            fontWeight = FontWeight.ExtraBold,
            fontSize = h2,
            baselineShift = BaselineShift(0.2f),
        )
        override val b1 = TextStyle(
            fontFamily = raleWay,
            fontWeight = FontWeight.Bold,
            fontSize = b1,
            baselineShift = BaselineShift(0.2f),
        )
        override val b2 = TextStyle(
            fontFamily = raleWay,
            fontWeight = FontWeight.Medium,
            fontSize = b2,
            baselineShift = BaselineShift(0.2f),
        )
        override val c = TextStyle(
            fontFamily = raleWay,
            fontWeight = FontWeight.ExtraBold,
            fontSize = c,
            baselineShift = BaselineShift(0.2f),
        )

        override val nH1 = TextStyle(
            fontFamily = openSans,
            fontWeight = FontWeight.Bold,
            fontSize = h1,
            baselineShift = BaselineShift(0.075f),
        )
        override val nH2 = TextStyle(
            fontFamily = openSans,
            fontWeight = FontWeight.Bold,
            fontSize = h2,
            baselineShift = BaselineShift(0.075f),
        )
        override val nB1 = TextStyle(
            fontFamily = openSans,
            fontWeight = FontWeight.Bold,
            fontSize = b1,
            baselineShift = BaselineShift(0.075f),
        )
        override val nB2 = TextStyle(
            fontFamily = openSans,
            fontWeight = FontWeight.Normal,
            fontSize = b2,
            baselineShift = BaselineShift(0.075f),
        )
        override val nC = TextStyle(
            fontFamily = openSans,
            fontWeight = FontWeight.Bold,
            fontSize = c,
            baselineShift = BaselineShift(0.075f),
        )
    }
}

private fun importDataShapes(): ImportDataShapes {
    return object : ImportDataShapes {
        override val r1 = RoundedCornerShape(32.dp)
        override val r1Top = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
        override val r1Bot = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)

        override val r2 = RoundedCornerShape(24.dp)
        override val r2Top = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        override val r2Bot = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)

        override val r3 = RoundedCornerShape(20.dp)
        override val r3Top = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        override val r3Bot = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)

        override val r4 = RoundedCornerShape(16.dp)
        override val r4Top = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        override val r4Bot = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
    }
}
