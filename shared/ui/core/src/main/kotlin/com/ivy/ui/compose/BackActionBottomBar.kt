package com.ivy.ui.compose

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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ivy.ui.R

/**
 * A shared bottom action bar with a circular back button on the left and a
 * single primary action on the right, separated by a horizontal divider line.
 * Used to keep the Categories, Reports and Bulk edit screens visually consistent.
 */
@Composable
fun BoxWithConstraintsScope.BackActionBottomBar(
    pure: Color,
    medium: Color,
    pureInverse: Color,
    onBack: () -> Unit,
    bottomInset: Dp = navigationBarInset().toDensityDp(),
    primaryAction: @Composable () -> Unit,
) {
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

        BottomBarBackButton(
            pure = pure,
            medium = medium,
            pureInverse = pureInverse,
            onBack = onBack
        )

        Spacer(Modifier.weight(1f))

        primaryAction()

        Spacer(Modifier.width(20.dp))
    }
}

@Composable
private fun BottomBarBackButton(
    pure: Color,
    medium: Color,
    pureInverse: Color,
    onBack: () -> Unit,
) {
    Icon(
        modifier = Modifier
            .rotate(180f)
            .clip(CircleShape)
            .background(pure, CircleShape)
            .border(2.dp, medium, CircleShape)
            .clickable(onClick = onBack)
            .padding(6.dp),
        painter = painterResource(id = R.drawable.ic_arrow_right),
        contentDescription = "back",
        tint = pureInverse,
    )
}
