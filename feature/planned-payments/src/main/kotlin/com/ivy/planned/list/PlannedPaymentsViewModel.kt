package com.ivy.planned.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.ivy.ui.ComposeViewModel
import com.ivy.data.model.Category
import com.ivy.domain.usecase.category.GetCategoriesUseCase
import com.ivy.domain.usecase.currency.GetBaseCurrencyCodeUseCase
import com.ivy.data.model.legacy.LegacyAccount
import com.ivy.data.model.PlannedPaymentRule
import com.ivy.domain.usecase.account.GetLegacyAccountsUseCase
import com.ivy.domain.usecase.planned.GetPlannedPaymentsOverviewUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import javax.inject.Inject

@Stable
@HiltViewModel
internal class PlannedPaymentsViewModel @Inject internal constructor(
    private val getBaseCurrencyCode: GetBaseCurrencyCodeUseCase,
    private val getPlannedPaymentsOverviewUseCase: GetPlannedPaymentsOverviewUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getLegacyAccountsUseCase: GetLegacyAccountsUseCase
) : ComposeViewModel<PlannedPaymentsScreenState, PlannedPaymentsScreenEvent>() {

    private var currency by mutableStateOf("")
    private var categories by mutableStateOf<ImmutableList<PlannedPaymentCategory>>(persistentListOf())
    private var accounts by mutableStateOf<ImmutableList<PlannedPaymentAccount>>(persistentListOf())
    private var oneTimePlannedPayment by
        mutableStateOf<ImmutableList<PlannedPaymentRule>>(persistentListOf())
    private var recurringPlannedPayment by
        mutableStateOf<ImmutableList<PlannedPaymentRule>>(persistentListOf())
    private var oneTimeIncome by mutableDoubleStateOf(0.0)
    private var oneTimeExpenses by mutableDoubleStateOf(0.0)
    private var recurringIncome by mutableDoubleStateOf(0.0)
    private var recurringExpenses by mutableDoubleStateOf(0.0)
    private var isOneTimePaymentsExpanded by mutableStateOf(true)
    private var isRecurringPaymentsExpanded by mutableStateOf(true)

    @Composable
    override fun uiState(): PlannedPaymentsScreenState {
        LaunchedEffect(Unit) {
            start()
        }

        return PlannedPaymentsScreenState(
            currency = getCurrency(),
            categories = getCategories(),
            accounts = getAccounts(),
            oneTimeIncome = getOneTimeIncome(),
            oneTimeExpenses = getOneTimeExpenses(),
            recurringExpenses = getRecurringExpenses(),
            recurringIncome = getRecurringIncome(),
            recurringPlannedPayment = getRecurringPlannedPayment(),
            oneTimePlannedPayment = getOneTimePlannedPayment(),
            isOneTimePaymentsExpanded = getOneTimePaymentsExpanded(),
            isRecurringPaymentsExpanded = getRecurringPaymentsExpanded()
        )
    }

    @Composable
    private fun getCurrency(): String {
        return currency
    }

    @Composable
    private fun getCategories(): ImmutableList<PlannedPaymentCategory> {
        return categories
    }

    @Composable
    private fun getAccounts(): ImmutableList<PlannedPaymentAccount> {
        return accounts
    }

    @Composable
    private fun getOneTimePlannedPayment(): ImmutableList<PlannedPaymentRule> {
        return oneTimePlannedPayment
    }

    @Composable
    private fun getRecurringPlannedPayment(): ImmutableList<PlannedPaymentRule> {
        return recurringPlannedPayment
    }

    @Composable
    private fun getOneTimeExpenses(): Double {
        return oneTimeExpenses
    }

    @Composable
    private fun getOneTimeIncome(): Double {
        return oneTimeIncome
    }

    @Composable
    private fun getRecurringExpenses(): Double {
        return recurringExpenses
    }

    @Composable
    private fun getRecurringIncome(): Double {
        return recurringIncome
    }

    @Composable
    private fun getRecurringPaymentsExpanded(): Boolean {
        return isRecurringPaymentsExpanded
    }

    @Composable
    private fun getOneTimePaymentsExpanded(): Boolean {
        return isOneTimePaymentsExpanded
    }

    override fun onEvent(event: PlannedPaymentsScreenEvent) {
        when (event) {
            is PlannedPaymentsScreenEvent.OnOneTimePaymentsExpanded -> {
                isOneTimePaymentsExpanded = event.isExpanded
            }
            is PlannedPaymentsScreenEvent.OnRecurringPaymentsExpanded -> {
                isRecurringPaymentsExpanded = event.isExpanded
            }
        }
    }

    private fun start() {
        viewModelScope.launch {
            currency = getBaseCurrencyCode()

            categories = getCategoriesUseCase()
                .map(Category::toPlannedPaymentCategory)
                .toImmutableList()
            accounts = getLegacyAccountsUseCase()
                .map(LegacyAccount::toPlannedPaymentAccount)
                .toImmutableList()

            val overview = getPlannedPaymentsOverviewUseCase()
            oneTimePlannedPayment = overview.oneTime.toImmutableList()
            oneTimeIncome = overview.oneTimeIncome
            oneTimeExpenses = overview.oneTimeExpenses
            recurringPlannedPayment = overview.recurring.toImmutableList()
            recurringIncome = overview.recurringIncome
            recurringExpenses = overview.recurringExpenses
        }
    }
}

private fun Category.toPlannedPaymentCategory() = PlannedPaymentCategory(
    id = id.value,
    name = name.value,
    color = color.value,
    icon = icon?.id,
)

private fun LegacyAccount.toPlannedPaymentAccount() = PlannedPaymentAccount(
    id = id,
    name = name,
    icon = icon,
    currency = currency,
)
