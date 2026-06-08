package com.ivy.importdata.csvimport.flow

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ivy.legacy.ui.theme.LegacyTheme
import com.ivy.legacy.ui.theme.style
import com.ivy.ui.R

@Composable
internal fun ImportToolbar(
    hasSkip: Boolean,
    onBack: () -> Unit,
    onSkip: () -> Unit,
) {
    ImportToolbarFrame(onBack = onBack) {
        if (hasSkip) {
            Spacer(Modifier.weight(1f))

            Text(
                modifier = Modifier
                    .clip(LegacyTheme.shapes.rFull)
                    .clickable { onSkip() }
                    .padding(all = 16.dp),
                text = stringResource(R.string.skip),
                style = LegacyTheme.typo.b2.style(
                    color = LegacyTheme.colors.gray,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(Modifier.width(32.dp))
        }
    }
}

@Composable
private fun ImportToolbarFrame(
    onBack: () -> Unit,
    content: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(LegacyTheme.colors.pure)
            .padding(top = 16.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(20.dp))

        Icon(
            modifier = Modifier
                .clip(CircleShape)
                .background(LegacyTheme.colors.pure, CircleShape)
                .border(2.dp, LegacyTheme.colors.medium, CircleShape)
                .clickable(onClick = onBack)
                .padding(6.dp),
            painter = painterResource(id = R.drawable.ic_back),
            contentDescription = "back",
            tint = LegacyTheme.colors.pureInverse,
        )

        content()
    }
}
