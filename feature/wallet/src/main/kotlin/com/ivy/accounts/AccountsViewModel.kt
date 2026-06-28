package com.ivy.accounts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.ivy.ui.resource.ResourceProvider
import com.ivy.data.model.AccountId
import com.ivy.domain.preferences.toggles.PreferenceToggleService
import com.ivy.domain.preferences.toggles.PreferenceToggleCatalog
import com.ivy.domain.usecase.account.GetAccountsUseCase
import com.ivy.domain.usecase.account.ObserveAccountChangesUseCase
import com.ivy.domain.usecase.account.SaveAccountUseCase
import com.ivy.domain.usecase.currency.GetBaseCurrencyCodeUseCase
import com.ivy.domain.usecase.settings.GetTransfersAsIncomeExpensePreferenceUseCase
import com.ivy.ui.period.PeriodState
import com.ivy.data.model.toCloseTimeRange
import com.ivy.data.model.currency.format
import com.ivy.ui.ComposeViewModel
import com.ivy.ui.R
import com.ivy.ui.preferences.asEnabledState
import com.ivy.domain.usecase.wallet.CalculateWalletBalanceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@Stable
@HiltViewModel
internal class AccountsViewModel @Inject internal constructor(
    private val resourceProvider: ResourceProvider,
    private val periodState: PeriodState,
    private val getTransfersAsIncomeExpensePreference: GetTransfersAsIncomeExpensePreferenceUseCase,
    private val calculateWalletBalanceUseCase: CalculateWalletBalanceUseCase,
    private val getBaseCurrencyCode: GetBaseCurrencyCodeUseCase,
    private val getAccountsUseCase: GetAccountsUseCase,
    private val saveAccountUseCase: SaveAccountUseCase,
    private val buildAccountDataUseCase: BuildAccountDataUseCase,
    private val observeAccountChangesUseCase: ObserveAccountChangesUseCase,
    private val preferenceToggles: PreferenceToggleCatalog,
    private val preferenceToggleService: PreferenceToggleService,
) : ComposeViewModel<AccountsState, AccountsEvent>() {
    private var baseCurrency by mutableStateOf("")
    private var accountsData by mutableStateOf(listOf<AccountData>())
    private var totalBalanceWithExcluded by mutableStateOf("")
    private var totalBalanceWithExcludedText by mutableStateOf("")
    private var totalBalanceWithoutExcluded by mutableStateOf("")
    private var totalBalanceWithoutExcludedText by mutableStateOf("")
    private var reorderVisible by mutableStateOf(false)

    init {
        viewModelScope.launch {
            observeAccountChangesUseCase().collectLatest {
                onStart()
            }
        }
    }

    @Composable
    override fun uiState(): AccountsState {
        LaunchedEffect(Unit) {
            onStart()
        }

        return AccountsState(
            baseCurrency = getBaseCurrency(),
            accountsData = getAccountsData(),
            totalBalanceWithExcluded = getTotalBalanceWithExcluded(),
            totalBalanceWithExcludedText = getTotalBalanceWithExcludedText(),
            totalBalanceWithoutExcluded = getTotalBalanceWithoutExcluded(),
            totalBalanceWithoutExcludedText = getTotalBalanceWithoutExcludedText(),
            reorderVisible = getReorderVisible(),
            compactAccountsModeEnabled = getCompactAccountsMode(),
            hideTotalBalance = getHideTotalBalance()
        )
    }

    @Composable
    private fun getHideTotalBalance(): Boolean {
        val preference = preferenceToggles.hideTotalBalance
        return preferenceToggleService.enabledFlow(preference)
            .asEnabledState(preference.defaultValue)
    }

    @Composable
    private fun getBaseCurrency(): String {
        return baseCurrency
    }

    @Composable
    private fun getAccountsData(): ImmutableList<AccountData> {
        return accountsData.toImmutableList()
    }

    @Composable
    private fun getTotalBalanceWithExcluded(): String {
        return totalBalanceWithExcluded
    }

    @Composable
    private fun getTotalBalanceWithExcludedText(): String {
        return totalBalanceWithExcludedText
    }

    @Composable
    private fun getTotalBalanceWithoutExcluded(): String {
        return totalBalanceWithoutExcluded
    }

    @Composable
    private fun getTotalBalanceWithoutExcludedText(): String {
        return totalBalanceWithoutExcludedText
    }

    @Composable
    private fun getReorderVisible(): Boolean {
        return reorderVisible
    }

    @Composable
    private fun getCompactAccountsMode(): Boolean {
        val preference = preferenceToggles.compactAccountsMode
        return preferenceToggleService.enabledFlow(preference)
            .asEnabledState(preference.defaultValue)
    }

    override fun onEvent(event: AccountsEvent) {
        viewModelScope.launch(Dispatchers.Default) {
            when (event) {
                is AccountsEvent.OnReorder -> reorder(event.accountIds)
                is AccountsEvent.OnReorderModalVisible -> reorderModalVisible(event.reorderVisible)
            }
        }
    }

    private suspend fun reorder(accountIds: List<AccountId>) {
        val reorderedAccounts = accountIds.mapNotNull { accountId ->
            accountsData.firstOrNull { it.account.id == accountId }?.account
        }
        withContext(Dispatchers.IO) {
            reorderedAccounts.mapIndexed { index, account ->
                saveAccountUseCase(account.copy(orderNum = index.toDouble()))
            }
        }

        startInternally()
    }

    private fun onStart() {
        viewModelScope.launch(Dispatchers.Default) {
            startInternally()
        }
    }

    private suspend fun startInternally() {
        val monthlyRange = periodState.rangeOf(periodState.currentMonth())

        val baseCurrencyCode = getBaseCurrencyCode()
        val accounts = getAccountsUseCase().toImmutableList()

        val includeTransfersInCalc = getTransfersAsIncomeExpensePreference()

        val accountsDataList = buildAccountDataUseCase(
            accounts = accounts,
            range = monthlyRange.toCloseTimeRange(),
            baseCurrency = baseCurrencyCode,
            includeTransfersInCalc = includeTransfersInCalc
        )

        val totalBalanceWithExcludedAccounts = calculateWalletBalanceUseCase(
            baseCurrency = baseCurrencyCode,
            withExcluded = true
        ).toDouble()

        val totalBalanceWithoutExcludedAccounts = calculateWalletBalanceUseCase(
            baseCurrency = baseCurrencyCode
        ).toDouble()

        baseCurrency = baseCurrencyCode
        accountsData = accountsDataList
        totalBalanceWithExcluded = totalBalanceWithExcludedAccounts.toString()
        totalBalanceWithExcludedText = resourceProvider.getString(
            R.string.total,
            baseCurrencyCode,
            totalBalanceWithExcludedAccounts.format(
                baseCurrencyCode
            )
        )
        totalBalanceWithoutExcluded = totalBalanceWithoutExcludedAccounts.toString()
        totalBalanceWithoutExcludedText = resourceProvider.getString(
            R.string.total_exclusive,
            baseCurrencyCode,
            totalBalanceWithoutExcludedAccounts.format(
                baseCurrencyCode
            )
        )
    }

    private fun reorderModalVisible(visible: Boolean) {
        reorderVisible = visible
    }
}
