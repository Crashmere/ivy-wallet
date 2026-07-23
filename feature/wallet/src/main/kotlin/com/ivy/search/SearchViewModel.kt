package com.ivy.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.ivy.data.model.Account
import com.ivy.data.model.Transaction
import com.ivy.data.model.TransactionHistoryItem
import com.ivy.data.model.Transfer
import com.ivy.data.model.getFromAccount
import com.ivy.data.model.primitive.NotBlankTrimmedString
import com.ivy.ui.ComposeViewModel
import com.ivy.data.model.Category
import com.ivy.data.model.Tag
import com.ivy.domain.preferences.toggles.PreferenceToggleService
import com.ivy.domain.preferences.toggles.PreferenceToggleCatalog
import com.ivy.domain.usecase.category.GetCategoriesUseCase
import com.ivy.domain.usecase.currency.GetBaseCurrencyCodeUseCase
import com.ivy.data.model.currency.getDefaultFIATCurrency
import com.ivy.domain.usecase.account.GetAccountsUseCase
import com.ivy.domain.usecase.tag.GetTagsUseCase
import com.ivy.domain.usecase.transaction.BuildTransactionHistoryItemsUseCase
import com.ivy.domain.usecase.transaction.GetTransactionsUseCase
import com.ivy.ui.period.PeriodState
import com.ivy.ui.preferences.asEnabledState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.inject.Inject

