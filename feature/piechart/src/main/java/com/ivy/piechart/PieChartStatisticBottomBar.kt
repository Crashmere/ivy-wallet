package com.ivy.piechart

import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ivy.base.model.TransactionType
import com.ivy.legacy.ui.theme.system.LegacyTheme
import com.ivy.legacy.ui.theme.system.style
import com.ivy.legacy.ui.navigationBarInset
import com.ivy.legacy.ui.toDensityDp
import com.ivy.ui.R
import com.ivy.legacy.ui.theme.Gradient
import com.ivy.legacy.ui.theme.GradientGreen
import com.ivy.legacy.ui.theme.White
import com.ivy.legacy.ui.component.ActionsRow
import com.ivy.legacy.ui.component.CloseButton
import com.ivy.legacy.ui.component.IvyButton
import com.ivy.legacy.ui.theme.gradientCutBackgroundTop

@Composable
fun BoxWithConstraintsScope.PieChartStatisticBottomBar(
    type: TransactionType,
    onClose: () -> Unit,
    onAdd: (TransactionType) -> Unit,
    modifier: Modifier = Modifier,
    bottomInset: Dp = navigationBarInset().toDensityDp()
) {
    ActionsRow(
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
            backgroundGradient = if (isIncome) GradientGreen else Gradient.solid(LegacyTheme.colors.pureInverse),
            textStyle = LegacyTheme.typo.b2.style(
                color = if (isIncome) White else LegacyTheme.colors.pure,
                fontWeight = FontWeight.ExtraBold
            ),
            iconTint = if (isIncome) White else LegacyTheme.colors.pure
        ) {
            onAdd(type)
        }

        Spacer(Modifier.width(20.dp))
    }
}
