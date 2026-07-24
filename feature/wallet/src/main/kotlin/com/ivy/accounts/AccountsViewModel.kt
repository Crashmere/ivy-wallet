package com.ivy.accounts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.ivy.data.model.AccountId
import com.ivy.domain.preferences.toggles.PreferenceToggleService
import com.ivy.domain.preferences.toggles.PreferenceToggleCatalog
import com.ivy.domain.usecase.account.GetAccountsUseCase
import com.ivy.domain.usecase.account.ObserveAccountChangesUseCase
import com.ivy.domain.usecase.account.SaveAccountUseCase
import com.ivy.domain.usecase.currency.GetBaseCurrencyCodeUseCase
import com.ivy.domain.usecase.settings.GetTransfersAsIncomeExpensePreferenceUseCase
import com.ivy.ui.period.PeriodState
import com.ivy.data.model.ClosedTimeRange
import com.ivy.data.model.toCloseTimeRange
import com.ivy.ui.ComposeViewModel
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
    private var netWorth by mutableStateOf(0.0)
    private var netWorthChange by mutableStateOf(0.0)

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
            netWorth = getNetWorth(),
            netWorthChange = netWorthChange,
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
    private fun getNetWorth(): Double {
        return netWorth
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

        val totalBalance = calculateWalletBalanceUseCase(
            baseCurrency = baseCurrencyCode
        ).toDouble()

        val startOfMonth = monthlyRange.toCloseTimeRange().from
        val netWorthLastMonth = calculateWalletBalanceUseCase(
            baseCurrency = baseCurrencyCode,
            range = ClosedTimeRange.to(startOfMonth)
        ).toDouble()

        baseCurrency = baseCurrencyCode
        netWorth = totalBalance
        netWorthChange = totalBalance - netWorthLastMonth
        accountsData = accountsDataList
    }
}
