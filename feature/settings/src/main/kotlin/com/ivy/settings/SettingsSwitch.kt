package com.ivy.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.ivy.legacy.ui.theme.LegacyTheme
import com.ivy.ui.animation.springBounce

@Composable
internal fun SettingsSwitch(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    onEnabledChange: (checked: Boolean) -> Unit
) {
    val color by animateColorAsState(
        targetValue = if (enabled) LegacyTheme.colors.green else LegacyTheme.colors.gray,
        animationSpec = springBounce()
    )

    Row(
        modifier = modifier
            .width(40.dp)
            .clip(LegacyTheme.shapes.rFull)
            .border(2.dp, color, LegacyTheme.shapes.rFull)
            .clickable {
                onEnabledChange(!enabled)
            }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val weightStart by animateFloatAsState(
            targetValue = if (enabled) 1f else 0f,
            animationSpec = springBounce()
        )

        Spacer(Modifier.width(4.dp))

        if (weightStart > 0) {
            Spacer(Modifier.weight(weightStart))
        }

        Spacer(
            modifier = Modifier
                .size(16.dp)
                .background(color, CircleShape)
        )

        val weightEnd = 1f - weightStart
        if (weightEnd > 0) {
            Spacer(Modifier.weight(weightEnd))
        }

        Spacer(Modifier.width(4.dp))
    }
}
