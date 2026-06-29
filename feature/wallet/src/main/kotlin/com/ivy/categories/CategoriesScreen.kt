package com.ivy.categories

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivy.ui.search.SearchInput
import com.ivy.ui.money.balancePrefix
import com.ivy.ui.money.compactBalancePrefix
import com.ivy.data.model.currency.format
import com.ivy.ui.compose.selectEndTextFieldValue
import com.ivy.ui.navigation.TransactionsScreen
import com.ivy.ui.navigation.navigation
import com.ivy.ui.navigation.screenScopedViewModel
import com.ivy.ui.period.LocalPeriodState
import com.ivy.ui.period.TimePeriod
import com.ivy.ui.period.displayShort
import com.ivy.ui.platform.LocalDatePicker
import com.ivy.ui.R
import com.ivy.ui.rememberScrollPositionListState
import com.ivy.ui.theme.colors.Gradient
import com.ivy.ui.theme.colors.IvyGradients
import com.ivy.ui.theme.colors.IvyFixedColors.White
import com.ivy.ui.money.BalanceRow
import com.ivy.ui.icon.ItemIconSDefaultIcon
import com.ivy.ui.modal.ChoosePeriodModal
import com.ivy.ui.modal.ReorderModalSingleType
import com.ivy.ui.compose.FilledIconButton
import com.ivy.ui.compose.GradientButton
import com.ivy.ui.compose.OutlinedPillButton
import com.ivy.ui.compose.ResourceIcon
import com.ivy.ui.theme.colors.findContrastTextColor
import com.ivy.ui.modal.IvyModal
import com.ivy.ui.modal.ModalTitle
import com.ivy.ui.theme.colors.toComposeColor
import com.ivy.ui.money.AmountCurrencyB1
import kotlinx.collections.immutable.ImmutableList
import java.util.UUID

@Composable
fun BoxWithConstraintsScope.CategoriesScreen() {
    val viewModel: CategoriesViewModel = screenScopedViewModel()
    val state = viewModel.uiState()

    UI(
        state = state,
        onEvent = viewModel::onEvent
    )
}

@Composable
private fun BoxWithConstraintsScope.UI(
    state: CategoriesScreenState = CategoriesScreenState(
        compactCategoriesModeEnabled = false,
        showCategorySearchBar = false
    ),
    onEvent: (CategoriesScreenEvent) -> Unit = {}
) {
    val nav = navigation()
    val datePicker = LocalDatePicker.current
    val listState = rememberScrollPositionListState(
        key = "categories_lazy_column"
    )
    var categoryModalVisible by remember { mutableStateOf(false) }
    var periodModal: TimePeriod? by remember { mutableStateOf(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        state = listState
    ) {
        item {
            Spacer(Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(Modifier.width(24.dp))

                MonthSelector(
                    period = state.period,
                    onPrevious = { onEvent(CategoriesScreenEvent.OnPreviousMonth) },
                    onNext = { onEvent(CategoriesScreenEvent.OnNextMonth) },
                    onClick = { periodModal = state.period }
                )

                Spacer(Modifier.weight(1f))

                FilledIconButton(
                    icon = R.drawable.ic_sort_by_alpha_24,
                    backgroundColor = CategoriesTheme.colors.medium,
                    tint = CategoriesTheme.colors.pureInverse,
                    onClick = {
                        onEvent(CategoriesScreenEvent.OnSortOrderModalVisible(visible = true))
                    },
                    clickAreaPadding = 12.dp
                )

                Spacer(modifier = Modifier.width(16.dp))

                FilledIconButton(
                    icon = R.drawable.ic_drag_handle,
                    contentDescription = "reorder",
                    backgroundColor = CategoriesTheme.colors.medium,
                    tint = CategoriesTheme.colors.pureInverse,
                ) {
                    onEvent(CategoriesScreenEvent.OnReorderModalVisible(true))
                }

                Spacer(Modifier.width(24.dp))
            }

            if (state.showCategorySearchBar) {
                Spacer(Modifier.height(16.dp))
                SearchField(onSearch = { onEvent(CategoriesScreenEvent.OnSearchQueryUpdate(it)) })
            }
            Spacer(Modifier.height(16.dp))
        }

        items(state.categories, key = { it.category.id.value }) { categoryData ->
            CategoryCard(
                currency = state.baseCurrency,
                categoryData = categoryData,
                compactModeEnabled = state.compactCategoriesModeEnabled,
                onLongClick = {
                    onEvent(CategoriesScreenEvent.OnReorderModalVisible(true))
                }
            ) {
                nav.navigateTo(
                    TransactionsScreen(
                        accountId = null,
                        categoryId = categoryData.category.id.value
                    )
                )
            }
        }

        item {
            Spacer(Modifier.height(150.dp)) // scroll hack
        }
    }
    CategoriesBottomBar(
        onAddCategory = {
            categoryModalVisible = true
        },
        onClose = {
            nav.back()
        },
    )

    ReorderModalSingleType(
        visible = state.reorderModalVisible,
        initialItems = state.categories,
        itemOrderNum = { it.category.orderNum },
        withNewOrderNum = { item, newOrderNum ->
            item.copy(
                category = item.category.copy(
                    orderNum = newOrderNum
                )
            )
        },
        dismiss = {
            onEvent(CategoriesScreenEvent.OnReorderModalVisible(false))
        },
        onReordered = {
            onEvent(CategoriesScreenEvent.OnReorder(it))
        }
    ) { _, item ->
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 24.dp)
                .padding(vertical = 8.dp),
            text = item.category.name.value,
            style = CategoriesTheme.typo.b1.copy(
                color = item.category.color.value.toComposeColor(),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start
            )
        )
    }

    CreateCategoryModal(
        visible = categoryModalVisible,
        onCreateCategory = {
            onEvent(CategoriesScreenEvent.OnCreateCategory(it))
        },
        dismiss = {
            categoryModalVisible = false
        }
    )

    SortModal(
        initialType = state.sortOrder,
        items = state.sortOrderItems,
        visible = state.sortModalVisible,
        dismiss = {
            onEvent(CategoriesScreenEvent.OnSortOrderModalVisible(visible = false))
        },
        onSortOrderChange = {
            onEvent(CategoriesScreenEvent.OnReorder(state.categories, it))
        }
    )

    ChoosePeriodModal(
        modal = periodModal,
        dismiss = { periodModal = null },
        saveSelectedPeriod = {}, // keep this screen's period independent from the global/home period
        pickDate = { minDate, maxDate, initialDate, onDatePicked ->
            datePicker.pickDate(
                minDate = minDate,
                maxDate = maxDate,
                initialDate = initialDate,
                onDatePicked = onDatePicked
            )
        },
        onPeriodSelected = { onEvent(CategoriesScreenEvent.OnSelectPeriod(it)) }
    )
}

