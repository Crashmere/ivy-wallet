package com.ivy.reports

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.ivy.data.model.Account
import com.ivy.data.model.Category
import com.ivy.data.model.ExternalFile
import com.ivy.data.model.Expense
import com.ivy.data.model.Income
import com.ivy.data.model.Tag
import com.ivy.data.model.Transaction
import com.ivy.data.model.TransactionHistoryDateDivider
import com.ivy.data.model.TransactionHistoryItem
import com.ivy.data.model.TransactionHistoryTransaction
import com.ivy.data.model.TransactionType
import com.ivy.data.model.Transfer
import com.ivy.data.model.getFromValue
import com.ivy.data.model.getTransactionType
import com.ivy.domain.preferences.toggles.PreferenceToggleCatalog
import com.ivy.domain.preferences.toggles.PreferenceToggleService
import com.ivy.domain.usecase.account.GetAccountsUseCase
import com.ivy.domain.usecase.category.GetCategoriesUseCase
import com.ivy.domain.usecase.csv.ExportCsvUseCase
import com.ivy.domain.usecase.currency.GetBaseCurrencyCodeUseCase
import com.ivy.domain.usecase.exchange.ExchangeTransactionAmountUseCase
import com.ivy.domain.usecase.tag.GetTagsUseCase
import com.ivy.domain.usecase.transaction.BuildTransactionHistoryItemsUseCase
import com.ivy.domain.usecase.transaction.GetTransactionsUseCase
import com.ivy.ui.ComposeViewModel
import com.ivy.ui.period.PeriodState
import com.ivy.ui.period.TimePeriod
import com.ivy.ui.platform.FilePicker
import com.ivy.ui.preferences.asEnabledState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject

private val exportTimestampFormatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmm")

