package com.ivy.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.ivy.data.model.TransactionHistoryItem
import com.ivy.data.model.primitive.NotBlankTrimmedString
import com.ivy.ui.ComposeViewModel
import com.ivy.data.model.Category
import com.ivy.domain.preferences.toggles.PreferenceToggleService
import com.ivy.domain.preferences.toggles.PreferenceToggleCatalog
import com.ivy.domain.usecase.category.GetCategoriesUseCase
import com.ivy.domain.usecase.currency.GetBaseCurrencyCodeUseCase
import com.ivy.data.model.legacy.LegacyAccount
import com.ivy.data.model.currency.getDefaultFIATCurrency
import com.ivy.domain.usecase.account.GetLegacyAccountsUseCase
import com.ivy.domain.usecase.transaction.BuildTransactionHistoryItemsUseCase
import com.ivy.domain.usecase.transaction.GetTransactionsUseCase
import com.ivy.ui.preferences.asEnabledState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@Stable
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val buildTransactionHistoryItemsUseCase: BuildTransactionHistoryItemsUseCase,
    private val getLegacyAccountsUseCase: GetLegacyAccountsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getBaseCurrencyCode: GetBaseCurrencyCodeUseCase,
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val preferenceToggleService: PreferenceToggleService,
    private val preferenceToggles: PreferenceToggleCatalog
) : ComposeViewModel<SearchState, SearchEvent>() {

    private val transactions =
        mutableStateOf<ImmutableList<TransactionHistoryItem>>(persistentListOf())
    private val baseCurrency = mutableStateOf<String>(getDefaultFIATCurrency().currencyCode)
    private val accounts = mutableStateOf<ImmutableList<LegacyAccount>>(persistentListOf())
    private val categories = mutableStateOf<ImmutableList<Category>>(persistentListOf())
    private val searchQuery = mutableStateOf("")

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
            shouldShowAccountSpecificColorInTransactions = getShouldShowAccountSpecificColorInTransactions()
        )
    }

    override fun onEvent(event: SearchEvent) {
        when (event) {
            is SearchEvent.Search -> search(event.query)
        }
    }

    private fun search(query: String) {
        searchQuery.value = query
        val normalizedQuery = query.lowercase().trim()

        viewModelScope.launch {
            val queryResult = withContext(Dispatchers.IO) {
                val filteredTransactions = getTransactionsUseCase()
                    .filter { transaction ->
                        transaction.title.matchesQuery(normalizedQuery) ||
                                transaction.description.matchesQuery(normalizedQuery)
                    }
                buildTransactionHistoryItemsUseCase(
                    baseCurrency = getBaseCurrencyCode(),
                    transactions = filteredTransactions
                ).toImmutableList()
            }

            transactions.value = queryResult
            baseCurrency.value = getBaseCurrencyCode()
            accounts.value = getLegacyAccountsUseCase()
            categories.value = getCategoriesUseCase().toImmutableList()
        }
    }

    private fun NotBlankTrimmedString?.matchesQuery(query: String): Boolean {
        return this?.value?.lowercase()?.contains(query) == true
    }
}