@Stable
@HiltViewModel
internal class SearchViewModel @Inject internal constructor(
    private val buildTransactionHistoryItemsUseCase: BuildTransactionHistoryItemsUseCase,
    private val getAccountsUseCase: GetAccountsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getTagsUseCase: GetTagsUseCase,
    private val getBaseCurrencyCode: GetBaseCurrencyCodeUseCase,
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val periodState: PeriodState,
    private val preferenceToggleService: PreferenceToggleService,
    private val preferenceToggles: PreferenceToggleCatalog
) : ComposeViewModel<SearchState, SearchEvent>() {

    private val transactions =
        mutableStateOf<ImmutableList<TransactionHistoryItem>>(persistentListOf())
    private val baseCurrency = mutableStateOf<String>(getDefaultFIATCurrency().currencyCode)
    private val accounts = mutableStateOf<ImmutableList<SearchAccount>>(persistentListOf())
    private val categories = mutableStateOf<ImmutableList<Category>>(persistentListOf())
    private val tags = mutableStateOf<ImmutableList<Tag>>(persistentListOf())
    private val searchQuery = mutableStateOf("")

    private val selectedCategoryIds = mutableStateOf<Set<UUID>>(emptySet())
    private val uncategorizedSelected = mutableStateOf(false)
    private val selectedAccountIds = mutableStateOf<Set<UUID>>(emptySet())
    private val selectedTagIds = mutableStateOf<Set<UUID>>(emptySet())
    private val timeFilter = mutableStateOf(SearchTimeFilter.ALL)

    @Composable
    fun getShouldShowAccountSpecificColorInTransactions(): Boolean {
        val preference = preferenceToggles.showAccountColorsInTransactions
        return preferenceToggleService.enabledFlow(preference)
            .asEnabledState(preference.defaultValue)
    }

    @Composable
    override fun uiState(): SearchState {
        LaunchedEffect(Unit) {
            search(searchQuery.value)
        }

        return SearchState(
            searchQuery = searchQuery.value,
            transactions = transactions.value,
            baseCurrency = baseCurrency.value,
            accounts = accounts.value,
            categories = categories.value,
            tags = tags.value,
            selectedCategoryIds = selectedCategoryIds.value.toImmutableSet(),
            uncategorizedSelected = uncategorizedSelected.value,
            selectedAccountIds = selectedAccountIds.value.toImmutableSet(),
            selectedTagIds = selectedTagIds.value.toImmutableSet(),
            timeFilter = timeFilter.value,
            shouldShowAccountSpecificColorInTransactions = getShouldShowAccountSpecificColorInTransactions()
        )
    }

    override fun onEvent(event: SearchEvent) {
        when (event) {
            is SearchEvent.Search -> search(event.query)
            is SearchEvent.ToggleCategory -> {
                selectedCategoryIds.value = selectedCategoryIds.value.toggle(event.categoryId)
                search(searchQuery.value)
            }

            SearchEvent.ToggleUncategorized -> {
                uncategorizedSelected.value = !uncategorizedSelected.value
                search(searchQuery.value)
            }

            is SearchEvent.ToggleAccount -> {
                selectedAccountIds.value = selectedAccountIds.value.toggle(event.accountId)
                search(searchQuery.value)
            }

            is SearchEvent.ToggleTag -> {
                selectedTagIds.value = selectedTagIds.value.toggle(event.tagId)
                search(searchQuery.value)
            }

            is SearchEvent.SetTimeFilter -> {
                timeFilter.value = event.filter
                search(searchQuery.value)
            }

            SearchEvent.ClearFilters -> {
                selectedCategoryIds.value = emptySet()
                uncategorizedSelected.value = false
                selectedAccountIds.value = emptySet()
                selectedTagIds.value = emptySet()
                timeFilter.value = SearchTimeFilter.ALL
                search(searchQuery.value)
            }
        }
    }

    private fun search(query: String) {
        searchQuery.value = query
        val normalizedQuery = query.lowercase().trim()

        viewModelScope.launch {
            val queryResult = withContext(Dispatchers.IO) {
                val textFiltered = getTransactionsUseCase()
                    .filter { transaction ->
                        transaction.title.matchesQuery(normalizedQuery) ||
                                transaction.description.matchesQuery(normalizedQuery)
                    }
                val filtered = applyFilters(textFiltered)
                buildTransactionHistoryItemsUseCase(
                    baseCurrency = getBaseCurrencyCode(),
                    transactions = filtered
                ).toImmutableList()
            }

            transactions.value = queryResult
            baseCurrency.value = getBaseCurrencyCode()
            accounts.value = getAccountsUseCase()
                .map { it.toSearchAccount() }
                .toImmutableList()
            categories.value = getCategoriesUseCase().toImmutableList()
            tags.value = getTagsUseCase().toImmutableList()
        }
    }

    private fun applyFilters(transactions: List<Transaction>): List<Transaction> {
        val categoryIds = selectedCategoryIds.value
        val includeUncategorized = uncategorizedSelected.value
        val accountIds = selectedAccountIds.value
        val tagIds = selectedTagIds.value

        val categoryFilterActive = categoryIds.isNotEmpty() || includeUncategorized
        val accountFilterActive = accountIds.isNotEmpty()
        val tagFilterActive = tagIds.isNotEmpty()

        var result = applyTimeFilter(transactions)

        if (categoryFilterActive) {
            result = result.filter { transaction ->
                val txCategoryId = transaction.category?.value
                if (txCategoryId == null) includeUncategorized else txCategoryId in categoryIds
            }
        }
        if (accountFilterActive) {
            result = result.filter { transaction ->
                transaction.getFromAccount().value in accountIds ||
                        (transaction is Transfer && transaction.toAccount.value in accountIds)
            }
        }
        if (tagFilterActive) {
            result = result.filter { transaction ->
                transaction.tags.any { it.value in tagIds }
            }
        }
        return result
    }

    private fun applyTimeFilter(transactions: List<Transaction>): List<Transaction> {
        return when (timeFilter.value) {
            SearchTimeFilter.ALL -> transactions
            SearchTimeFilter.THIS_MONTH -> {
                val range = periodState.rangeOf(periodState.currentMonth())
                transactions.filter { range.includes(it.time) }
            }

            SearchTimeFilter.LAST_MONTH -> {
                val period = periodState.shiftMonth(periodState.currentMonth(), -1)
                    ?: return transactions
                val range = periodState.rangeOf(period)
                transactions.filter { range.includes(it.time) }
            }

            SearchTimeFilter.LAST_3_MONTHS -> {
                val from = Instant.now().minus(90, ChronoUnit.DAYS)
                transactions.filter { !it.time.isBefore(from) }
            }

            SearchTimeFilter.THIS_YEAR -> {
                val from = java.time.LocalDate.now()
                    .withDayOfYear(1)
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                transactions.filter { !it.time.isBefore(from) }
            }
        }
    }

    private fun NotBlankTrimmedString?.matchesQuery(query: String): Boolean {
        return this?.value?.lowercase()?.contains(query) == true
    }
}

private fun Set<UUID>.toggle(id: UUID): Set<UUID> =
    if (contains(id)) this - id else this + id

private fun Account.toSearchAccount() = SearchAccount(
    id = id.value,
    name = name.value,
    color = color.value,
    icon = icon?.id,
    currency = asset.code,
)