@Composable
private fun MonthSelector(
    period: TimePeriod,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val periodState = LocalPeriodState.current

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        MonthNavArrow(
            icon = R.drawable.ic_back,
            contentDescription = "Previous month",
            onClick = onPrevious
        )

        Spacer(Modifier.width(4.dp))

        OutlinedPillButton(
            text = period.displayShort(periodState.startDayOfMonth),
            iconStart = R.drawable.ic_calendar,
            shape = CategoriesTheme.shapes.rFull,
            backgroundColor = CategoriesTheme.colors.pure,
            iconTint = CategoriesTheme.colors.pureInverse,
            borderColor = CategoriesTheme.colors.medium,
            textStyle = CategoriesTheme.typo.b2.copy(
                fontWeight = FontWeight.Bold,
                color = CategoriesTheme.colors.pureInverse,
                textAlign = TextAlign.Start
            ),
            onClick = onClick
        )

        Spacer(Modifier.width(4.dp))

        MonthNavArrow(
            icon = R.drawable.ic_arrow_right,
            contentDescription = "Next month",
            onClick = onNext
        )
    }
}

@Composable
private fun MonthNavArrow(
    @DrawableRes icon: Int,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        ResourceIcon(
            icon = icon,
            tint = CategoriesTheme.colors.pureInverse,
            contentDescription = contentDescription
        )
    }
}

@Composable
private fun CategoryCard(
    currency: String,
    categoryData: CategoryData,
    compactModeEnabled: Boolean,
    onLongClick: () -> Unit,
    onClick: () -> Unit
) {
    val contrastColor = findContrastTextColor(categoryData.category.color.value.toComposeColor())

    if (!compactModeEnabled) {
        Spacer(Modifier.height(16.dp))
        DefaultCategoryCard(onClick, categoryData, currency)
    } else {
        Spacer(Modifier.height(8.dp))
        CompactCategoryCard(
            categoryData = categoryData,
            contrastColor = contrastColor,
            currency = currency,
            onClick = onClick
        )
    }
}