@Stable
@HiltViewModel
internal class ReportViewModel @Inject internal constructor(
    private val periodState: PeriodState,
    private val exchangeTransactionAmountUseCase: ExchangeTransactionAmountUseCase,
    private val getAccountsUseCase: GetAccountsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val buildTransactionHistoryItemsUseCase: BuildTransactionHistoryItemsUseCase,
    private val getBaseCurrencyCode: GetBaseCurrencyCodeUseCase,
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val getTagsUseCase: GetTagsUseCase,
    private val exportCsvUseCase: ExportCsvUseCase,
    private val preferenceToggles: PreferenceToggleCatalog,
    private val preferenceToggleService: PreferenceToggleService,
    private val filePicker: FilePicker,
) : ComposeViewModel<ReportState, ReportEvent>() {

    private val baseCurrency = mutableStateOf("")
    private val selectedPeriod = mutableStateOf(periodState.currentMonth())
    private val sortOrder = mutableStateOf(SortOrder.TIME)

    private val includeIncome = mutableStateOf(true)
    private val includeExpense = mutableStateOf(true)
    private val includeTransfer = mutableStateOf(false)

    private val allCategories = mutableStateOf<ImmutableList<Category>>(persistentListOf())
    private val allAccounts = mutableStateOf<ImmutableList<Account>>(persistentListOf())
    private val allTags = mutableStateOf<ImmutableList<Tag>>(persistentListOf())

    private val filterCategories = mutableStateOf<ImmutableList<Category>>(persistentListOf())
    private val filterHasUncategorized = mutableStateOf(false)
    private val filterAccounts = mutableStateOf<ImmutableList<Account>>(persistentListOf())
    private val filterTags = mutableStateOf<ImmutableList<Tag>>(persistentListOf())

    private val selectedCategoryIds = mutableStateOf<Set<UUID>>(emptySet())
    private val uncategorizedSelected = mutableStateOf(false)
    private val selectedAccountIds = mutableStateOf<Set<UUID>>(emptySet())
    private val selectedTagIds = mutableStateOf<Set<UUID>>(emptySet())

    private val includeKeywords = mutableStateOf<List<String>>(emptyList())
    private val excludeKeywords = mutableStateOf<List<String>>(emptyList())
    private val amountMin = mutableStateOf<Float?>(null)
    private val amountMax = mutableStateOf<Float?>(null)
    private val amountRangeMin = mutableFloatStateOf(0f)
    private val amountRangeMax = mutableFloatStateOf(10000f)

    private val matchingTransactions =
        mutableStateOf<ImmutableList<TransactionHistoryItem>>(persistentListOf())
    private val matchingCount = mutableIntStateOf(0)
    private val income = mutableDoubleStateOf(0.0)
    private val expenses = mutableDoubleStateOf(0.0)
    private val expenseByCategory =
        mutableStateOf<ImmutableList<CategoryBreakdownItem>>(persistentListOf())
    private val incomeByCategory =
        mutableStateOf<ImmutableList<CategoryBreakdownItem>>(persistentListOf())
    private val loading = mutableStateOf(false)
    private val advancedExpanded = mutableStateOf(false)

    private var inRangeRaw: List<Transaction> = emptyList()
    private var matchingRaw: List<Transaction> = emptyList()
    private var accountModels: List<Account> = emptyList()

    private val _uiEvents = MutableSharedFlow<ReportUiEvent>()
    val uiEvents: SharedFlow<ReportUiEvent> = _uiEvents.asSharedFlow()

    @Composable
    private fun getShouldShowAccountColors(): Boolean {
        val preference = preferenceToggles.showAccountColorsInTransactions
        return preferenceToggleService.enabledFlow(preference)
            .asEnabledState(preference.defaultValue)
    }

    @Composable
    override fun uiState(): ReportState {
        LaunchedEffect(Unit) {
            start()
        }

        return ReportState(
            baseCurrency = baseCurrency.value,
            period = selectedPeriod.value,
            sortOrder = sortOrder.value,
            includeIncome = includeIncome.value,
            includeExpense = includeExpense.value,
            includeTransfer = includeTransfer.value,
            filterCategories = filterCategories.value,
            filterHasUncategorized = filterHasUncategorized.value,
            filterAccounts = filterAccounts.value,
            filterTags = filterTags.value,
            selectedCategoryIds = selectedCategoryIds.value.toImmutableSet(),
            uncategorizedSelected = uncategorizedSelected.value,
            selectedAccountIds = selectedAccountIds.value.toImmutableSet(),
            selectedTagIds = selectedTagIds.value.toImmutableSet(),
            includeKeywords = includeKeywords.value,
            excludeKeywords = excludeKeywords.value,
            amountMin = amountMin.value,
            amountMax = amountMax.value,
            amountRangeMin = amountRangeMin.floatValue,
            amountRangeMax = amountRangeMax.floatValue,
            matchingTransactions = matchingTransactions.value,
            matchingCount = matchingCount.intValue,
            income = income.doubleValue,
            expenses = expenses.doubleValue,
            expenseByCategory = expenseByCategory.value,
            incomeByCategory = incomeByCategory.value,
            allCategories = allCategories.value,
            allAccounts = allAccounts.value,
            allTags = allTags.value,
            shouldShowAccountColorsInTransactions = getShouldShowAccountColors(),
            loading = loading.value,
            advancedExpanded = advancedExpanded.value,
        )
    }

    override fun onEvent(event: ReportEvent) {
        when (event) {
            ReportEvent.OnPreviousMonth -> shiftMonth(-1L)
            ReportEvent.OnNextMonth -> shiftMonth(1L)
            is ReportEvent.OnSelectPeriod -> selectPeriod(event.period)
            ReportEvent.ToggleIncome -> {
                includeIncome.value = !includeIncome.value; refilter()
            }
            ReportEvent.ToggleExpense -> {
                includeExpense.value = !includeExpense.value; refilter()
            }
            ReportEvent.ToggleTransfer -> {
                includeTransfer.value = !includeTransfer.value; refilter()
            }
            is ReportEvent.ToggleCategoryFilter -> toggleCategory(event.categoryId)
            ReportEvent.ToggleUncategorizedFilter -> toggleUncategorized()
            is ReportEvent.ToggleAccountFilter -> toggleAccount(event.accountId)
            is ReportEvent.ToggleTagFilter -> toggleTag(event.tagId)
            ReportEvent.ClearFilters -> clearFilters()
            ReportEvent.SelectAllTypes -> selectAllTypes()
            ReportEvent.SelectAllCategories -> selectAllCategories()
            ReportEvent.SelectAllAccounts -> selectAllAccounts()
            ReportEvent.SelectAllTags -> selectAllTags()
            is ReportEvent.AddIncludeKeyword -> {
                includeKeywords.value = includeKeywords.value + event.keyword; refilter()
            }
            is ReportEvent.RemoveIncludeKeyword -> {
                includeKeywords.value = includeKeywords.value - event.keyword; refilter()
            }
            is ReportEvent.AddExcludeKeyword -> {
                excludeKeywords.value = excludeKeywords.value + event.keyword; refilter()
            }
            is ReportEvent.RemoveExcludeKeyword -> {
                excludeKeywords.value = excludeKeywords.value - event.keyword; refilter()
            }
            is ReportEvent.SetAmountRange -> {
                amountMin.value = event.min; amountMax.value = event.max; refilter()
            }
            ReportEvent.ToggleSortOrder -> {
                sortOrder.value = if (sortOrder.value == SortOrder.TIME) {
                    SortOrder.AMOUNT
                } else {
                    SortOrder.TIME
                }
                refilter()
            }
            ReportEvent.ToggleAdvanced -> {
                advancedExpanded.value = !advancedExpanded.value
            }
            ReportEvent.OnExport -> export()
        }
    }

    private fun start() {
        viewModelScope.launch(Dispatchers.IO) {
            baseCurrency.value = getBaseCurrencyCode()
            allCategories.value = getCategoriesUseCase().toImmutableList()
            accountModels = getAccountsUseCase()
            allAccounts.value = accountModels.toImmutableList()
            allTags.value = getTagsUseCase().toImmutableList()
            reload()
        }
    }

    private suspend fun reload() {
        loading.value = true
        val range = periodState.rangeOf(selectedPeriod.value)
        inRangeRaw = getTransactionsUseCase()
            .filter { it.settled }
            .filter { range.includes(it.time) }
        buildFilterOptions(inRangeRaw)
        recompute()
        loading.value = false
    }

    private fun buildFilterOptions(transactions: List<Transaction>) {
        val usedCatIds = transactions.mapNotNull { it.category?.value }.toHashSet()
        val visibleCatIds = usedCatIds + selectedCategoryIds.value
        filterCategories.value = allCategories.value
            .filter { it.id.value in visibleCatIds }
            .toImmutableList()
        filterHasUncategorized.value =
            transactions.any { it.category == null } || uncategorizedSelected.value

        val usedAccIds = transactions.map {
            when (it) {
                is Expense -> it.account.value
                is Income -> it.account.value
                is Transfer -> it.fromAccount.value
            }
        }.toHashSet()
        val visibleAccIds = usedAccIds + selectedAccountIds.value
        filterAccounts.value = allAccounts.value
            .filter { it.id.value in visibleAccIds }
            .toImmutableList()

        val usedTagIds = transactions.flatMap { it.tags }.map { it.value }.toHashSet()
        val visibleTagIds = usedTagIds + selectedTagIds.value
        filterTags.value = allTags.value
            .filter { it.id.value in visibleTagIds }
            .toImmutableList()
    }

    private suspend fun recompute() {
        val filtered = applyFilter(inRangeRaw)
        matchingRaw = filtered

        updateAmountRange(filtered)
        computeBreakdowns(filtered)

        val bc = baseCurrency.value
        if (sortOrder.value == SortOrder.TIME) {
            val history = buildTransactionHistoryItemsUseCase(
                baseCurrency = bc,
                transactions = filtered.sortedByDescending { it.time }
            ).toImmutableList()
            matchingTransactions.value = history
            matchingCount.intValue = filtered.size

            var inc = 0.0
            var exp = 0.0
            history.forEach {
                if (it is TransactionHistoryDateDivider) {
                    inc += it.income
                    exp += it.expenses
                }
            }
            income.doubleValue = inc
            expenses.doubleValue = exp
        } else {
            val withAmounts = filtered.map { tx ->
                val exchanged = exchangeTransactionAmountUseCase(
                    transaction = tx,
                    accounts = accountModels,
                    baseCurrency = bc,
                )
                tx to exchanged
            }.sortedByDescending { it.second }

            var inc = 0.0
            var exp = 0.0
            val historyItems = withAmounts.map { (tx, _) ->
                when (tx.getTransactionType()) {
                    TransactionType.INCOME -> inc += tx.getFromValue().amount.value.toDouble()
                    TransactionType.EXPENSE -> exp += tx.getFromValue().amount.value.toDouble()
                    TransactionType.TRANSFER -> {}
                }
                TransactionHistoryTransaction(
                    transaction = tx,
                    tags = persistentListOf(),
                )
            }
            matchingTransactions.value = historyItems.toImmutableList()
            matchingCount.intValue = filtered.size
            income.doubleValue = inc
            expenses.doubleValue = exp
        }
    }

    private suspend fun updateAmountRange(transactions: List<Transaction>) {
        if (transactions.isEmpty()) {
            amountRangeMin.floatValue = 0f
            amountRangeMax.floatValue = 0f
            return
        }
        val bc = baseCurrency.value
        var min = Float.MAX_VALUE
        var max = 0f
        for (tx in transactions) {
            val amount = exchangeTransactionAmountUseCase(
                transaction = tx,
                accounts = accountModels,
                baseCurrency = bc,
            ).toFloat()
            if (amount < min) min = amount
            if (amount > max) max = amount
        }
        amountRangeMin.floatValue = min
        amountRangeMax.floatValue = if (max > min) max else min + 1f
    }

    private suspend fun computeBreakdowns(transactions: List<Transaction>) {
        val bc = baseCurrency.value
        val catById = allCategories.value.associateBy { it.id.value }
        val expenseMap = LinkedHashMap<UUID?, Double>()
        val incomeMap = LinkedHashMap<UUID?, Double>()
        for (tx in transactions) {
            val type = tx.getTransactionType()
            if (type == TransactionType.TRANSFER) continue
            val amount = exchangeTransactionAmountUseCase(
                transaction = tx,
                accounts = accountModels,
                baseCurrency = bc,
            ).toDouble()
            val catId = tx.category?.value
            when (type) {
                TransactionType.EXPENSE ->
                    expenseMap[catId] = (expenseMap[catId] ?: 0.0) + amount
                TransactionType.INCOME ->
                    incomeMap[catId] = (incomeMap[catId] ?: 0.0) + amount
                TransactionType.TRANSFER -> {}
            }
        }
        expenseByCategory.value = expenseMap.toBreakdownItems(catById)
        incomeByCategory.value = incomeMap.toBreakdownItems(catById)
    }

    private fun Map<UUID?, Double>.toBreakdownItems(
        catById: Map<UUID, Category>,
    ): ImmutableList<CategoryBreakdownItem> =
        map { (catId, amount) ->
            val cat = catId?.let { catById[it] }
            CategoryBreakdownItem(
                name = cat?.name?.value ?: "无类别",
                colorArgb = cat?.color?.value,
                amount = amount,
            )
        }.filter { it.amount > 0.0 }
            .sortedByDescending { it.amount }
            .toImmutableList()

    private suspend fun applyFilter(transactions: List<Transaction>): List<Transaction> {
        val categoryIds = selectedCategoryIds.value
        val includeUncategorized = uncategorizedSelected.value
        val accountIds = selectedAccountIds.value
        val tagIds = selectedTagIds.value
        val categoryFilterActive = categoryIds.isNotEmpty() || includeUncategorized
        val accountFilterActive = accountIds.isNotEmpty()
        val tagFilterActive = tagIds.isNotEmpty()
        val incKws = includeKeywords.value
        val excKws = excludeKeywords.value
        val minAmt = amountMin.value
        val maxAmt = amountMax.value
        val bc = baseCurrency.value

        return transactions.filter { tx ->
            val txType = tx.getTransactionType()
            when (txType) {
                TransactionType.INCOME -> includeIncome.value
                TransactionType.EXPENSE -> includeExpense.value
                TransactionType.TRANSFER -> includeTransfer.value
            }
        }.filter { tx ->
            if (!categoryFilterActive) return@filter true
            val txCatId = tx.category?.value
            if (txCatId == null) includeUncategorized else txCatId in categoryIds
        }.filter { tx ->
            if (!accountFilterActive) return@filter true
            when (tx) {
                is Expense -> tx.account.value in accountIds
                is Income -> tx.account.value in accountIds
                is Transfer -> tx.fromAccount.value in accountIds ||
                        tx.toAccount.value in accountIds
            }
        }.filter { tx ->
            if (!tagFilterActive) return@filter true
            tx.tags.any { it.value in tagIds }
        }.filter { tx ->
            if (incKws.isEmpty()) return@filter true
            val title = tx.title?.value ?: ""
            val desc = tx.description?.value ?: ""
            incKws.any {
                title.contains(it, ignoreCase = true) ||
                        desc.contains(it, ignoreCase = true)
            }
        }.filter { tx ->
            if (excKws.isEmpty()) return@filter true
            val title = tx.title?.value ?: ""
            val desc = tx.description?.value ?: ""
            excKws.none {
                title.contains(it, ignoreCase = true) ||
                        desc.contains(it, ignoreCase = true)
            }
        }.let { list ->
            if (minAmt == null && maxAmt == null) return@let list
            val result = mutableListOf<Transaction>()
            for (tx in list) {
                val amount = exchangeTransactionAmountUseCase(
                    transaction = tx,
                    accounts = accountModels,
                    baseCurrency = bc,
                ).toFloat()
                val passMin = minAmt == null || amount >= minAmt
                val passMax = maxAmt == null || amount <= maxAmt
                if (passMin && passMax) result += tx
            }
            result
        }
    }

    private fun shiftMonth(increment: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val shifted = periodState.shiftMonth(selectedPeriod.value, increment) ?: return@launch
            selectedPeriod.value = shifted
            reload()
        }
    }

    private fun selectPeriod(period: TimePeriod) {
        viewModelScope.launch(Dispatchers.IO) {
            selectedPeriod.value = period
            reload()
        }
    }

    private fun toggleCategory(categoryId: UUID) {
        selectedCategoryIds.value = selectedCategoryIds.value.toggle(categoryId)
        refilter()
    }

    private fun toggleUncategorized() {
        uncategorizedSelected.value = !uncategorizedSelected.value
        refilter()
    }

    private fun toggleAccount(accountId: UUID) {
        selectedAccountIds.value = selectedAccountIds.value.toggle(accountId)
        refilter()
    }

    private fun toggleTag(tagId: UUID) {
        selectedTagIds.value = selectedTagIds.value.toggle(tagId)
        refilter()
    }

    private fun selectAllTypes() {
        val allSelected = includeIncome.value && includeExpense.value && includeTransfer.value
        if (allSelected) {
            includeIncome.value = true
            includeExpense.value = true
            includeTransfer.value = false
        } else {
            includeIncome.value = true
            includeExpense.value = true
            includeTransfer.value = true
        }
        refilter()
    }

    private fun selectAllCategories() {
        val allCatIds = filterCategories.value.map { it.id.value }.toSet()
        val hasUncat = filterHasUncategorized.value
        val allSelected = selectedCategoryIds.value.containsAll(allCatIds) &&
                (!hasUncat || uncategorizedSelected.value)
        if (allSelected) {
            selectedCategoryIds.value = emptySet()
            uncategorizedSelected.value = false
        } else {
            selectedCategoryIds.value = allCatIds
            if (hasUncat) uncategorizedSelected.value = true
        }
        refilter()
    }

    private fun selectAllAccounts() {
        val allAccIds = filterAccounts.value.map { it.id.value }.toSet()
        val allSelected = selectedAccountIds.value.containsAll(allAccIds)
        selectedAccountIds.value = if (allSelected) emptySet() else allAccIds
        refilter()
    }

    private fun selectAllTags() {
        val allTagIdSet = filterTags.value.map { it.id.value }.toSet()
        val allSelected = selectedTagIds.value.containsAll(allTagIdSet)
        selectedTagIds.value = if (allSelected) emptySet() else allTagIdSet
        refilter()
    }

    private fun clearFilters() {
        includeIncome.value = true
        includeExpense.value = true
        includeTransfer.value = false
        selectedCategoryIds.value = emptySet()
        uncategorizedSelected.value = false
        selectedAccountIds.value = emptySet()
        selectedTagIds.value = emptySet()
        includeKeywords.value = emptyList()
        excludeKeywords.value = emptyList()
        amountMin.value = null
        amountMax.value = null
        refilter()
    }

    private fun refilter() {
        viewModelScope.launch(Dispatchers.IO) {
            recompute()
        }
    }

    private fun export() {
        val matched = matchingRaw
        if (matched.isEmpty()) return

        filePicker.createFile(
            "IvyWalletReport-${utcTimestamp()}.csv"
        ) { fileUri ->
            viewModelScope.launch {
                loading.value = true
                exportCsvUseCase.exportToFile(
                    outputFile = ExternalFile(fileUri.toString()),
                    exportScope = { matched }
                )
                _uiEvents.emit(ReportUiEvent.ShareCsvFile(fileUri))
                loading.value = false
            }
        }
    }

    private fun utcTimestamp(): String =
        Instant.now().atZone(ZoneOffset.UTC).toLocalDateTime().format(exportTimestampFormatter)
}

private fun Set<UUID>.toggle(id: UUID): Set<UUID> =
    if (contains(id)) this - id else this + id
