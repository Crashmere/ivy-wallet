package com.ivy.piechart

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.ivy.data.model.TransactionType
import com.ivy.domain.usecase.currency.GetBaseCurrencyCodeUseCase
import com.ivy.domain.usecase.settings.GetTransfersAsIncomeExpensePreferenceUseCase
import com.ivy.domain.usecase.transaction.GetTransactionsByIdsUseCase
import com.ivy.ui.period.PeriodState
import com.ivy.ui.period.TimePeriod
import com.ivy.ui.ComposeViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

@Stable
@HiltViewModel
internal class PieChartStatisticViewModel @Inject internal constructor(
    private val getBaseCurrencyCode: GetBaseCurrencyCodeUseCase,
    private val periodState: PeriodState,
    private val buildPieChartDataUseCase: BuildPieChartDataUseCase,
    private val getTransfersAsIncomeExpensePreference: GetTransfersAsIncomeExpensePreferenceUseCase,
    private val getTransactionsByIdsUseCase: GetTransactionsByIdsUseCase,
) : ComposeViewModel<PieChartStatisticState, PieChartStatisticEvent>() {

    private var treatTransfersAsIncomeExpense by mutableStateOf(false)
    private var transactionType by mutableStateOf(TransactionType.INCOME)
    private var period by mutableStateOf(TimePeriod())
    private var baseCurrency by mutableStateOf("")
    private var totalAmount by mutableDoubleStateOf(0.0)
    private var categoryAmounts by mutableStateOf<ImmutableList<CategoryAmount>>(persistentListOf())
    private var selectedCategory by mutableStateOf<SelectedCategory?>(null)
    private var accountIdFilterList by mutableStateOf<ImmutableList<UUID>>(persistentListOf())
    private var showCloseButtonOnly by mutableStateOf(false)
    private var filterExcluded by mutableStateOf(false)
    private var inputTransactionIds by mutableStateOf<ImmutableList<UUID>>(persistentListOf())
    private var grouping by mutableStateOf(PieChartGrouping.CATEGORY)

    @Composable
    override fun uiState(): PieChartStatisticState {
        return PieChartStatisticState(
            transactionType = getTransactionType(),
            period = getPeriod(),
            baseCurrency = getBaseCurrency(),
            totalAmount = getTotalAmount(),
            categoryAmounts = getCategoryAmounts(),
            selectedCategory = getSelectedCategory(),
            accountIdFilterList = getAccountIdFilterList(),
            showCloseButtonOnly = getShowCloseButtonOnly(),
            filterExcluded = getFilterExcluded(),
            grouping = getGrouping()
        )
    }

    @Composable
    private fun getTransactionType(): TransactionType {
        return transactionType
    }

    @Composable
    private fun getPeriod(): TimePeriod {
        return period
    }

    @Composable
    private fun getBaseCurrency(): String {
        return baseCurrency
    }

    @Composable
    private fun getTotalAmount(): Double {
        return totalAmount
    }

    @Composable
    private fun getCategoryAmounts(): ImmutableList<CategoryAmount> {
        return categoryAmounts
    }

    @Composable
    private fun getSelectedCategory(): SelectedCategory? {
        return selectedCategory
    }

    @Composable
    private fun getAccountIdFilterList(): ImmutableList<UUID> {
        return accountIdFilterList
    }

    @Composable
    private fun getShowCloseButtonOnly(): Boolean {
        return showCloseButtonOnly
    }

    @Composable
    private fun getFilterExcluded(): Boolean {
        return filterExcluded
    }

    @Composable
    private fun getGrouping(): PieChartGrouping {
        return grouping
    }

    override fun onEvent(event: PieChartStatisticEvent) {
        viewModelScope.launch(Dispatchers.Default) {
            when (event) {
                is PieChartStatisticEvent.OnSelectNextMonth -> nextMonth()
                is PieChartStatisticEvent.OnSelectPreviousMonth -> previousMonth()
                is PieChartStatisticEvent.OnSetPeriod -> onSetPeriod(event.timePeriod)
                is PieChartStatisticEvent.OnCategoryClicked -> onCategoryClicked(event.categoryId)
                is PieChartStatisticEvent.OnGroupingSelected -> onGroupingSelected(event.grouping)
            }
        }
    }

    fun start(
        type: TransactionType,
        accountIdFilterList: ImmutableList<UUID>,
        filterExcluded: Boolean,
        inputTransactionIds: ImmutableList<UUID>,
        transfersAsIncomeExpense: Boolean,
    ) {
        viewModelScope.launch(Dispatchers.Default) {
            startInternally(
                period = periodState.selectedPeriod,
                type = type,
                accountIdFilterList = accountIdFilterList,
                filterExclude = filterExcluded,
                inputTransactionIds = inputTransactionIds,
                transfersAsIncomeExpenseValue = transfersAsIncomeExpense
            )
        }
    }

    private suspend fun startInternally(
        period: TimePeriod,
        type: TransactionType,
        accountIdFilterList: ImmutableList<UUID>,
        filterExclude: Boolean,
        inputTransactionIds: ImmutableList<UUID>,
        transfersAsIncomeExpenseValue: Boolean
    ) {
        initialise(period, type, accountIdFilterList, filterExclude, inputTransactionIds)
        treatTransfersAsIncomeExpense = transfersAsIncomeExpenseValue
        load(periodValue = period)
    }

    private suspend fun initialise(
        periodValue: TimePeriod,
        type: TransactionType,
        accountIdFilterListValue: ImmutableList<UUID>,
        filterExcludedValue: Boolean,
        inputTransactionIdsValue: ImmutableList<UUID>
    ) {
        val baseCurrencyValue = getBaseCurrencyCode()

        period = periodValue
        transactionType = type
        accountIdFilterList = accountIdFilterListValue
        filterExcluded = filterExcludedValue
        inputTransactionIds = inputTransactionIdsValue
        showCloseButtonOnly = inputTransactionIdsValue.isNotEmpty()
        baseCurrency = baseCurrencyValue
        grouping = defaultGroupingFor(type)
    }

    private fun defaultGroupingFor(type: TransactionType): PieChartGrouping {
        return if (type == TransactionType.INCOME) {
            PieChartGrouping.ACCOUNT
        } else {
            PieChartGrouping.CATEGORY
        }
    }

    private suspend fun load(
        periodValue: TimePeriod
    ) {
        val type = transactionType
        val accountIdFilterList = accountIdFilterList
        val inputTransactionIds = inputTransactionIds
        val baseCurrency = baseCurrency
        val range = periodState.rangeOf(periodValue)

        val treatTransferAsIncExp =
            getTransfersAsIncomeExpensePreference() &&
                    accountIdFilterList.isNotEmpty() &&
                    treatTransfersAsIncomeExpense

        val pieChartActOutput = withContext(Dispatchers.IO) {
            val inputTransactions = if (inputTransactionIds.isEmpty()) {
                emptyList()
            } else {
                getTransactionsByIdsUseCase(inputTransactionIds)
            }
            buildPieChartDataUseCase(
                baseCurrency = baseCurrency,
                range = range,
                type = type,
                accountIdFilterList = accountIdFilterList,
                treatTransferAsIncExp = treatTransferAsIncExp,
                existingTransactions = inputTransactions,
                showAccountTransfersCategory = accountIdFilterList.isNotEmpty(),
                grouping = grouping
            )
        }

        val totalAmountValue = pieChartActOutput.totalAmount
        val categoryAmountsValue = pieChartActOutput.categoryAmounts

        period = periodValue
        totalAmount = totalAmountValue
        categoryAmounts = categoryAmountsValue
        selectedCategory = null
    }

    private suspend fun onSetPeriod(periodValue: TimePeriod) {
        periodState.select(periodValue)
        load(
            periodValue = periodValue
        )
    }

    private suspend fun onGroupingSelected(newGrouping: PieChartGrouping) {
        if (newGrouping == grouping) return
        grouping = newGrouping
        load(
            periodValue = period
        )
    }

    private suspend fun nextMonth() {
        val nextPeriod = periodState.shiftMonth(period, increment = 1L)
        if (nextPeriod != null) {
            periodState.select(nextPeriod)
            load(
                periodValue = nextPeriod
            )
        }
    }

    private suspend fun previousMonth() {
        val previousPeriod = periodState.shiftMonth(period, increment = -1L)
        if (previousPeriod != null) {
            periodState.select(previousPeriod)
            load(
                periodValue = previousPeriod
            )
        }
    }

    private suspend fun onCategoryClicked(categoryId: UUID?) {
        val clickedCategoryId = categoryAmounts
            .firstOrNull { it.category?.id?.value == categoryId }
            ?.category
            ?.id
            ?.value
        val selectedCategoryValue = if (categoryId == selectedCategory?.categoryId) {
            null
        } else {
            clickedCategoryId?.let { SelectedCategory(categoryId = it) }
        }

        val existingCategoryAmounts = categoryAmounts
        val newCategoryAmounts = if (selectedCategoryValue != null) {
            existingCategoryAmounts
                .sortedByDescending { it.amount }
                .sortedByDescending {
                    selectedCategoryValue.categoryId == it.category?.id?.value
                }
        } else {
            existingCategoryAmounts.sortedByDescending {
                it.amount
            }
        }.toImmutableList()

        selectedCategory = selectedCategoryValue
        categoryAmounts = newCategoryAmounts
    }
}
