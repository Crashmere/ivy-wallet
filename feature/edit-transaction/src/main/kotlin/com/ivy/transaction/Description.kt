package com.ivy.transaction

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ivy.ui.R
import com.ivy.ui.compose.ResourceIcon
import com.ivy.ui.compose.clickableNoIndication
import com.ivy.ui.compose.rememberInteractionSource

@Composable
internal fun Description(
    description: String?,
    onAddDescription: () -> Unit,
    onEditDescription: (String) -> Unit
) {
    if (!description.isNullOrBlank()) {
        DescriptionText(
            description = description,
            onClick = {
                onEditDescription(description)
            }
        )
    } else {
        AddPrimaryAttributeButton(
            icon = R.drawable.ic_description,
            text = stringResource(R.string.add_description),
            onClick = onAddDescription
        )
    }
}

@Composable
private fun DescriptionText(
    description: String,
    onClick: () -> Unit,
) {
    PrimaryAttributeColumn(
        icon = R.drawable.ic_description,
        title = stringResource(R.string.description),
        onClick = onClick
    ) {
        Spacer(Modifier.height(12.dp))

        Text(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .testTag("trn_description"),
            text = description,
            style = EditTransactionTheme.typo.nB2.copy(
                color = EditTransactionTheme.colors.pureInverse,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Left
            ),
        )

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun AddPrimaryAttributeButton(
    @DrawableRes icon: Int,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(EditTransactionTheme.shapes.r4)
            .background(EditTransactionTheme.colors.medium, EditTransactionTheme.shapes.r4)
            .clickable(onClick = onClick)
            .padding(vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(16.dp))

        ResourceIcon(
            icon = icon,
            tint = EditTransactionTheme.colors.pureInverse
        )

        Spacer(Modifier.width(8.dp))

        Text(
            text = text,
            style = EditTransactionTheme.typo.b2.copy(
                color = EditTransactionTheme.colors.pureInverse,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start
            )
        )
    }
}

@Composable
private fun PrimaryAttributeColumn(
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
            .clip(EditTransactionTheme.shapes.r4)
            .border(2.dp, EditTransactionTheme.colors.medium, EditTransactionTheme.shapes.r4)
            .clickableNoIndication(rememberInteractionSource(), onClick = onClick),
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ResourceIcon(
                icon = icon,
                tint = EditTransactionTheme.colors.pureInverse
            )

            Spacer(Modifier.width(8.dp))

            Text(
                text = title,
                style = EditTransactionTheme.typo.b2.copy(
                    color = EditTransactionTheme.colors.pureInverse,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Start
                )
            )

            TitleRowExtra?.invoke(this)
        }

        Content()
    }
}
