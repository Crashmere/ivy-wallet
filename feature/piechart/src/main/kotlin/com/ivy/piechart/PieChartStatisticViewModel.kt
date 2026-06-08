package com.ivy.piechart

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.ivy.data.model.TransactionType
import com.ivy.data.model.Category
import com.ivy.domain.usecase.currency.GetBaseCurrencyCodeUseCase
import com.ivy.domain.usecase.settings.GetTransfersAsIncomeExpensePreferenceUseCase
import com.ivy.domain.usecase.transaction.GetLegacyTransactionsByIdsUseCase
import com.ivy.ui.period.PeriodState
import com.ivy.ui.period.TimePeriod
import com.ivy.ui.ComposeViewModel
import com.ivy.ui.modal.ChoosePeriodModalData
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
    private val getLegacyTransactionsByIdsUseCase: GetLegacyTransactionsByIdsUseCase,
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
    private var choosePeriodModal by mutableStateOf<ChoosePeriodModalData?>(null)

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
            choosePeriodModal = getChoosePeriodModal()
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
    private fun getChoosePeriodModal(): ChoosePeriodModalData? {
        return choosePeriodModal
    }

    override fun onEvent(event: PieChartStatisticEvent) {
        viewModelScope.launch(Dispatchers.Default) {
            when (event) {
                is PieChartStatisticEvent.OnSelectNextMonth -> nextMonth()
                is PieChartStatisticEvent.OnSelectPreviousMonth -> previousMonth()
                is PieChartStatisticEvent.OnSetPeriod -> onSetPeriod(event.timePeriod)
                is PieChartStatisticEvent.OnShowMonthModal -> configureMonthModal(event.timePeriod)
                is PieChartStatisticEvent.OnCategoryClicked -> onCategoryClicked(event.category)
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
                getLegacyTransactionsByIdsUseCase(inputTransactionIds)
            }
            buildPieChartDataUseCase(
                baseCurrency = baseCurrency,
                range = range,
                type = type,
                accountIdFilterList = accountIdFilterList,
                treatTransferAsIncExp = treatTransferAsIncExp,
                existingTransactions = inputTransactions,
                showAccountTransfersCategory = accountIdFilterList.isNotEmpty()
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

    private suspend fun configureMonthModal(timePeriod: TimePeriod?) {
        val choosePeriodModalData = if (timePeriod != null) {
            ChoosePeriodModalData(period = timePeriod)
        } else {
            null
        }

        choosePeriodModal = choosePeriodModalData
    }

    private suspend fun onCategoryClicked(clickedCategory: Category?) {
        val selectedCategoryValue = if (clickedCategory == selectedCategory?.category) {
            null
        } else {
            clickedCategory?.let { SelectedCategory(category = it) }
        }

        val existingCategoryAmounts = categoryAmounts
        val newCategoryAmounts = if (selectedCategoryValue != null) {
            existingCategoryAmounts
                .sortedByDescending { it.amount }
                .sortedByDescending {
                    selectedCategoryValue.category == it.category
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
