package com.ivy.planned.list

import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ivy.legacy.design.l0_system.LegacyTheme
import com.ivy.ui.legacy.navigationBarInset
import com.ivy.ui.legacy.toDensityDp
import com.ivy.ui.R
import com.ivy.legacy.ui.component.ActionsRow
import com.ivy.legacy.ui.component.CloseButton
import com.ivy.legacy.ui.component.IvyOutlinedButton
import com.ivy.legacy.ui.theme.gradientCutBackgroundTop

@Composable
fun BoxWithConstraintsScope.PlannedPaymentsBottomBar(
    bottomInset: Dp = navigationBarInset().toDensityDp(),
    onClose: () -> Unit,
    onAdd: () -> Unit
) {
    ActionsRow(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .gradientCutBackgroundTop(LegacyTheme.colors.pure, LocalDensity.current)
            .padding(bottom = bottomInset)
            .padding(bottom = 24.dp)
    ) {
        Spacer(Modifier.width(20.dp))

        CloseButton {
            onClose()
        }

        Spacer(Modifier.weight(1f))

        IvyOutlinedButton(
            iconStart = R.drawable.ic_planned_payments,
            text = stringResource(R.string.add_payment),
            solidBackground = true
        ) {
            onAdd()
        }

        Spacer(Modifier.width(20.dp))
    }
}