package com.ivy.ui.modal

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.unit.sp
import com.ivy.ui.R

internal object ModalPrimaryButtonTheme {
    val colors: ModalPrimaryButtonColors = modalPrimaryButtonColors()

    val typo: ModalPrimaryButtonTypography = modalPrimaryButtonTypography()

    val shapes: ModalPrimaryButtonShapes = modalPrimaryButtonShapes()
}

internal interface ModalPrimaryButtonColors {
    val gray: Color
    val white: Color
}

internal interface ModalPrimaryButtonTypography {
    val b2: TextStyle
}

internal interface ModalPrimaryButtonShapes {
    val rFull: CornerBasedShape
}

private fun modalPrimaryButtonColors(): ModalPrimaryButtonColors {
    return object : ModalPrimaryButtonColors {
        override val gray = Color(0xFF939199)
        override val white = Color(0xFFFAFAFA)
    }
}

private fun modalPrimaryButtonTypography(): ModalPrimaryButtonTypography {
    val raleWay = FontFamily(
        Font(R.font.raleway_regular, FontWeight.Normal),
        Font(R.font.raleway_medium, FontWeight.Medium),
        Font(R.font.raleway_black, FontWeight.Black),
        Font(R.font.raleway_light, FontWeight.Light),
        Font(R.font.raleway_semibold, FontWeight.SemiBold),
        Font(R.font.raleway_bold, FontWeight.Bold),
        Font(R.font.raleway_extrabold, FontWeight.ExtraBold),
    )

    return object : ModalPrimaryButtonTypography {
        override val b2 = TextStyle(
            fontFamily = raleWay,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            baselineShift = BaselineShift(0.2f),
        )
    }
}

private fun modalPrimaryButtonShapes(): ModalPrimaryButtonShapes {
    return object : ModalPrimaryButtonShapes {
        override val rFull = RoundedCornerShape(percent = 50)
    }
}
