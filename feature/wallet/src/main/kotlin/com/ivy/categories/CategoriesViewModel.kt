package com.ivy.categories

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.viewModelScope
import com.ivy.domain.preferences.toggles.PreferenceToggleService
import com.ivy.domain.preferences.toggles.PreferenceToggleCatalog
import com.ivy.domain.usecase.category.SaveCategoryUseCase
import com.ivy.domain.usecase.currency.GetBaseCurrencyCodeUseCase
import com.ivy.ui.period.PeriodState
import com.ivy.ui.period.TimePeriod
import com.ivy.ui.ComposeViewModel
import com.ivy.ui.preferences.asEnabledState
import com.ivy.domain.usecase.category.CreateCategoryUseCase
import com.ivy.domain.usecase.category.GetCategorySortOrderPreferenceUseCase
import com.ivy.domain.usecase.category.GetCategoryMonthlyStatsUseCase
import com.ivy.domain.usecase.category.SetCategorySortOrderPreferenceUseCase
import com.ivy.domain.usecase.account.GetAccountsUseCase
import com.ivy.data.model.CreateCategoryData
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
internal class CategoriesViewModel @Inject internal constructor(
    private val createCategoryUseCase: CreateCategoryUseCase,
    private val saveCategoryUseCase: SaveCategoryUseCase,
    private val periodState: PeriodState,
    private val getCategorySortOrderPreference: GetCategorySortOrderPreferenceUseCase,
    private val setCategorySortOrderPreference: SetCategorySortOrderPreferenceUseCase,
    private val getBaseCurrencyCode: GetBaseCurrencyCodeUseCase,
    private val getCategoryMonthlyStatsUseCase: GetCategoryMonthlyStatsUseCase,
    private val getAccountsUseCase: GetAccountsUseCase,
    private val preferenceToggles: PreferenceToggleCatalog,
    private val preferenceToggleService: PreferenceToggleService,
) : ComposeViewModel<CategoriesScreenState, CategoriesScreenEvent>() {

    private val baseCurrency = mutableStateOf("")
    private val categories =
        mutableStateOf<ImmutableList<CategoryData>>(persistentListOf<CategoryData>())
    private val accounts =
        mutableStateOf<ImmutableList<CategoryAccountHeader>>(persistentListOf())
    private val searchQuery = mutableStateOf("")
    private val reorderModalVisible = mutableStateOf(false)
    private val sortModalVisible = mutableStateOf(false)
    private val sortOrder = mutableStateOf(SortOrder.DEFAULT)

    // Local to this screen: defaults to the current month and is switched via the
    // month selector. Kept independent from the global/home period on purpose.
    private val selectedPeriod = mutableStateOf(periodState.currentMonth())

    @Composable
    override fun uiState(): CategoriesScreenState {
        LaunchedEffect(Unit) {
            start()
        }

        return CategoriesScreenState(
            baseCurrency = getBaseCurrency(),
            categories = getCategories(),
            accounts = getAccounts(),
            reorderModalVisible = getReorderModalVisible(),
            sortOrder = getSortOrder(),
            sortModalVisible = getSortModalVisible(),
            compactCategoriesModeEnabled = getCompactCategoriesMode(),
            showCategorySearchBar = getShowCategorySearchBar(),
            period = getPeriod()
        )
    }

    @Composable
    private fun getPeriod(): TimePeriod {
        return selectedPeriod.value
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
    private fun getAccounts(): ImmutableList<CategoryAccountHeader> {
        return accounts.value
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
            initialise()
            loadCategories()
        }
    }

    private suspend fun initialise() {
        withContext(Dispatchers.IO) {
            baseCurrency.value = getBaseCurrencyCode()

            val sortOrder = SortOrder.from(
                getCategorySortOrderPreference()
            )

            this@CategoriesViewModel.sortOrder.value = sortOrder
        }
    }

    private suspend fun loadCategories() {
        withContext(Dispatchers.IO) {
            val monthlyRange = periodState.rangeOf(selectedPeriod.value)
            val categories = getCategoryMonthlyStatsUseCase(
                range = monthlyRange,
                baseCurrency = baseCurrency.value
            ).map {
                CategoryData(
                    category = it.category,
                    monthlyBalance = it.balance,
                    monthlyIncome = it.income,
                    monthlyExpenses = it.expenses,
                    monthlyCount = it.count
                )
            }

            val sortedList = sortList(categories, sortOrder.value).toImmutableList()
            this@CategoriesViewModel.categories.value = sortedList
            loadAccounts()
        }
    }

    private suspend fun loadAccounts() {
        accounts.value = getAccountsUseCase()
            .sortedBy { it.orderNum }
            .map { account ->
                CategoryAccountHeader(
                    id = account.id.value,
                    name = account.name.value,
                    color = account.color.value,
                    icon = account.icon?.id,
                    orderNum = account.orderNum,
                    categoryIds = account.visibleCategories.mapTo(mutableSetOf()) { it.value },
                )
            }
            .toImmutableList()
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
            initialise()
            loadCategories()
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
                is CategoriesScreenEvent.OnNextMonth -> shiftSelectedMonth(increment = 1L)
                is CategoriesScreenEvent.OnPreviousMonth -> shiftSelectedMonth(increment = -1L)
                is CategoriesScreenEvent.OnSelectPeriod -> selectPeriod(event.period)
            }
        }
    }

    private suspend fun shiftSelectedMonth(increment: Long) {
        val shifted = periodState.shiftMonth(selectedPeriod.value, increment) ?: return
        selectedPeriod.value = shifted
        loadCategories()
    }

    private suspend fun selectPeriod(period: TimePeriod) {
        selectedPeriod.value = period
        loadCategories()
    }
}
