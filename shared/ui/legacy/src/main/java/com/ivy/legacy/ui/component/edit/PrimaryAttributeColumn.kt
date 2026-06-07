package com.ivy.legacy.ui.component.edit

import androidx.annotation.DrawableRes
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ivy.design.l0_system.LegacyTheme
import com.ivy.design.l0_system.style
import com.ivy.ui.legacy.clickableNoIndication
import com.ivy.ui.legacy.rememberInteractionSource
import com.ivy.wallet.ui.theme.components.IvyIcon
import androidx.compose.ui.res.stringResource
import com.ivy.ui.R

@Deprecated("Old design system. Use `:ivy-design` and Material3")
@Composable
fun PrimaryAttributeColumn(
    @DrawableRes icon: Int,
    title: String,
    TitleRowExtra: (@Composable RowScope.() -> Unit)? = null,
    onClick: () -> Unit,
    Content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(LegacyTheme.shapes.r4)
            .border(2.dp, LegacyTheme.colors.medium, LegacyTheme.shapes.r4)
            .clickableNoIndication(rememberInteractionSource(), onClick = onClick),
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IvyIcon(icon = icon)

            Spacer(Modifier.width(8.dp))

            Text(
                text = title,
                style = LegacyTheme.typo.b2.style(
                    color = LegacyTheme.colors.pureInverse,
                    fontWeight = FontWeight.ExtraBold
                )
            )

            TitleRowExtra?.invoke(this)
        }

        Content()
    }
}