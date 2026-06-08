package com.ivy.ui.modal

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ivy.ui.compose.thenIf
import com.ivy.ui.compose.drawColoredShadow
import com.ivy.ui.platform.hideKeyboard
import com.ivy.ui.compose.onCompositionStart
import com.ivy.ui.R
import com.ivy.ui.theme.colors.Gradient
import com.ivy.ui.icon.ItemIconSDefaultIcon
import com.ivy.ui.compose.GradientIconButton
import com.ivy.ui.compose.OutlinedPillButton
import com.ivy.ui.compose.WrapContentRow
import com.ivy.ui.theme.colors.findContrastTextColor
import com.ivy.ui.theme.colors.toComposeColor
import java.util.UUID
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Suppress("ParameterNaming")
@ExperimentalFoundationApi
@Composable
fun BoxWithConstraintsScope.ChooseCategoryModal(
    id: UUID = UUID.randomUUID(),
    visible: Boolean,
    initialCategoryId: UUID?,
    categories: List<CategoryModalCategory>,

    showCategoryModal: (UUID?) -> Unit,
    onCategoryChanged: (UUID?) -> Unit,
    dismiss: () -> Unit
) {
    var selectedCategoryId by remember(initialCategoryId) {
        mutableStateOf(initialCategoryId)
    }

    IvyModal(
        id = id,
        visible = visible,
        dismiss = dismiss,
        PrimaryAction = {
            ModalSkip {
                save(
                    categoryId = selectedCategoryId,
                    onCategoryChanged = onCategoryChanged,
                    dismiss = dismiss
                )
            }
        }
    ) {
        val view = LocalView.current
        onCompositionStart {
            view.hideKeyboard()
        }

        Spacer(Modifier.height(32.dp))

        ModalTitle(
            text = stringResource(R.string.choose_category)
        )

        Spacer(Modifier.height(24.dp))

        CategoryPicker(
            categories = categories,
            selectedCategoryId = selectedCategoryId,
            showCategoryModal = showCategoryModal,
            onEditCategory = {
                showCategoryModal(it.id)
            }
        ) {
            selectedCategoryId = it?.id
            save(
                shouldDismissModal = it != null,
                categoryId = it?.id,
                onCategoryChanged = onCategoryChanged,
                dismiss = dismiss
            )
        }

        Spacer(Modifier.height(56.dp))
    }
}

private fun save(
    shouldDismissModal: Boolean = true,

    categoryId: UUID?,
    onCategoryChanged: (UUID?) -> Unit,
    dismiss: () -> Unit
) {
    onCategoryChanged(categoryId)
    if (shouldDismissModal) {
        dismiss()
    }
}

@ExperimentalFoundationApi
@Suppress("ParameterNaming")
@Composable
private fun CategoryPicker(
    categories: List<CategoryModalCategory>,
    selectedCategoryId: UUID?,
    showCategoryModal: (UUID?) -> Unit,
    onEditCategory: (CategoryModalCategory) -> Unit,
    onSelected: (CategoryModalCategory?) -> Unit,
) {
    val data = mutableListOf<Any>()
    data.addAll(categories)
    data.add(AddNewCategory())

    WrapContentRow(
        modifier = Modifier
            .padding(horizontal = 16.dp),
        horizontalMarginBetweenItems = 12.dp,
        verticalMarginBetweenRows = 12.dp,
        items = data
    ) {
        when (it) {
            is CategoryModalCategory -> {
                CategoryButton(
                    category = it,
                    selected = it.id == selectedCategoryId,
                    onClick = {
                        onSelected(it)
                    },
                    onLongClick = {
                        onEditCategory(it)
                    },
                    onDeselect = {
                        onSelected(null)
                    }
                )
            }

            is AddNewCategory -> {
                AddNewButton {
                    showCategoryModal(null)
                }
            }
        }
    }
}

@ExperimentalFoundationApi
@Composable
private fun CategoryButton(
    category: CategoryModalCategory,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDeselect: () -> Unit,
) {
    val categoryColor = category.color.toComposeColor()

    val rFull = CategoryModalTheme.shapes.rFull

    Row(
        modifier = Modifier
            .thenIf(selected) {
                drawColoredShadow(categoryColor)
            }
            .clip(CategoryModalTheme.shapes.rFull)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .border(
                width = 2.dp,
                color = if (selected) CategoryModalTheme.colors.pureInverse else CategoryModalTheme.colors.medium,
                shape = CategoryModalTheme.shapes.rFull
            )
            .thenIf(selected) {
                background(categoryColor, rFull)
            }
            .testTag("choose_category_button"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(if (selected) 12.dp else 8.dp))

        ItemIconSDefaultIcon(
            modifier = Modifier
                .background(categoryColor, CircleShape),
            iconName = category.icon,
            defaultIcon = R.drawable.ic_custom_category_s,
            tint = findContrastTextColor(categoryColor)
        )

        Text(
            modifier = Modifier
                .padding(vertical = 12.dp)
                .padding(
                    start = if (selected) 12.dp else 12.dp,
                    end = if (selected) 20.dp else 24.dp
                ),
            text = category.name,
            style = CategoryModalTheme.typo.b2.copy(
                color = if (selected) {
                    findContrastTextColor(categoryColor)
                } else {
                    CategoryModalTheme.colors.pureInverse
                },
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Start
            )
        )

        if (selected) {
            val deselectBtnBackground = findContrastTextColor(categoryColor)
            GradientIconButton(
                modifier = Modifier
                    .size(32.dp),
                icon = R.drawable.ic_remove,
                backgroundGradient = Gradient.solid(deselectBtnBackground),
                tint = findContrastTextColor(deselectBtnBackground)
            ) {
                onDeselect()
            }

            Spacer(Modifier.width(8.dp))
        }
    }
}

@Composable
private fun AddNewButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedPillButton(
        modifier = modifier,
        text = stringResource(R.string.add_new),
        iconStart = R.drawable.ic_plus,
        shape = CategoryModalTheme.shapes.rFull,
        backgroundColor = CategoryModalTheme.colors.pure,
        iconTint = CategoryModalTheme.colors.pureInverse,
        borderColor = CategoryModalTheme.colors.mediumInverse,
        textStyle = CategoryModalTheme.typo.b2.copy(
            color = CategoryModalTheme.colors.pureInverse,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start
        ),
        padding = 10.dp,
        onClick = onClick
    )
}

private class AddNewCategory