@Composable
private fun DefaultCategoryCard(
    onClick: () -> Unit,
    categoryData: CategoryData,
    currency: String
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(CategoriesTheme.shapes.r4)
            .border(2.dp, CategoriesTheme.colors.medium, CategoriesTheme.shapes.r4)
            .clickable(
                onClick = onClick
            )
    ) {
        CategoryHeader(
            categoryData = categoryData,
            currency = currency,
            contrastColor = findContrastTextColor(categoryData.category.color.value.toComposeColor())
        )

        Spacer(Modifier.height(12.dp))

        // Emitting content
        AddedSpent(
            currency = currency,
            monthlyIncome = categoryData.monthlyIncome,
            monthlyExpenses = categoryData.monthlyExpenses
        )

        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun CompactCategoryCard(
    categoryData: CategoryData,
    contrastColor: Color,
    currency: String,
    onClick: () -> Unit
) {
    val category = categoryData.category
    val balancePrefixValue = compactBalancePrefix(
        income = categoryData.monthlyIncome,
        expenses = categoryData.monthlyExpenses
    )

    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .border(2.dp, CategoriesTheme.colors.medium, CategoriesTheme.shapes.r4)
            .clickable(
                onClick = onClick
            ),
    ) {
        Row(
            modifier = Modifier
                .padding(all = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(category.color.value.toComposeColor()),
                contentAlignment = Alignment.Center,
            ) {
                ItemIconSDefaultIcon(
                    iconName = category.icon?.id,
                    defaultIcon = R.drawable.ic_custom_account_s,
                    tint = contrastColor
                )
            }

            Row(
                modifier =
                Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth()
                    .fillMaxHeight(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category.name.value,
                    style = CategoriesTheme.typo.b2.copy(
                        color = CategoriesTheme.colors.pureInverse,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Start
                    )
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Format the monthly balance according to the currency format and remove
                    // any '+' or '-' signs that might be included from the prefix to ensure
                    // a clean and consistent representation.
                    val currencyFormatted =
                        categoryData.monthlyBalance.format(currency).replace(Regex("[+-]"), "")

                    Text(
                        text = "$balancePrefixValue$currencyFormatted",
                        style = CategoriesTheme.typo.nB1.copy(
                            color = CategoriesTheme.colors.pureInverse,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Start
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = currency,
                        style = CategoriesTheme.typo.nB2.copy(
                            color = CategoriesTheme.colors.pureInverse,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Start
                        )
                    )
                }
            }
        }
    }
}

@Composable
internal fun AddedSpent(
    monthlyIncome: Double,
    monthlyExpenses: Double,
    currency: String,
    modifier: Modifier = Modifier,
    textColor: Color = CategoriesTheme.colors.pureInverse,
    dividerColor: Color = CategoriesTheme.colors.medium,
    center: Boolean = true,
    dividerSpacer: Dp? = null,

    ) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (center) {
            Spacer(Modifier.weight(1f))
        }

        LabelAmount(
            textColor = textColor,
            label = stringResource(R.string.month_expenses),
            amount = monthlyExpenses,
            currency = currency,
            center = center
        )

        if (center) {
            Spacer(Modifier.weight(1f))
        }

        if (dividerSpacer != null) {
            Spacer(modifier = Modifier.width(dividerSpacer))
        }

        // Divider
        Spacer(
            modifier = Modifier
                .width(2.dp)
                .height(48.dp)
                .background(dividerColor, CategoriesTheme.shapes.rFull)
        )

        if (center) {
            Spacer(Modifier.weight(1f))
        }

        if (dividerSpacer != null) {
            Spacer(modifier = Modifier.width(dividerSpacer))
        }

        LabelAmount(
            textColor = textColor,
            label = stringResource(R.string.month_income),
            amount = monthlyIncome,
            currency = currency,
            center = center
        )

        if (center) {
            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun LabelAmount(
    label: String,
    amount: Double,
    currency: String,
    textColor: Color,
    center: Boolean
) {
    Column(
        horizontalAlignment = if (center) Alignment.CenterHorizontally else Alignment.Start
    ) {
        Text(
            text = label,
            style = CategoriesTheme.typo.c.copy(
                color = textColor,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Start
            )
        )

        Spacer(Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            AmountCurrencyB1(
                textColor = textColor,
                amount = amount,
                currency = currency
            )
        }
    }
}

@Composable
private fun CategoryHeader(
    categoryData: CategoryData,
    currency: String,
    contrastColor: Color,
) {
    val category = categoryData.category
    val balancePrefixValue = balancePrefix(
        income = categoryData.monthlyIncome,
        expenses = categoryData.monthlyExpenses
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(category.color.value.toComposeColor(), CategoriesTheme.shapes.r4Top)
    ) {
        Spacer(Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.width(20.dp))

            ItemIconSDefaultIcon(
                iconName = category.icon?.id,
                defaultIcon = R.drawable.ic_custom_category_s,
                tint = contrastColor
            )

            Spacer(Modifier.width(8.dp))

            Text(
                text = category.name.value,
                style = CategoriesTheme.typo.b1.copy(
                    color = contrastColor,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Start
                )
            )
        }

        Spacer(Modifier.height(4.dp))

        BalanceRow(
            modifier = Modifier.align(Alignment.CenterHorizontally),

            textColor = contrastColor,
            currency = currency,
            balance = categoryData.monthlyBalance,

            balanceFontSize = 30.sp,
            currencyFontSize = 30.sp,

            currencyUpfront = false,
            balanceAmountPrefix = balancePrefixValue
        )

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
internal fun BoxWithConstraintsScope.SortModal(
    items: ImmutableList<SortOrder>,
    visible: Boolean,
    initialType: SortOrder,
    dismiss: () -> Unit,
    onSortOrderChange: (SortOrder) -> Unit,
    title: String = stringResource(R.string.sort_by),
    id: UUID = UUID.randomUUID()
) {
    var sortOrder by remember(initialType) {
        mutableStateOf(initialType)
    }

    val applyChange = {
        onSortOrderChange(sortOrder)
        dismiss()
    }

    IvyModal(
        id = id,
        visible = visible,
        dismiss = dismiss,
        PrimaryAction = {
            SortModalSetButton {
                applyChange()
            }
        },
    ) {
        Spacer(Modifier.height(32.dp))

        ModalTitle(text = title)

        Spacer(Modifier.height(32.dp))

        items.forEach {
            SelectTypeButton(
                text = it.displayName,
                icon = when (it) {
                    SortOrder.DEFAULT -> R.drawable.ic_custom_star_s
                    SortOrder.BALANCE_AMOUNT -> R.drawable.ic_vue_money_coins
                    SortOrder.EXPENSES -> R.drawable.ic_expense
                    SortOrder.ALPHABETICAL -> R.drawable.ic_sort_by_alpha_24
                },
                selected = it == sortOrder
            ) {
                sortOrder = it
                applyChange()
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun SortModalSetButton(onClick: () -> Unit) {
    GradientButton(
        text = stringResource(R.string.set),
        backgroundGradient = IvyGradients.Green,
        disabledBackgroundColor = CategoriesTheme.colors.gray,
        shape = CategoriesTheme.shapes.rFull,
        textStyle = CategoriesTheme.typo.b2.copy(
            color = Color(0xFFFAFAFA),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start
        ),
        iconStart = R.drawable.ic_check,
        iconTint = Color(0xFFFAFAFA),
        onClick = onClick
    )
}

@Composable
private fun SelectTypeButton(
    text: String,
    @DrawableRes icon: Int,
    selected: Boolean,
    selectedGradient: Gradient = IvyGradients.Green,
    textSelectedColor: Color = White,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .height(64.dp)
            .clip(CategoriesTheme.shapes.r4)
            .background(
                brush = if (selected) selectedGradient.asHorizontalBrush() else SolidColor(CategoriesTheme.colors.medium),
                shape = CategoriesTheme.shapes.r4
            )
            .clickable {
                onClick()
            }
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(16.dp))

        val textColor = if (selected) textSelectedColor else CategoriesTheme.colors.pureInverse

        ResourceIcon(
            icon = icon,
            tint = textColor,
            modifier = Modifier.fillMaxHeight()
        )

        Spacer(Modifier.width(12.dp))

        Text(
            modifier = Modifier.wrapContentHeight(),
            text = text,
            style = CategoriesTheme.typo.b1.copy(
                color = textColor,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start
            ),
            textAlign = TextAlign.Center,
        )

        if (selected) {
            Spacer(Modifier.weight(1f))

            ResourceIcon(
                icon = R.drawable.ic_check,
                tint = textSelectedColor
            )

            Text(
                text = stringResource(R.string.selected_text),
                style = CategoriesTheme.typo.b2.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = textSelectedColor,
                    textAlign = TextAlign.Start
                )
            )

            Spacer(Modifier.width(24.dp))
        }
    }
}

@Composable
private fun SearchField(
    onSearch: (String) -> Unit,
) {
    var searchQueryTextFieldValue by remember {
        mutableStateOf(selectEndTextFieldValue(""))
    }

    SearchInput(
        searchQueryTextFieldValue = searchQueryTextFieldValue,
        hint = "Search categories",
        focus = false,
        showClearIcon = searchQueryTextFieldValue.text.isNotEmpty(),
        onSetSearchQueryTextField = {
            searchQueryTextFieldValue = it
            onSearch(it.text)
        }
    )
}
