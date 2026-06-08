package com.ivy.budgets

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewModelScope
import com.ivy.budgets.model.DisplayBudget
import com.ivy.data.model.Category
import com.ivy.data.model.Expense
import com.ivy.data.model.Income
import com.ivy.data.model.Transaction
import com.ivy.data.model.Transfer
import com.ivy.data.model.getFromAccount
import com.ivy.ui.period.PeriodState
import com.ivy.data.model.FromToTimeRange
import com.ivy.data.model.toCloseTimeRange
import com.ivy.data.model.legacy.LegacyAccount
import com.ivy.data.model.Budget
import com.ivy.data.model.currency.format
import com.ivy.domain.usecase.budget.CreateBudgetUseCase
import com.ivy.domain.usecase.budget.DeleteBudgetUseCase
import com.ivy.domain.usecase.budget.GetBudgetsUseCase
import com.ivy.domain.usecase.budget.ReorderBudgetsUseCase
import com.ivy.domain.usecase.budget.UpdateBudgetUseCase
import com.ivy.domain.usecase.category.GetCategoriesUseCase
import com.ivy.domain.usecase.currency.GetBaseCurrencyCodeUseCase
import com.ivy.domain.usecase.exchange.ExchangeTransactionAmountUseCase
import com.ivy.domain.usecase.transaction.GetTransactionsBetweenUseCase
import com.ivy.ui.ComposeViewModel
import com.ivy.ui.R
import com.ivy.domain.usecase.account.GetLegacyAccountsUseCase
import com.ivy.data.model.CreateBudgetData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.abs

