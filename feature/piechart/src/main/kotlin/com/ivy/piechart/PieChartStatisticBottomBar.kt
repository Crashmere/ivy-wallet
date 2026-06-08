package com.ivy.piechart

import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ivy.data.model.TransactionType
import com.ivy.legacy.ui.theme.LegacyTheme
import com.ivy.ui.compose.navigationBarInset
import com.ivy.ui.compose.toDensityDp
import com.ivy.ui.R
import com.ivy.ui.theme.colors.Gradient
import com.ivy.ui.theme.colors.IvyGradients
import com.ivy.ui.theme.colors.IvyFixedColors.White
import com.ivy.legacy.ui.button.CloseButton
import com.ivy.legacy.ui.button.IvyButton
import com.ivy.ui.compose.gradientCutBackgroundTop

@Composable
internal fun BoxWithConstraintsScope.PieChartStatisticBottomBar(
    type: TransactionType,
    onClose: () -> Unit,
    onAdd: (TransactionType) -> Unit,
    modifier: Modifier = Modifier,
    bottomInset: Dp = navigationBarInset().toDensityDp()
) {
    PieChartStatisticActionsRow(
        modifier = modifier
            .align(Alignment.BottomCenter)
            .gradientCutBackgroundTop(LegacyTheme.colors.pure, LocalDensity.current)
            .padding(bottom = bottomInset)
            .padding(bottom = 16.dp)
    ) {
        Spacer(Modifier.width(20.dp))

        CloseButton {
            onClose()
        }

        Spacer(Modifier.weight(1f))

        val isIncome = type == TransactionType.INCOME
        IvyButton(
            iconStart = R.drawable.ic_plus,
            text = if (isIncome) {
                stringResource(
                    id = R.string.add_income
                )
            } else {
                stringResource(id = R.string.add_expense)
            },
            backgroundGradient = if (isIncome) IvyGradients.Green else Gradient.solid(LegacyTheme.colors.pureInverse),
            textStyle = LegacyTheme.typo.b2.copy(
                color = if (isIncome) White else LegacyTheme.colors.pure,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Start
            ),
            iconTint = if (isIncome) White else LegacyTheme.colors.pure
        ) {
            onAdd(type)
        }

        Spacer(Modifier.width(20.dp))
    }
}

@Composable
private fun PieChartStatisticActionsRow(
    modifier: Modifier = Modifier,
    lineColor: Color = LegacyTheme.colors.medium,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                drawLine(
                    color = lineColor,
                    strokeWidth = 2.dp.toPx(),
                    start = Offset(
                        x = 0f,
                        y = size.height / 2
                    ),
                    end = Offset(
                        x = size.width,
                        y = size.height / 2
                    )
                )
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        content()
    }
}
