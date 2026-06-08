package com.ivy.importdata.csvimport.flow

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ivy.legacy.ui.theme.LegacyTheme
import com.ivy.legacy.ui.theme.style
import com.ivy.ui.R
import com.ivy.legacy.ui.theme.Gray
import com.ivy.legacy.ui.component.IvyToolbar

@Composable
fun ImportToolbar(
    hasSkip: Boolean,
    onBack: () -> Unit,
    onSkip: () -> Unit,
) {
    IvyToolbar(onBack = onBack) {
        if (hasSkip) {
            Spacer(Modifier.weight(1f))

            Text(
                modifier = Modifier
                    .clip(LegacyTheme.shapes.rFull)
                    .clickable { onSkip() }
                    .padding(all = 16.dp),
                text = stringResource(R.string.skip),
                style = LegacyTheme.typo.b2.style(
                    color = Gray,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(Modifier.width(32.dp))
        }
    }
}
