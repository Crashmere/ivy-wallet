package com.ivy.planned.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ivy.legacy.ui.theme.LegacyTheme
import com.ivy.legacy.ui.theme.style
import com.ivy.data.model.PlannedPaymentRule
import com.ivy.ui.R
import com.ivy.legacy.ui.icon.IvyIcon
import kotlinx.collections.immutable.ImmutableList
import java.util.UUID
import kotlin.math.absoluteValue

@Suppress("LongParameterList")
@Composable
internal fun PlannedPaymentsLazyColumn(
    Header: @Composable () -> Unit,
    currency: String,
    categories: ImmutableList<PlannedPaymentCategory>,
    accounts: ImmutableList<PlannedPaymentAccount>,
    oneTime: ImmutableList<PlannedPaymentRule>,
    oneTimeIncome: Double,
    oneTimeExpenses: Double,
    recurring: ImmutableList<PlannedPaymentRule>,
    recurringIncome: Double,
    recurringExpenses: Double,
    oneTimeExpanded: Boolean,
    recurringExpanded: Boolean,
    setOneTimeExpanded: (Boolean) -> Unit,
    setRecurringExpanded: (Boolean) -> Unit,
    onPlannedPaymentClick: (PlannedPaymentRule) -> Unit,
    onCategoryClick: (UUID) -> Unit,
    onAccountClick: (UUID) -> Unit,
    listState: LazyListState = rememberLazyListState(),
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        state = listState
    ) {
        item {
            Header()
        }

        plannedPaymentItems(
            currency = currency,
            categories = categories,
            accounts = accounts,
            onPlannedPaymentClick = onPlannedPaymentClick,
            onCategoryClick = onCategoryClick,
            onAccountClick = onAccountClick,

            oneTime = oneTime,
            oneTimeIncome = oneTimeIncome,
            oneTimeExpenses = oneTimeExpenses,
            oneTimeExpanded = oneTimeExpanded,
            setOneTimeExpanded = setOneTimeExpanded,

            recurring = recurring,
            recurringIncome = recurringIncome,
            recurringExpenses = recurringExpenses,
            recurringExpanded = recurringExpanded,
            setRecurringExpanded = setRecurringExpanded
        )
    }
}

@Suppress("LongParameterList")
private fun LazyListScope.plannedPaymentItems(
    currency: String,
    categories: ImmutableList<PlannedPaymentCategory>,
    accounts: ImmutableList<PlannedPaymentAccount>,
    onPlannedPaymentClick: (PlannedPaymentRule) -> Unit,
    onCategoryClick: (UUID) -> Unit,
    onAccountClick: (UUID) -> Unit,

    oneTime: ImmutableList<PlannedPaymentRule>,
    oneTimeIncome: Double,
    oneTimeExpenses: Double,
    oneTimeExpanded: Boolean,
    setOneTimeExpanded: (Boolean) -> Unit,

    recurring: ImmutableList<PlannedPaymentRule>,
    recurringIncome: Double,
    recurringExpenses: Double,
    recurringExpanded: Boolean,
    setRecurringExpanded: (Boolean) -> Unit,
) {
    if (oneTime.isNotEmpty()) {
        item {
            PlannedPaymentSectionDivider(
                expanded = oneTimeExpanded,
                setExpanded = setOneTimeExpanded,
                title = stringResource(R.string.one_time_payments),
                titleColor = LegacyTheme.colors.pureInverse,
                baseCurrency = currency,
                income = oneTimeIncome,
                expenses = oneTimeExpenses.absoluteValue
            )
        }

        if (oneTimeExpanded) {
            itemsIndexed(oneTime) { _, item ->
                PlannedPaymentCard(
                    baseCurrency = currency,
                    categories = categories,
                    accounts = accounts,
                    plannedPayment = item,
                    onClick = onPlannedPaymentClick,
                    onCategoryClick = onCategoryClick,
                    onAccountClick = onAccountClick
                )
            }
        }
    }

    if (recurring.isNotEmpty()) {
        item {
            PlannedPaymentSectionDivider(
                expanded = recurringExpanded,
                setExpanded = setRecurringExpanded,
                title = stringResource(R.string.recurring_payments),
                titleColor = LegacyTheme.colors.pureInverse,
                baseCurrency = currency,
                income = recurringIncome,
                expenses = recurringExpenses.absoluteValue
            )
        }

        if (recurringExpanded) {
            itemsIndexed(recurring) { _, item ->
                PlannedPaymentCard(
                    baseCurrency = currency,
                    categories = categories,
                    accounts = accounts,
                    plannedPayment = item,
                    onClick = onPlannedPaymentClick,
                    onCategoryClick = onCategoryClick,
                    onAccountClick = onAccountClick
                )
            }
        }
    }

    if (oneTime.isEmpty() && recurring.isEmpty()) {
        item {
            NoPlannedPaymentsEmptyState()
        }
    }

    item {
        // last spacer - scroll hack
        Spacer(Modifier.height(150.dp))
    }
}

@Composable
private fun LazyItemScope.NoPlannedPaymentsEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(64.dp))

        IvyIcon(
            icon = R.drawable.ic_planned_payments,
            tint = LegacyTheme.colors.gray
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.no_planned_payments),
            style = LegacyTheme.typo.b1.style(
                color = LegacyTheme.colors.gray,
                fontWeight = FontWeight.ExtraBold
            ),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.no_planned_payments_description),
            style = LegacyTheme.typo.b2.style(
                color = LegacyTheme.colors.gray,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        )
    }
}
