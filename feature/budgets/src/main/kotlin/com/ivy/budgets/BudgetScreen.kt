package com.ivy.budgets

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ivy.budgets.model.DisplayBudget
import com.ivy.data.model.Budget
import com.ivy.ui.time.LocalTimeFormatter
import com.ivy.ui.period.toDisplay
import com.ivy.ui.compose.clickableNoIndication
import com.ivy.data.model.currency.format
import com.ivy.ui.compose.rememberInteractionSource
import com.ivy.ui.navigation.navigation
import com.ivy.ui.navigation.screenScopedViewModel
import com.ivy.ui.R
import com.ivy.ui.modal.ReorderModalSingleType
import com.ivy.ui.money.AmountCurrencyB1
import com.ivy.ui.compose.FilledIconButton
import com.ivy.ui.compose.ResourceIcon

@Composable
fun BoxWithConstraintsScope.BudgetScreen() {
    val viewModel: BudgetViewModel = screenScopedViewModel()
    val uiState = viewModel.uiState()

    UI(
        state = uiState,
        onEvent = viewModel::onEvent
    )
}

@Composable
private fun BoxWithConstraintsScope.UI(
    state: BudgetScreenState,
    onEvent: (BudgetScreenEvent) -> Unit = {}
) {
    var budgetModalVisible by remember { mutableStateOf(false) }
    var budgetModalBudget: Budget? by remember { mutableStateOf(null) }
    var budgetModalAutoFocus by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .verticalScroll(rememberScrollState()),
    ) {
        Spacer(Modifier.height(32.dp))

        Toolbar(
            timeRange = state.timeRange,
            totalRemainingBudgetText = state.totalRemainingBudgetText,
            baseCurrency = state.baseCurrency,
            appBudgetMax = state.appBudgetMax,
            categoryBudgetsTotal = state.categoryBudgetsTotal,
            setReorderModalVisible = {
                onEvent(BudgetScreenEvent.OnReorderModalVisible(it))
            }
        )

        Spacer(Modifier.height(8.dp))

        for (item in state.budgets) {
            Spacer(Modifier.height(24.dp))

            BudgetItem(
                displayBudget = item,
                baseCurrency = state.baseCurrency
            ) {
                budgetModalBudget = item.budget
                budgetModalAutoFocus = false
                budgetModalVisible = true
            }
        }

        if (state.budgets.isEmpty()) {
            Spacer(Modifier.weight(1f))

            NoBudgetsEmptyState(
                emptyStateTitle = stringResource(R.string.no_budgets),
                emptyStateText = stringResource(R.string.no_budgets_text)
            )

            Spacer(Modifier.weight(1f))
        }

        Spacer(Modifier.height(150.dp)) // scroll hack
    }

    val nav = navigation()
    BudgetBottomBar(
        onAdd = {
            budgetModalBudget = null
            budgetModalAutoFocus = true
            budgetModalVisible = true
        },
        onClose = {
            nav.back()
        },
    )

    ReorderModalSingleType(
        visible = state.reorderModalVisible,
        initialItems = state.budgets,
        itemOrderNum = { it.budget.orderId },
        withNewOrderNum = { item, newOrderNum ->
            item.copy(
                budget = item.budget.copy(
                    orderId = newOrderNum
                )
            )
        },
        dismiss = {
            onEvent(BudgetScreenEvent.OnReorderModalVisible(false))
        },
        onReordered = { onEvent(BudgetScreenEvent.OnReorder(it.map { item -> item.budget.id })) }
    ) { _, item ->
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 24.dp)
                .padding(vertical = 8.dp),
            text = item.budget.name,
            style = BudgetsTheme.typo.b1.copy(
                color = BudgetsTheme.colors.pureInverse,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start
            )
        )
    }

    BudgetModal(
        visible = budgetModalVisible,
        budget = budgetModalBudget,
        baseCurrency = state.baseCurrency,
        categories = state.categories,
        autoFocusKeyboard = budgetModalAutoFocus,
        onCreate = { onEvent(BudgetScreenEvent.OnCreateBudget(it)) },
        onEdit = { onEvent(BudgetScreenEvent.OnEditBudget(it)) },
        onDelete = { onEvent(BudgetScreenEvent.OnDeleteBudget(it.id)) },
        dismiss = {
            budgetModalVisible = false
        }
    )
}

