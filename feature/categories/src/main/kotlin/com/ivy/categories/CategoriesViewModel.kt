package com.ivy.categories

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.viewModelScope
import com.ivy.data.model.legacy.LegacyTransaction
import com.ivy.domain.preferences.toggles.PreferenceToggleService
import com.ivy.domain.preferences.toggles.PreferenceToggleCatalog
import com.ivy.domain.usecase.category.GetCategoriesUseCase
import com.ivy.domain.usecase.category.SaveCategoryUseCase
import com.ivy.domain.usecase.currency.GetBaseCurrencyCodeUseCase
import com.ivy.domain.usecase.transaction.GetLegacyTransactionsForAccountsUseCase
import com.ivy.ui.period.PeriodState
import com.ivy.data.model.legacy.LegacyAccount
import com.ivy.ui.ComposeViewModel
import com.ivy.ui.preferences.asEnabledState
import com.ivy.domain.usecase.account.GetLegacyAccountsUseCase
import com.ivy.domain.usecase.category.CalculateCategoryIncomeWithAccountFiltersUseCase
import com.ivy.domain.usecase.category.CreateCategoryUseCase
import com.ivy.domain.usecase.category.GetCategorySortOrderPreferenceUseCase
import com.ivy.domain.usecase.category.SetCategorySortOrderPreferenceUseCase
import com.ivy.data.model.CreateCategoryData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@Stable
@HiltViewModel
internal class CategoriesViewModel @Inject internal constructor(
    private val createCategoryUseCase: CreateCategoryUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val saveCategoryUseCase: SaveCategoryUseCase,
    private val periodState: PeriodState,
    private val getCategorySortOrderPreference: GetCategorySortOrderPreferenceUseCase,
    private val setCategorySortOrderPreference: SetCategorySortOrderPreferenceUseCase,
    private val getBaseCurrencyCode: GetBaseCurrencyCodeUseCase,
    private val getLegacyAccountsUseCase: GetLegacyAccountsUseCase,
    private val getLegacyTransactionsForAccountsUseCase: GetLegacyTransactionsForAccountsUseCase,
    private val calculateCategoryIncomeWithAccountFiltersUseCase: CalculateCategoryIncomeWithAccountFiltersUseCase,
    private val preferenceToggles: PreferenceToggleCatalog,
    private val preferenceToggleService: PreferenceToggleService,
) : ComposeViewModel<CategoriesScreenState, CategoriesScreenEvent>() {

    private val baseCurrency = mutableStateOf("")
    private val categories =
        mutableStateOf<ImmutableList<CategoryData>>(persistentListOf<CategoryData>())
    private val searchQuery = mutableStateOf("")
    private val reorderModalVisible = mutableStateOf(false)
    private val sortModalVisible = mutableStateOf(false)
    private val sortOrder = mutableStateOf(SortOrder.DEFAULT)

    @Composable
    override fun uiState(): CategoriesScreenState {
        LaunchedEffect(Unit) {
            start()
        }

        return CategoriesScreenState(
            baseCurrency = getBaseCurrency(),
            categories = getCategories(),
            reorderModalVisible = getReorderModalVisible(),
            sortOrder = getSortOrder(),
            sortModalVisible = getSortModalVisible(),
            compactCategoriesModeEnabled = getCompactCategoriesMode(),
            showCategorySearchBar = getShowCategorySearchBar()
        )
    }

    @Composable
    private fun getCompactCategoriesMode(): Boolean {
        val preference = preferenceToggles.compactCategoriesMode
        return preferenceToggleService.enabledFlow(preference)
            .asEnabledState(preference.defaultValue)
    }

    @Composable
    private fun getShowCategorySearchBar(): Boolean {
        val preference = preferenceToggles.showCategorySearchBar
        return preferenceToggleService.enabledFlow(preference)
            .asEnabledState(preference.defaultValue)
    }

    @Composable
    private fun getBaseCurrency(): String {
        return baseCurrency.value
    }

    @Composable
    private fun getCategories(): ImmutableList<CategoryData> {
        val allCats = categories.value
        return remember(allCats, searchQuery.value) {
            allCats.filter {
                searchQuery.value.lowercase().trim() in it.category.name.toString().lowercase()
            }.toImmutableList()
        }
    }

    @Composable
    private fun getReorderModalVisible(): Boolean {
        return reorderModalVisible.value
    }

    @Composable
    private fun getSortOrder(): SortOrder {
        return sortOrder.value
    }

    @Composable
    private fun getSortModalVisible(): Boolean {
        return sortModalVisible.value
    }

    private fun start() {
        viewModelScope.launch(Dispatchers.IO) {
            val input = initialise()
            loadCategories(input)
        }
    }

    private suspend fun initialise(): CategoryLoadInput {
        return withContext(Dispatchers.IO) {
            val range = periodState.rangeOf(periodState.currentMonth()) // this must be monthly

            val accounts = getLegacyAccountsUseCase()
            baseCurrency.value = getBaseCurrencyCode()

            val transactions = getLegacyTransactionsForAccountsUseCase(
                range = range,
                accountIdFilterSet = accounts.map { it.id }.toHashSet()
            )

            val sortOrder = SortOrder.from(
                getCategorySortOrderPreference()
            )

            this@CategoriesViewModel.sortOrder.value = sortOrder

            CategoryLoadInput(
                accounts = accounts,
                transactions = transactions
            )
        }
    }

    private suspend fun loadCategories(input: CategoryLoadInput) {
        withContext(Dispatchers.IO) {
            val scope = this
            val categories = getCategoriesUseCase().mapAsync(scope) {
                val catIncomeExpense = calculateCategoryIncomeWithAccountFiltersUseCase(
                    transactions = input.transactions,
                    accountFilterList = input.accounts,
                    category = it,
                    baseCurrency = baseCurrency.value
                )

                CategoryData(
                    category = it,
                    monthlyBalance = (catIncomeExpense.income - catIncomeExpense.expense).toDouble(),
                    monthlyIncome = catIncomeExpense.income.toDouble(),
                    monthlyExpenses = catIncomeExpense.expense.toDouble()
                )
            }

            val sortedList = sortList(categories, sortOrder.value).toImmutableList()
            this@CategoriesViewModel.categories.value = sortedList
        }
    }

    private fun updateSearchQuery(queryString: String) {
        searchQuery.value = queryString
    }

    private suspend fun reorder(
        newOrder: List<CategoryData>,
        sortOrder: SortOrder = SortOrder.DEFAULT
    ) {
        val sortedList = sortList(newOrder, sortOrder).toImmutableList()

        if (sortOrder == SortOrder.DEFAULT) {
            withContext(Dispatchers.IO) {
                sortedList.forEachIndexed { index, categoryData ->
                    saveCategoryUseCase(categoryData.category.copy(orderNum = index.toDouble()))
                }
            }
        }

        withContext(Dispatchers.IO) {
            setCategorySortOrderPreference(sortOrder.orderNum)
        }

        this.categories.value = sortedList
        this.sortOrder.value = sortOrder
    }

    private fun sortList(
        categoryData: List<CategoryData>,
        sortOrder: SortOrder
    ): List<CategoryData> {
        return when (sortOrder) {
            SortOrder.DEFAULT -> categoryData.sortedBy {
                it.category.orderNum
            }

            SortOrder.BALANCE_AMOUNT -> categoryData.sortedByDescending {
                it.monthlyBalance
            }.partition { it.monthlyBalance.toInt() != 0 } // Partition into non-zero and zero lists
                .let { (nonZero, zero) -> nonZero + zero }

            SortOrder.ALPHABETICAL -> categoryData.sortedBy {
                it.category.name.value
            }

            SortOrder.EXPENSES -> categoryData.sortedByDescending {
                it.monthlyExpenses
            }
        }
    }

    private suspend fun createCategory(data: CreateCategoryData) {
        if (createCategoryUseCase(data) != null) {
            val input = initialise()
            loadCategories(input)
        }
    }

    override fun onEvent(event: CategoriesScreenEvent) {
        viewModelScope.launch(Dispatchers.Default) {
            when (event) {
                is CategoriesScreenEvent.OnReorder -> reorder(event.newOrder, event.sortOrder)
                is CategoriesScreenEvent.OnCreateCategory -> createCategory(event.createCategoryData)
                is CategoriesScreenEvent.OnReorderModalVisible -> {
                    reorderModalVisible.value = event.visible
                }

                is CategoriesScreenEvent.OnSortOrderModalVisible -> {
                    sortModalVisible.value = event.visible
                }

                is CategoriesScreenEvent.OnSearchQueryUpdate -> updateSearchQuery(event.queryString)
            }
        }
    }
}

private data class CategoryLoadInput(
    val accounts: List<LegacyAccount>,
    val transactions: List<LegacyTransaction>,
)

suspend inline fun <T, R> Iterable<T>.mapAsync(
    scope: CoroutineScope,
    crossinline transform: suspend (T) -> R
): List<R> {
    return this.map {
        scope.async {
            transform(it)
        }
    }.awaitAll()
}
