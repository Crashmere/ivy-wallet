package com.ivy.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ivy.ui.R
import com.ivy.ui.compose.OutlinedPillButton
import com.ivy.ui.modal.IvyModal
import com.ivy.ui.modal.ModalPositiveButton
import com.ivy.ui.modal.ModalTitle
import java.util.UUID

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun BoxScope.TransactionsFilterModal(
    visible: Boolean,
    filter: AccountTransactionFilter?,
    onToggleCategory: (UUID) -> Unit,
    onToggleUncategorized: () -> Unit,
    onToggleTag: (UUID) -> Unit,
    onClear: () -> Unit,
    dismiss: () -> Unit,
) {
    val modalId = remember(visible) {
        if (visible) UUID.randomUUID() else null
    }

    IvyModal(
        id = modalId,
        visible = visible,
        dismiss = dismiss,
        SecondaryActions = {
            if (filter?.isActive == true) {
                FilterResetButton(onClick = onClear)
            }
        },
        PrimaryAction = {
            ModalPositiveButton(
                text = "完成",
                iconStart = R.drawable.ic_check,
                onClick = dismiss
            )
        }
    ) {
        Spacer(Modifier.height(32.dp))

        ModalTitle(text = "筛选交易")

        Spacer(Modifier.height(24.dp))

        if (filter != null) {
            if (filter.availableCategories.isNotEmpty() || filter.hasUncategorized) {
                FilterSectionLabel(text = "类别")

                Spacer(Modifier.height(12.dp))

                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    filter.availableCategories.forEach { category ->
                        FilterChip(
                            text = category.name.value,
                            selected = category.id.value in filter.selectedCategoryIds,
                            onClick = { onToggleCategory(category.id.value) }
                        )
                    }
                    if (filter.hasUncategorized) {
                        FilterChip(
                            text = "无类别",
                            selected = filter.uncategorizedSelected,
                            onClick = onToggleUncategorized
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
            }

            if (filter.availableTags.isNotEmpty()) {
                FilterSectionLabel(text = "标签")

                Spacer(Modifier.height(12.dp))

                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    filter.availableTags.forEach { tag ->
                        FilterChip(
                            text = "#${tag.name.value}",
                            selected = tag.id.value in filter.selectedTagIds,
                            onClick = { onToggleTag(tag.id.value) }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(48.dp))
    }
}

@Composable
private fun FilterSectionLabel(text: String) {
    Text(
        modifier = Modifier.padding(horizontal = 24.dp),
        text = text,
        style = TransactionsTheme.typo.b2.copy(
            color = TransactionsTheme.colors.pureInverse,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start,
        )
    )
}

@Composable
private fun FilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Text(
        modifier = Modifier
            .clip(CircleShape)
            .background(
                color = if (selected) {
                    TransactionsTheme.colors.pureInverse
                } else {
                    Color.Transparent
                },
                shape = CircleShape
            )
            .border(
                width = 2.dp,
                color = if (selected) {
                    TransactionsTheme.colors.pureInverse
                } else {
                    TransactionsTheme.colors.medium
                },
                shape = CircleShape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        text = text,
        style = TransactionsTheme.typo.c.copy(
            color = if (selected) {
                TransactionsTheme.colors.pure
            } else {
                TransactionsTheme.colors.pureInverse
            },
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Start,
        )
    )
}

@Composable
private fun FilterResetButton(onClick: () -> Unit) {
    OutlinedPillButton(
        text = "重置",
        iconStart = null,
        shape = TransactionsTheme.shapes.rFull,
        solidBackground = false,
        backgroundColor = TransactionsTheme.colors.pure,
        iconTint = TransactionsTheme.colors.pureInverse,
        borderColor = TransactionsTheme.colors.medium,
        textStyle = TransactionsTheme.typo.b2.copy(
            fontWeight = FontWeight.Bold,
            color = TransactionsTheme.colors.pureInverse,
            textAlign = TextAlign.Start,
        ),
        onClick = onClick
    )
}
