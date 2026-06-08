package com.ivy.ui.summary

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivy.ui.R

internal object IncomeExpenseSummaryTheme {
    val colors: IncomeExpenseSummaryColors = incomeExpenseSummaryColors()

    val typo: IncomeExpenseSummaryTypography = incomeExpenseSummaryTypography()

    val shapes: IncomeExpenseSummaryShapes = incomeExpenseSummaryShapes()
}

internal interface IncomeExpenseSummaryColors {
    val gray: Color
    val green: Color
    val mediumBlack: Color
    val mediumWhite: Color
}

internal interface IncomeExpenseSummaryTypography {
    val c: TextStyle
    val b2: TextStyle
    val nB1: TextStyle
}

internal interface IncomeExpenseSummaryShapes {
    val r2: CornerBasedShape
    val rFull: CornerBasedShape
}

private fun incomeExpenseSummaryColors(): IncomeExpenseSummaryColors {
    return object : IncomeExpenseSummaryColors {
        override val gray = Color(0xFF939199)
        override val green = Color(0xFF14CC9E)
        override val mediumBlack = Color(0xFF2B2C2D)
        override val mediumWhite = Color(0xFFEFEEF0)
    }
}

private fun incomeExpenseSummaryTypography(): IncomeExpenseSummaryTypography {
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

    return object : IncomeExpenseSummaryTypography {
        override val c = TextStyle(
            fontFamily = raleWay,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 12.sp,
            baselineShift = BaselineShift(0.2f),
        )

        override val b2 = TextStyle(
            fontFamily = raleWay,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            baselineShift = BaselineShift(0.2f),
        )

        override val nB1 = TextStyle(
            fontFamily = openSans,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            baselineShift = BaselineShift(0.075f),
        )
    }
}

private fun incomeExpenseSummaryShapes(): IncomeExpenseSummaryShapes {
    return object : IncomeExpenseSummaryShapes {
        override val r2 = RoundedCornerShape(24.dp)
        override val rFull = RoundedCornerShape(percent = 50)
    }
}
