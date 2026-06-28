package com.ivy.exchangerates

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ivy.ui.R
import com.ivy.ui.compose.GradientButton
import com.ivy.ui.compose.gradientCutBackgroundTop
import com.ivy.ui.compose.navigationBarInset
import com.ivy.ui.compose.toDensityDp
import com.ivy.ui.theme.colors.IvyGradients

@Composable
internal fun BoxWithConstraintsScope.ExchangeRatesBottomBar(
    onClose: () -> Unit,
    onAddRate: () -> Unit
) {
    ExchangeRatesBackBottomBar(onBack = onClose) {
        GradientButton(
            text = stringResource(R.string.add_manual_exchange_rate),
            backgroundGradient = IvyGradients.Ivy,
            disabledBackgroundColor = ExchangeRatesTheme.colors.gray,
            shape = ExchangeRatesTheme.shapes.rFull,
            textStyle = ExchangeRatesTheme.typo.b2.copy(
                color = Color(0xFFFAFAFA),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start
            ),
            iconStart = R.drawable.ic_plus,
            iconTint = Color(0xFFFAFAFA),
        ) {
            onAddRate()
        }
    }
}

@Composable
private fun BoxWithConstraintsScope.ExchangeRatesBackBottomBar(
    bottomInset: Dp = navigationBarInset().toDensityDp(),
    onBack: () -> Unit,
    primaryAction: @Composable () -> Unit,
) {
    val pure = ExchangeRatesTheme.colors.pure
    val medium = ExchangeRatesTheme.colors.medium

    Row(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .gradientCutBackgroundTop(pure, LocalDensity.current)
            .padding(bottom = bottomInset)
            .padding(bottom = 16.dp)
            .fillMaxWidth()
            .drawBehind {
                drawLine(
                    color = medium,
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
        Spacer(Modifier.width(20.dp))

        BottomBarBackButton(onBack = onBack)

        Spacer(Modifier.weight(1f))

        primaryAction()

        Spacer(Modifier.width(20.dp))
    }
}

@Composable
private fun BottomBarBackButton(
    onBack: () -> Unit
) {
    Icon(
        modifier = Modifier
            .rotate(180f)
            .clip(CircleShape)
            .background(ExchangeRatesTheme.colors.pure, CircleShape)
            .border(2.dp, ExchangeRatesTheme.colors.medium, CircleShape)
            .clickable(onClick = onBack)
            .padding(6.dp),
        painter = painterResource(id = R.drawable.ic_arrow_right),
        contentDescription = "back",
        tint = ExchangeRatesTheme.colors.pureInverse,
    )
}
