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
import com.ivy.data.model.legacy.Account
import com.ivy.data.model.legacy.PlannedPaymentRule
import com.ivy.base.coroutines.ioThread
import com.ivy.domain.usecase.account.GetLegacyAccountsUseCase
import com.ivy.legacy.domain.logic.PlannedPaymentsLogic
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import javax.inject.Inject

@Stable
@HiltViewModel
class PlannedPaymentsViewModel @Inject constructor(
    private val getBaseCurrencyCode: GetBaseCurrencyCodeUseCase,
    private val plannedPaymentsLogic: PlannedPaymentsLogic,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getLegacyAccountsUseCase: GetLegacyAccountsUseCase
) : ComposeViewModel<PlannedPaymentsScreenState, PlannedPaymentsScreenEvent>() {

    private var currency by mutableStateOf("")
    private var categories by mutableStateOf<ImmutableList<Category>>(persistentListOf())
    private var accounts by mutableStateOf<ImmutableList<Account>>(persistentListOf())
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
    private fun getCategories(): ImmutableList<Category> {
        return categories
    }

    @Composable
    private fun getAccounts(): ImmutableList<Account> {
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

            categories = getCategoriesUseCase().toImmutableList()
            accounts = getLegacyAccountsUseCase()

            oneTimePlannedPayment =
                ioThread { plannedPaymentsLogic.oneTime() }.toImmutableList()
            oneTimeIncome = ioThread { plannedPaymentsLogic.oneTimeIncome() }
            oneTimeExpenses = ioThread { plannedPaymentsLogic.oneTimeExpenses() }

            recurringPlannedPayment =
                ioThread { plannedPaymentsLogic.recurring() }.toImmutableList()
            recurringIncome = ioThread { plannedPaymentsLogic.recurringIncome() }
            recurringExpenses = ioThread { plannedPaymentsLogic.recurringExpenses() }
        }
    }
}