@Composable
private fun Toolbar(
    timeRange: com.ivy.data.model.FromToTimeRange?,
    totalRemainingBudgetText: String?,
    baseCurrency: String,
    appBudgetMax: Double,
    categoryBudgetsTotal: Double,
    setReorderModalVisible: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 24.dp, end = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.budgets),
                style = BudgetsTheme.typo.h2.copy(
                    color = BudgetsTheme.colors.pureInverse,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Start
                )
            )

            if (timeRange != null) {
                Spacer(Modifier.height(4.dp))

                Text(
                    text = timeRange.toDisplay(LocalTimeFormatter.current),
                    style = BudgetsTheme.typo.b2.copy(
                        color = BudgetsTheme.colors.pureInverse,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Start
                    )
                )
            }

            if (categoryBudgetsTotal > 0 || appBudgetMax > 0) {
                Spacer(Modifier.height(4.dp))

                val categoryBudgetText = if (categoryBudgetsTotal > 0) {
                    stringResource(
                        R.string.for_categories,
                        categoryBudgetsTotal.format(baseCurrency),
                        baseCurrency
                    )
                } else {
                    ""
                }

                val appBudgetMaxText = if (appBudgetMax > 0) {
                    stringResource(
                        R.string.app_budget,
                        appBudgetMax.format(baseCurrency),
                        baseCurrency
                    )
                } else {
                    ""
                }

                val hasBothBudgetTypes =
                    categoryBudgetText.isNotBlank() && appBudgetMaxText.isNotBlank()
                Text(
                    modifier = Modifier.testTag("budgets_info_text"),
                    text = if (hasBothBudgetTypes) {
                        stringResource(
                            R.string.budget_info_both,
                            categoryBudgetText,
                            appBudgetMaxText
                        )
                    } else {
                        stringResource(R.string.budget_info, categoryBudgetText, appBudgetMaxText)
                    },
                    style = BudgetsTheme.typo.nC.copy(
                        color = BudgetsTheme.colors.gray,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Start
                    )
                )

                if (totalRemainingBudgetText != null) {
                    Text(
                        text = totalRemainingBudgetText,
                        style = BudgetsTheme.typo.nC.copy(
                            color = BudgetsTheme.colors.gray,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Start
                        )
                    )
                }
            }
        }

        FilledIconButton(
            icon = R.drawable.ic_drag_handle,
            contentDescription = "reorder",
            backgroundColor = BudgetsTheme.colors.medium,
            tint = BudgetsTheme.colors.pureInverse,
        ) {
            setReorderModalVisible(true)
        }

        Spacer(Modifier.width(24.dp))
    }
}

@SuppressLint("ComposeContentEmitterReturningValues", "ComposeMultipleContentEmitters")
@Composable
private fun BudgetItem(
    displayBudget: DisplayBudget,
    baseCurrency: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickableNoIndication(rememberInteractionSource()) {
                onClick()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = displayBudget.budget.name,
                style = BudgetsTheme.typo.b1.copy(
                    color = BudgetsTheme.colors.pureInverse,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Start
                )
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = determineBudgetType(displayBudget.budget.parseCategoryIds().size),
                style = BudgetsTheme.typo.c.copy(
                    color = BudgetsTheme.colors.gray,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start
                )
            )
        }

        AmountCurrencyB1(
            amount = displayBudget.budget.amount,
            currency = baseCurrency,
            amountFontWeight = FontWeight.ExtraBold
        )

        Spacer(Modifier.width(32.dp))
    }

    Spacer(Modifier.height(12.dp))

    BudgetBattery(
        modifier = Modifier.padding(horizontal = 16.dp),
        currency = baseCurrency,
        expenses = displayBudget.spentAmount,
        budget = displayBudget.budget.amount,
        backgroundNotFilled = BudgetsTheme.colors.medium
    ) {
        onClick()
    }
}

@Composable
private fun NoBudgetsEmptyState(
    emptyStateTitle: String,
    emptyStateText: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))

        ResourceIcon(
            icon = R.drawable.ic_budget_xl,
            tint = BudgetsTheme.colors.gray
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = emptyStateTitle,
            style = BudgetsTheme.typo.b1.copy(
                color = BudgetsTheme.colors.gray,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Start
            )
        )

        Spacer(Modifier.height(8.dp))

        Text(
            modifier = Modifier.padding(horizontal = 32.dp),
            text = emptyStateText,
            style = BudgetsTheme.typo.b2.copy(
                color = BudgetsTheme.colors.gray,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        )

        Spacer(Modifier.height(96.dp))
    }
}