@Stable
@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val reorderBudgetsUseCase: ReorderBudgetsUseCase,
    private val createBudgetUseCase: CreateBudgetUseCase,
    private val updateBudgetUseCase: UpdateBudgetUseCase,
    private val deleteBudgetUseCase: DeleteBudgetUseCase,
    private val periodState: PeriodState,
    private val getLegacyAccountsUseCase: GetLegacyAccountsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getBudgetsUseCase: GetBudgetsUseCase,
    private val getBaseCurrencyCode: GetBaseCurrencyCodeUseCase,
    private val getTransactionsBetweenUseCase: GetTransactionsBetweenUseCase,
    private val exchangeTransactionAmountUseCase: ExchangeTransactionAmountUseCase,
) : ComposeViewModel<BudgetScreenState, BudgetScreenEvent>() {

    private val baseCurrency = mutableStateOf("")
    private val timeRange = mutableStateOf<FromToTimeRange?>(null)
    private val budgets = mutableStateOf<ImmutableList<DisplayBudget>>(persistentListOf())
    private val categories = mutableStateOf<ImmutableList<Category>>(persistentListOf())
    private val accounts = mutableStateOf<ImmutableList<LegacyAccount>>(persistentListOf())
    private val categoryBudgetsTotal = mutableDoubleStateOf(0.0)
    private val appBudgetMax = mutableDoubleStateOf(0.0)
    private val totalRemainingBudget = mutableDoubleStateOf(0.0)
    private val reorderModalVisible = mutableStateOf(false)
    private val budgetModalData = mutableStateOf<BudgetModalData?>(null)

    @Composable
    override fun uiState(): BudgetScreenState {
        LaunchedEffect(Unit) {
            start()
        }

        return BudgetScreenState(
            baseCurrency = getBaseCurrency(),
            categories = getCategories(),
            accounts = getAccounts(),
            budgets = getBudgets(),
            categoryBudgetsTotal = getCategoryBudgetsTotal(),
            appBudgetMax = getAppBudgetMax(),
            totalRemainingBudgetText = getTotalRemainingBudgetText(),
            timeRange = getTimeRange(),
            reorderModalVisible = getReorderModalVisible(),
            budgetModalData = getBudgetModalData()
        )
    }

    @Composable
    private fun getBaseCurrency(): String {
        return baseCurrency.value
    }

    @Composable
    private fun getTimeRange(): FromToTimeRange? {
        return timeRange.value
    }

    @Composable
    private fun getCategories(): ImmutableList<Category> {
        return categories.value
    }

    @Composable
    private fun getAccounts(): ImmutableList<LegacyAccount> {
        return accounts.value
    }

    @Composable
    private fun getBudgets(): ImmutableList<DisplayBudget> {
        return budgets.value
    }

    @Composable
    private fun getReorderModalVisible(): Boolean {
        return reorderModalVisible.value
    }

    @Composable
    private fun getCategoryBudgetsTotal(): Double {
        return categoryBudgetsTotal.doubleValue
    }

    @Composable
    private fun getAppBudgetMax(): Double {
        return appBudgetMax.doubleValue
    }

    @Composable
    private fun getTotalRemainingBudgetText(): String? {
        val budgetExceeded = totalRemainingBudget.doubleValue < 0
        return when {
            categoryBudgetsTotal.doubleValue > 0 -> stringResource(
                if (budgetExceeded) R.string.budget_exceeded_info else R.string.total_budget_info,
                abs(totalRemainingBudget.doubleValue).format(baseCurrency.value),
                baseCurrency.value
            )

            else -> null
        }
    }

    @Composable
    private fun getBudgetModalData(): BudgetModalData? {
        return budgetModalData.value
    }

    override fun onEvent(event: BudgetScreenEvent) {
        when (event) {
            is BudgetScreenEvent.OnCreateBudget -> {
                createBudget(event.budgetData)
            }

            is BudgetScreenEvent.OnEditBudget -> {
                editBudget(event.budget)
            }

            is BudgetScreenEvent.OnDeleteBudget -> {
                deleteBudget(event.budget)
            }

            is BudgetScreenEvent.OnReorder -> {
                reorder(event.newOrder)
            }

            is BudgetScreenEvent.OnReorderModalVisible -> {
                reorderModalVisible.value = event.visible
            }

            is BudgetScreenEvent.OnBudgetModalData -> {
                budgetModalData.value = event.budgetModalData
            }
        }
    }

    private fun start() {
        viewModelScope.launch {
            categories.value = getCategoriesUseCase().toImmutableList()
            val accounts = getLegacyAccountsUseCase()
            val baseCurrency = getBaseCurrencyCode()
            val timeRange = periodState.rangeOf(periodState.currentMonth())
            val budgets = getBudgetsUseCase()

            appBudgetMax.doubleValue = budgets
                .filter { it.categoryIdsSerialized.isNullOrBlank() }
                .maxOfOrNull { it.amount } ?: 0.0

            categoryBudgetsTotal.doubleValue = budgets
                .filter { it.categoryIdsSerialized.isNullOrBlank().not() }
                .sumOf { it.amount }

            this@BudgetViewModel.budgets.value = withContext(Dispatchers.IO) {
                budgets.map {
                    DisplayBudget(
                        budget = it,
                        spentAmount = calculateSpentAmount(
                            budget = it,
                            transactions = getTransactionsBetweenUseCase(timeRange.toCloseTimeRange()),
                            accounts = accounts,
                            baseCurrencyCode = baseCurrency
                        )
                    )
                }.toImmutableList()
            }
            totalRemainingBudget.doubleValue = calculateTotalRemainingBudget(
                budgets = this@BudgetViewModel.budgets.value,
                categoryBudgetsTotal = categoryBudgetsTotal.doubleValue
            )
            this@BudgetViewModel.accounts.value = accounts
            this@BudgetViewModel.baseCurrency.value = baseCurrency
            this@BudgetViewModel.timeRange.value = timeRange
        }
    }

    private suspend fun calculateSpentAmount(
        budget: Budget,
        transactions: List<Transaction>,
        baseCurrencyCode: String,
        accounts: List<LegacyAccount>
    ): Double {
        val accountsFilter = budget.parseAccountIds()
        val categoryFilter = budget.parseCategoryIds()

        var spentAmount = 0.0
        for (transaction in transactions
            .filter { accountsFilter.isEmpty() || accountsFilter.contains(it.getFromAccount().value) }
            .filter { categoryFilter.isEmpty() || categoryFilter.contains(it.category?.value) }) {
            spentAmount += when (transaction) {
                    is Income -> {
                        0.0 // ignore income
                    }

                    is Expense -> {
                        // increment spent amount
                        exchangeTransactionAmountUseCase(
                            transaction = transaction,
                            accounts = accounts,
                            baseCurrency = baseCurrencyCode
                        ).toDouble()
                    }

                    is Transfer -> {
                        // ignore transfers for simplicity
                        0.0
                    }
                }
        }
        return spentAmount
    }

    private fun createBudget(data: CreateBudgetData) {
        viewModelScope.launch {
            if (createBudgetUseCase(data) != null) {
                start()
            }
        }
    }

    private fun editBudget(budget: Budget) {
        viewModelScope.launch {
            if (updateBudgetUseCase(budget)) {
                start()
            }
        }
    }

    private fun deleteBudget(budget: Budget) {
        viewModelScope.launch {
            if (deleteBudgetUseCase(budget)) {
                start()
            }
        }
    }

    private fun reorder(newOrder: List<DisplayBudget>) {
        viewModelScope.launch {
            reorderBudgetsUseCase(newOrder.map(DisplayBudget::budget))
            start()
        }
    }
}

fun calculateTotalRemainingBudget(
    budgets: ImmutableList<DisplayBudget>,
    categoryBudgetsTotal: Double
): Double {
    return categoryBudgetsTotal - budgets
        .filter { it.budget.categoryIdsSerialized.isNullOrBlank().not() }
        .sumOf { it.spentAmount }
}
