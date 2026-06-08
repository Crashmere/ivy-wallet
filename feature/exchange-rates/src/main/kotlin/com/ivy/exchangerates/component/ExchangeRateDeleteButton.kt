package com.ivy.exchangerates.component

import com.ivy.exchangerates.ExchangeRatesTheme

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ivy.ui.compose.GradientIconButton
import com.ivy.ui.theme.colors.Gradient
import com.ivy.ui.theme.colors.IvyFixedColors.White
import com.ivy.ui.R

@Composable
internal fun ExchangeRateDeleteButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    GradientIconButton(
        modifier = modifier
            .size(48.dp)
            .testTag("exchange_rate_delete_button"),
        backgroundPadding = 6.dp,
        icon = R.drawable.ic_delete,
        backgroundGradient = Gradient.solid(ExchangeRatesTheme.colors.red),
        enabled = true,
        hasShadow = true,
        tint = White,
        onClick = onClick
    )
}
