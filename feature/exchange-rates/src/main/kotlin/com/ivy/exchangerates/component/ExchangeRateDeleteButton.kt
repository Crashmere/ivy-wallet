package com.ivy.exchangerates.component

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ivy.legacy.ui.button.IvyCircleButton
import com.ivy.legacy.ui.theme.Gradient
import com.ivy.legacy.ui.theme.LegacyTheme
import com.ivy.legacy.ui.theme.White
import com.ivy.ui.R

@Composable
internal fun ExchangeRateDeleteButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    IvyCircleButton(
        modifier = modifier
            .size(48.dp)
            .testTag("exchange_rate_delete_button"),
        backgroundPadding = 6.dp,
        icon = R.drawable.ic_delete,
        backgroundGradient = Gradient.solid(LegacyTheme.colors.red),
        enabled = true,
        hasShadow = true,
        tint = White,
        onClick = onClick
    )
}
