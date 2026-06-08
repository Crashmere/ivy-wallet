package com.ivy.budgets

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ivy.data.model.Category
import com.ivy.legacy.ui.theme.LegacyTheme
import com.ivy.data.model.Budget
import com.ivy.ui.compose.selectEndTextFieldValue
import com.ivy.ui.R
import com.ivy.data.model.CreateBudgetData
import com.ivy.legacy.ui.icon.ItemIconSDefaultIcon
import com.ivy.legacy.ui.modal.DeleteModal
import com.ivy.legacy.ui.modal.IvyModal
import com.ivy.legacy.ui.modal.ModalAmountSection
import com.ivy.legacy.ui.modal.ModalTitle
import com.ivy.legacy.ui.modal.edit.AmountModal
import com.ivy.ui.theme.colors.findContrastTextColor
import com.ivy.ui.theme.colors.toComposeColor
import com.ivy.ui.compose.thenIf
import java.util.UUID

internal data class BudgetModalData(
    val budget: Budget?,

    val baseCurrency: String,
    val categories: List<Category>,

    val id: UUID = UUID.randomUUID(),
    val autoFocusKeyboard: Boolean = true,
)

@Composable
internal fun BoxWithConstraintsScope.BudgetModal(
    modal: BudgetModalData?,

    onCreate: (CreateBudgetData) -> Unit,
    onEdit: (Budget) -> Unit,
    onDelete: (Budget) -> Unit,
    dismiss: () -> Unit
) {
    val initialBudget = modal?.budget
    var nameTextFieldValue by remember(modal) {
        mutableStateOf(selectEndTextFieldValue(initialBudget?.name))
    }
    var amount by remember(modal) {
        mutableDoubleStateOf(initialBudget?.amount ?: 0.0)
    }
    var categoryIds by remember(modal) {
        mutableStateOf(modal?.budget?.parseCategoryIds() ?: emptyList())
    }
    var accountIds by remember(modal) {
        mutableStateOf(modal?.budget?.parseAccountIds() ?: emptyList())
    }

    var amountModalVisible by remember(modal) { mutableStateOf(false) }
    var deleteModalVisible by remember(modal) { mutableStateOf(false) }

    IvyModal(
        id = modal?.id,
        visible = modal != null,
        dismiss = dismiss,
        PrimaryAction = {
            BudgetModalAddSave(
                isEdit = modal?.budget != null,
                enabled = nameTextFieldValue.text.isNullOrBlank().not() && amount > 0.0
            ) {
                if (initialBudget != null) {
                    onEdit(
                        initialBudget.copy(
                            name = nameTextFieldValue.text.trim(),
                            amount = amount,
                            categoryIdsSerialized = Budget.serialize(categoryIds),
                            accountIdsSerialized = Budget.serialize(accountIds)
                        )
                    )
                } else {
                    onCreate(
                        CreateBudgetData(
                            name = nameTextFieldValue.text.trim(),
                            amount = amount,
                            categoryIdsSerialized = Budget.serialize(categoryIds),
                            accountIdsSerialized = Budget.serialize(accountIds)
                        )
                    )
                }

                dismiss()
            }
        }
    ) {
        Spacer(Modifier.height(32.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            ModalTitle(
                text = if (modal?.budget != null) {
                    stringResource(
                        R.string.edit_budget
                    )
                } else {
                    stringResource(R.string.create_budget)
                }
            )

            if (initialBudget != null) {
                Spacer(Modifier.weight(1f))

                BudgetModalDelete {
                    deleteModalVisible = true
                }

                Spacer(Modifier.width(24.dp))
            }
        }

        Spacer(Modifier.height(24.dp))

        BudgetNameInput(
            hint = stringResource(R.string.budget_name),
            autoFocusKeyboard = modal?.autoFocusKeyboard ?: true,
            textFieldValue = nameTextFieldValue,
            setTextFieldValue = {
                nameTextFieldValue = it
            }
        )

        Spacer(Modifier.height(24.dp))

        CategoriesRow(
            categories = modal?.categories ?: emptyList(),
            budgetCategoryIds = categoryIds,
            onSetBudgetCategoryIds = {
                categoryIds = it
            }
        )

        Spacer(Modifier.height(24.dp))

        ModalAmountSection(
            label = stringResource(R.string.budget_amount_uppercase),
            currency = modal?.baseCurrency ?: "",
            amount = amount,
            amountPaddingTop = 24.dp,
            amountPaddingBottom = 0.dp
        ) {
            amountModalVisible = true
        }
    }

    val amountModalId = remember(modal, amount) {
        UUID.randomUUID()
    }
    AmountModal(
        id = amountModalId,
        visible = amountModalVisible,
        currency = modal?.baseCurrency ?: "",
        initialAmount = amount,
        dismiss = { amountModalVisible = false }
    ) {
        amount = it
    }

    DeleteModal(
        visible = deleteModalVisible,
        title = stringResource(R.string.confirm_deletion),
        description = stringResource(
            R.string.confirm_budget_deletion_warning,
            nameTextFieldValue.text
        ),
        dismiss = { deleteModalVisible = false }
    ) {
        if (initialBudget != null) {
            onDelete(initialBudget)
        }
        deleteModalVisible = false
        dismiss()
    }
}

@Composable
private fun CategoriesRow(
    categories: List<Category>,
    budgetCategoryIds: List<UUID>,

    onSetBudgetCategoryIds: (List<UUID>) -> Unit,
) {
    Text(
        modifier = Modifier.padding(start = 32.dp),
        text = determineBudgetType(budgetCategoryIds.size),
        style = LegacyTheme.typo.b1.copy(
            fontWeight = FontWeight.Medium,
            color = LegacyTheme.colors.pureInverse,
            textAlign = TextAlign.Start
        )
    )

    Spacer(Modifier.height(16.dp))

    LazyRow(
        modifier = Modifier.testTag("budget_categories_row")
    ) {
        item {
            Spacer(Modifier.width(24.dp))
        }

        items(items = categories) { category ->
            BudgetCategoryListItem(
                icon = category.icon?.id,
                defaultIcon = R.drawable.ic_custom_category_s,
                text = category.name.value,
                selectedColor = category.color.value.toComposeColor().takeIf {
                    budgetCategoryIds.contains(category.id.value)
                }
            ) { selected ->
                if (selected) {
                    // remove category
                    onSetBudgetCategoryIds(budgetCategoryIds.filter { it != category.id.value })
                } else {
                    // add category
                    onSetBudgetCategoryIds(budgetCategoryIds.plus(category.id.value))
                }
            }
        }

        item {
            Spacer(Modifier.width(24.dp))
        }
    }
}

@Composable
private fun BudgetCategoryListItem(
    icon: String?,
    @DrawableRes defaultIcon: Int,
    text: String,
    selectedColor: Color?,
    onClick: (selected: Boolean) -> Unit
) {
    val textColor = if (selectedColor != null) {
        findContrastTextColor(selectedColor)
    } else {
        LegacyTheme.colors.pureInverse
    }

    val medium = LegacyTheme.colors.medium
    val rFull = LegacyTheme.shapes.rFull

    Row(
        modifier = Modifier
            .clip(LegacyTheme.shapes.rFull)
            .thenIf(selectedColor == null) {
                border(2.dp, medium, rFull)
            }
            .thenIf(selectedColor != null) {
                background(selectedColor!!, rFull)
            }
            .clickable {
                onClick(selectedColor != null)
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(12.dp))

        ItemIconSDefaultIcon(
            iconName = icon,
            defaultIcon = defaultIcon,
            tint = textColor
        )

        Spacer(Modifier.width(4.dp))

        Text(
            modifier = Modifier.padding(vertical = 10.dp),
            text = text,
            style = LegacyTheme.typo.b2.copy(
                color = textColor,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Start
            )
        )

        Spacer(Modifier.width(24.dp))
    }

    Spacer(Modifier.width(12.dp))
}
