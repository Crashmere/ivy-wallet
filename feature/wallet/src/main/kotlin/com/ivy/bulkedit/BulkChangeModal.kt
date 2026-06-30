package com.ivy.bulkedit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ivy.data.model.TransactionHistoryTransaction
import com.ivy.ui.R
import com.ivy.ui.compose.ResourceIcon
import com.ivy.ui.modal.IvyModal
import com.ivy.ui.modal.ModalTitle
import java.util.UUID

private enum class BulkAttr { CATEGORY, ACCOUNT, TAG }
private enum class TagAction { ADD, REMOVE }

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun BoxScope.BulkChangeModal(
    visible: Boolean,
    state: BulkEditState,
    onEvent: (BulkEditEvent) -> Unit,
    dismiss: () -> Unit,
) {
    val modalId = remember(visible) {
        if (visible) UUID.randomUUID() else null
    }

    var attribute by remember(modalId) { mutableStateOf(BulkAttr.CATEGORY) }
    var categoryChosen by remember(modalId) { mutableStateOf(false) }
    var newCategoryId: UUID? by remember(modalId) { mutableStateOf(null) }
    var newAccountId: UUID? by remember(modalId) { mutableStateOf(null) }
    var tagAction by remember(modalId) { mutableStateOf(TagAction.ADD) }
    var newTagId: UUID? by remember(modalId) { mutableStateOf(null) }

    val matchingTags = remember(state.matchingTransactions) {
        state.matchingTransactions
            .filterIsInstance<TransactionHistoryTransaction>()
            .flatMap { it.tags }
            .distinctBy { it.id.value }
    }

    val canApply = when (attribute) {
        BulkAttr.CATEGORY -> categoryChosen
        BulkAttr.ACCOUNT -> newAccountId != null
        BulkAttr.TAG -> newTagId != null
    }

    val apply: () -> Unit = {
        when (attribute) {
            BulkAttr.CATEGORY -> onEvent(BulkEditEvent.ApplyCategoryChange(newCategoryId))
            BulkAttr.ACCOUNT -> newAccountId?.let { onEvent(BulkEditEvent.ApplyAccountChange(it)) }
            BulkAttr.TAG -> newTagId?.let {
                if (tagAction == TagAction.ADD) {
                    onEvent(BulkEditEvent.ApplyAddTag(it))
                } else {
                    onEvent(BulkEditEvent.ApplyRemoveTag(it))
                }
            }
        }
        dismiss()
    }

    IvyModal(
        id = modalId,
        visible = visible,
        dismiss = dismiss,
        PrimaryAction = {
            ApplyButton(enabled = canApply, onClick = apply)
        }
    ) {
        Spacer(Modifier.height(32.dp))

        ModalTitle(text = "批量修改 ${state.matchingCount} 笔交易")

        Spacer(Modifier.height(8.dp))

        Text(
            modifier = Modifier.padding(horizontal = 24.dp),
            text = "选择要统一修改的属性，并选定新的值。",
            style = BulkEditTheme.typo.c.copy(
                color = BulkEditTheme.colors.gray,
                textAlign = TextAlign.Start
            )
        )

        Spacer(Modifier.height(20.dp))

        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(text = "类别", selected = attribute == BulkAttr.CATEGORY) {
                attribute = BulkAttr.CATEGORY
            }
            FilterChip(text = "账户", selected = attribute == BulkAttr.ACCOUNT) {
                attribute = BulkAttr.ACCOUNT
            }
            FilterChip(text = "标签", selected = attribute == BulkAttr.TAG) {
                attribute = BulkAttr.TAG
            }
        }

        Spacer(Modifier.height(24.dp))

        when (attribute) {
            BulkAttr.CATEGORY -> {
                ModalSubTitle(text = "改为类别")
                Spacer(Modifier.height(12.dp))
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    state.allCategories.forEach { category ->
                        FilterChip(
                            text = category.name.value,
                            selected = categoryChosen && newCategoryId == category.id.value
                        ) {
                            categoryChosen = true
                            newCategoryId = category.id.value
                        }
                    }
                    FilterChip(
                        text = "无类别",
                        selected = categoryChosen && newCategoryId == null
                    ) {
                        categoryChosen = true
                        newCategoryId = null
                    }
                }
            }

            BulkAttr.ACCOUNT -> {
                ModalSubTitle(text = "移动到账户")
                Spacer(Modifier.height(8.dp))
                Text(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    text = "转账类型的交易会被自动跳过。",
                    style = BulkEditTheme.typo.c.copy(
                        color = BulkEditTheme.colors.gray,
                        textAlign = TextAlign.Start
                    )
                )
                Spacer(Modifier.height(12.dp))
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    state.allAccounts.forEach { account ->
                        FilterChip(
                            text = account.name.value,
                            selected = newAccountId == account.id.value
                        ) {
                            newAccountId = account.id.value
                        }
                    }
                }
            }

            BulkAttr.TAG -> {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(text = "添加标签", selected = tagAction == TagAction.ADD) {
                        tagAction = TagAction.ADD
                        newTagId = null
                    }
                    FilterChip(text = "移除标签", selected = tagAction == TagAction.REMOVE) {
                        tagAction = TagAction.REMOVE
                        newTagId = null
                    }
                }

                Spacer(Modifier.height(16.dp))

                val tagOptions = if (tagAction == TagAction.ADD) {
                    state.allTags
                } else {
                    matchingTags
                }

                if (tagOptions.isEmpty()) {
                    Text(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        text = if (tagAction == TagAction.ADD) {
                            "暂无可用标签。"
                        } else {
                            "当前匹配的交易没有可移除的标签。"
                        },
                        style = BulkEditTheme.typo.c.copy(
                            color = BulkEditTheme.colors.gray,
                            textAlign = TextAlign.Start
                        )
                    )
                } else {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        tagOptions.forEach { tag ->
                            FilterChip(
                                text = "#${tag.name.value}",
                                selected = newTagId == tag.id.value
                            ) {
                                newTagId = tag.id.value
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(48.dp))
    }
}

@Composable
private fun ModalSubTitle(text: String) {
    Text(
        modifier = Modifier.padding(horizontal = 24.dp),
        text = text,
        style = BulkEditTheme.typo.b2.copy(
            color = BulkEditTheme.colors.pureInverse,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start
        )
    )
}

@Composable
private fun ApplyButton(
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(BulkEditTheme.shapes.rFull)
            .background(
                if (enabled) BulkEditTheme.colors.pureInverse else BulkEditTheme.colors.medium
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ResourceIcon(
            icon = R.drawable.ic_check,
            tint = if (enabled) BulkEditTheme.colors.pure else BulkEditTheme.colors.gray
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "应用修改",
            style = BulkEditTheme.typo.b2.copy(
                color = if (enabled) BulkEditTheme.colors.pure else BulkEditTheme.colors.gray,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start
            )
        )
    }
}
