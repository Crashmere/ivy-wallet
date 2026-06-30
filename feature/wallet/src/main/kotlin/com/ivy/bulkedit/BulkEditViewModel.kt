package com.ivy.bulkedit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.ivy.data.model.Account
import com.ivy.data.model.AccountId
import com.ivy.data.model.Category
import com.ivy.data.model.CategoryId
import com.ivy.data.model.Tag
import com.ivy.data.model.TagId
import com.ivy.data.model.Transaction
import com.ivy.data.model.TransactionHistoryDateDivider
import com.ivy.data.model.TransactionHistoryItem
import com.ivy.domain.preferences.toggles.PreferenceToggleCatalog
import com.ivy.domain.preferences.toggles.PreferenceToggleService
import com.ivy.domain.usecase.account.GetAccountsUseCase
import com.ivy.domain.usecase.category.GetCategoriesUseCase
import com.ivy.domain.usecase.currency.GetBaseCurrencyCodeUseCase
import com.ivy.domain.usecase.tag.GetTagsUseCase
import com.ivy.domain.usecase.transaction.BuildTransactionHistoryItemsUseCase
import com.ivy.domain.usecase.transaction.BulkUpdateTransactionsUseCase
import com.ivy.domain.usecase.transaction.GetTransactionsUseCase
import com.ivy.ui.ComposeViewModel
import com.ivy.ui.period.PeriodState
import com.ivy.ui.period.TimePeriod
import com.ivy.ui.preferences.asEnabledState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@Stable
@HiltViewModel
internal class BulkEditViewModel @Inject internal constructor(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getAccountsUseCase: GetAccountsUseCase,
    private val getTagsUseCase: GetTagsUseCase,
    private val getBaseCurrencyCode: GetBaseCurrencyCodeUseCase,
    private val buildTransactionHistoryItemsUseCase: BuildTransactionHistoryItemsUseCase,
    private val bulkUpdateTransactionsUseCase: BulkUpdateTransactionsUseCase,
    private val periodState: PeriodState,
    private val preferenceToggles: PreferenceToggleCatalog,
    private val preferenceToggleService: PreferenceToggleService,
) : ComposeViewModel<BulkEditState, BulkEditEvent>() {

    private val baseCurrency = mutableStateOf("")
    private val selectedPeriod = mutableStateOf(periodState.currentMonth())

    private val allCategories = mutableStateOf<ImmutableList<Category>>(persistentListOf())
    private val allAccounts = mutableStateOf<ImmutableList<Account>>(persistentListOf())
    private val allTags = mutableStateOf<ImmutableList<Tag>>(persistentListOf())

    private val filterCategories = mutableStateOf<ImmutableList<Category>>(persistentListOf())
    private val filterHasUncategorized = mutableStateOf(false)
    private val filterTags = mutableStateOf<ImmutableList<Tag>>(persistentListOf())

    private val selectedCategoryIds = mutableStateOf<Set<UUID>>(emptySet())
    private val uncategorizedSelected = mutableStateOf(false)
    private val selectedTagIds = mutableStateOf<Set<UUID>>(emptySet())

    private val matchingTransactions =
        mutableStateOf<ImmutableList<TransactionHistoryItem>>(persistentListOf())
    private val matchingCount = mutableIntStateOf(0)
    private val income = mutableDoubleStateOf(0.0)
    private val expenses = mutableDoubleStateOf(0.0)
    private val loading = mutableStateOf(true)

    // Raw matched transactions, cached for the bulk operations.
    private var matchingRaw: List<Transaction> = emptyList()

    // All transactions inside the current period, used to derive filter options.
    private var inRangeRaw: List<Transaction> = emptyList()

    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages = _messages.receiveAsFlow()

    @Composable
    override fun uiState(): BulkEditState {
        LaunchedEffect(Unit) {
            start()
        }
        return BulkEditState(
            baseCurrency = baseCurrency.value,
            period = selectedPeriod.value,
            filterCategories = filterCategories.value,
            filterHasUncategorized = filterHasUncategorized.value,
            filterTags = filterTags.value,
            allCategories = allCategories.value,
            allAccounts = allAccounts.value,
            allTags = allTags.value,
            selectedCategoryIds = selectedCategoryIds.value.toImmutableSet(),
            uncategorizedSelected = uncategorizedSelected.value,
            selectedTagIds = selectedTagIds.value.toImmutableSet(),
            matchingTransactions = matchingTransactions.value,
            matchingCount = matchingCount.intValue,
            income = income.doubleValue,
            expenses = expenses.doubleValue,
            shouldShowAccountColorsInTransactions = getShouldShowAccountColors(),
            loading = loading.value,
        )
    }

    @Composable
    private fun getShouldShowAccountColors(): Boolean {
        val preference = preferenceToggles.showAccountColorsInTransactions
        return preferenceToggleService.enabledFlow(preference)
            .asEnabledState(preference.defaultValue)
    }

    private fun start() {
        viewModelScope.launch(Dispatchers.IO) {
            initialise()
            reload()
        }
    }

    private suspend fun initialise() {
        baseCurrency.value = getBaseCurrencyCode()
        allCategories.value = getCategoriesUseCase().toImmutableList()
        allAccounts.value = getAccountsUseCase().toImmutableList()
        allTags.value = getTagsUseCase().toImmutableList()
    }

    private suspend fun reload() {
        loading.value = true
        val range = periodState.rangeOf(selectedPeriod.value)
        inRangeRaw = getTransactionsUseCase().filter { range.includes(it.time) }
        buildFilterOptions(inRangeRaw)
        recompute()
        loading.value = false
    }

    private suspend fun recompute() {
        val filtered = applyFilter(inRangeRaw)
        matchingRaw = filtered

        val history = buildTransactionHistoryItemsUseCase(
            baseCurrency = baseCurrency.value,
            transactions = filtered
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
    }

    private fun buildFilterOptions(transactions: List<Transaction>) {
        // Keep already-selected options visible even if no transaction in the
        // current period uses them, so their chip doesn't silently disappear.
        val usedCatIds = transactions.mapNotNull { it.category?.value }.toHashSet()
        val visibleCatIds = usedCatIds + selectedCategoryIds.value
        filterCategories.value = allCategories.value
            .filter { it.id.value in visibleCatIds }
            .toImmutableList()
        filterHasUncategorized.value =
            transactions.any { it.category == null } || uncategorizedSelected.value

        val usedTagIds = transactions.flatMap { it.tags }.map { it.value }.toHashSet()
        val visibleTagIds = usedTagIds + selectedTagIds.value
        filterTags.value = allTags.value
            .filter { it.id.value in visibleTagIds }
            .toImmutableList()
    }

    private fun applyFilter(transactions: List<Transaction>): List<Transaction> {
        val categoryIds = selectedCategoryIds.value
        val includeUncategorized = uncategorizedSelected.value
        val tagIds = selectedTagIds.value
        val categoryFilterActive = categoryIds.isNotEmpty() || includeUncategorized
        val tagFilterActive = tagIds.isNotEmpty()
        if (!categoryFilterActive && !tagFilterActive) return transactions
        return transactions.filter { transaction ->
            val categoryMatches = !categoryFilterActive || run {
                val txCategoryId = transaction.category?.value
                if (txCategoryId == null) includeUncategorized else txCategoryId in categoryIds
            }
            val tagMatches = !tagFilterActive ||
                    transaction.tags.any { it.value in tagIds }
            categoryMatches && tagMatches
        }
    }

    override fun onEvent(event: BulkEditEvent) {
        when (event) {
            BulkEditEvent.OnPreviousMonth -> shiftMonth(-1L)
            BulkEditEvent.OnNextMonth -> shiftMonth(1L)
            is BulkEditEvent.OnSelectPeriod -> selectPeriod(event.period)
            is BulkEditEvent.ToggleCategoryFilter -> toggleCategory(event.categoryId)
            BulkEditEvent.ToggleUncategorizedFilter -> toggleUncategorized()
            is BulkEditEvent.ToggleTagFilter -> toggleTag(event.tagId)
            BulkEditEvent.ClearFilters -> clearFilters()
            is BulkEditEvent.ApplyCategoryChange -> applyCategoryChange(event.categoryId)
            is BulkEditEvent.ApplyAccountChange -> applyAccountChange(event.accountId)
            is BulkEditEvent.ApplyAddTag -> applyAddTag(event.tagId)
            is BulkEditEvent.ApplyRemoveTag -> applyRemoveTag(event.tagId)
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

    private fun toggleTag(tagId: UUID) {
        selectedTagIds.value = selectedTagIds.value.toggle(tagId)
        refilter()
    }

    private fun clearFilters() {
        selectedCategoryIds.value = emptySet()
        uncategorizedSelected.value = false
        selectedTagIds.value = emptySet()
        refilter()
    }

    private fun refilter() {
        viewModelScope.launch(Dispatchers.IO) {
            recompute()
        }
    }

    private fun applyCategoryChange(categoryId: UUID?) {
        val targets = matchingRaw
        if (targets.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            bulkUpdateTransactionsUseCase.updateCategory(
                transactions = targets,
                categoryId = categoryId?.let { CategoryId(it) }
            )
            _messages.send("已修改 ${targets.size} 笔交易的类别")
            reload()
        }
    }

    private fun applyAccountChange(accountId: UUID) {
        val targets = matchingRaw
        if (targets.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            val skipped = bulkUpdateTransactionsUseCase.updateAccount(
                transactions = targets,
                accountId = AccountId(accountId)
            )
            val moved = targets.size - skipped
            _messages.send(
                if (skipped > 0) {
                    "已移动 $moved 笔交易，跳过 $skipped 笔转账"
                } else {
                    "已将 $moved 笔交易移动到所选账户"
                }
            )
            reload()
        }
    }

    private fun applyAddTag(tagId: UUID) {
        val targets = matchingRaw
        if (targets.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            bulkUpdateTransactionsUseCase.addTag(targets, TagId(tagId))
            _messages.send("已为 ${targets.size} 笔交易添加标签")
            reload()
        }
    }

    private fun applyRemoveTag(tagId: UUID) {
        val targets = matchingRaw
        if (targets.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            bulkUpdateTransactionsUseCase.removeTag(targets, TagId(tagId))
            _messages.send("已为 ${targets.size} 笔交易移除标签")
            reload()
        }
    }
}

private fun Set<UUID>.toggle(id: UUID): Set<UUID> =
    if (contains(id)) this - id else this + id
