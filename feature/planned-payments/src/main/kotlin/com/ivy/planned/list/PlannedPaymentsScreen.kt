package com.ivy.planned.list

import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ivy.data.model.PlannedPaymentRule
import com.ivy.data.model.TransactionType
import com.ivy.legacy.ui.theme.LegacyTheme
import com.ivy.ui.navigation.EditPlannedScreen
import com.ivy.ui.navigation.TransactionRouteType
import com.ivy.ui.navigation.TransactionsScreen
import com.ivy.ui.navigation.navigation
import com.ivy.ui.navigation.screenScopedViewModel
import com.ivy.ui.R
import com.ivy.ui.rememberScrollPositionListState

@Composable
fun BoxWithConstraintsScope.PlannedPaymentsScreen() {
    val viewModel: PlannedPaymentsViewModel = screenScopedViewModel()
    val uiState = viewModel.uiState()

    UI(
        state = uiState,
        onEvent = viewModel::onEvent
    )
}

@Composable
private fun BoxWithConstraintsScope.UI(
    state: PlannedPaymentsScreenState,
    onEvent: (PlannedPaymentsScreenEvent) -> Unit = {}
) {
    val nav = navigation()

    PlannedPaymentsLazyColumn(
        Header = {
            Spacer(Modifier.height(32.dp))

            Text(
                modifier = Modifier.padding(start = 24.dp),
                text = stringResource(R.string.planned_payments_inline),
                style = LegacyTheme.typo.h2.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = LegacyTheme.colors.pureInverse,
                    textAlign = TextAlign.Start
                )
            )

            Spacer(Modifier.height(24.dp))
        },
        currency = state.currency,
        categories = state.categories,
        accounts = state.accounts,
        oneTime = state.oneTimePlannedPayment,
        oneTimeIncome = state.oneTimeIncome,
        oneTimeExpenses = state.oneTimeExpenses,
        recurring = state.recurringPlannedPayment,
        recurringIncome = state.recurringIncome,
        recurringExpenses = state.recurringExpenses,
        oneTimeExpanded = state.isOneTimePaymentsExpanded,
        recurringExpanded = state.isRecurringPaymentsExpanded,
        setOneTimeExpanded = {
            onEvent(PlannedPaymentsScreenEvent.OnOneTimePaymentsExpanded(it))
        },
        setRecurringExpanded = {
            onEvent(PlannedPaymentsScreenEvent.OnRecurringPaymentsExpanded(it))
        },
        onPlannedPaymentClick = { rule ->
            nav.navigateTo(rule.toEditPlannedScreen())
        },
        onCategoryClick = { categoryId ->
            nav.navigateTo(
                TransactionsScreen(
                    accountId = null,
                    categoryId = categoryId
                )
            )
        },
        onAccountClick = { accountId ->
            nav.navigateTo(
                TransactionsScreen(
                    accountId = accountId,
                    categoryId = null
                )
            )
        },
        listState = rememberScrollPositionListState(key = "plannedPayments")
    )

    PlannedPaymentsBottomBar(
        onClose = {
            nav.back()
        },
        onAdd = {
            nav.navigateTo(
                EditPlannedScreen(
                    type = TransactionRouteType.EXPENSE,
                    plannedPaymentRuleId = null
                )
            )
        }
    )
}

private fun PlannedPaymentRule.toEditPlannedScreen(): EditPlannedScreen {
    return EditPlannedScreen(
        plannedPaymentRuleId = id,
        type = type.toRouteType()
    )
}

private fun TransactionType.toRouteType(): TransactionRouteType {
    return TransactionRouteType.valueOf(name)
}
