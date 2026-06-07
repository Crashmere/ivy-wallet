package com.ivy.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivy.domain.usecase.account.CreateAccountWithBalanceUseCase
import com.ivy.domain.usecase.currency.GetBaseCurrencyUseCase
import com.ivy.domain.usecase.exchange.SyncExchangeRatesUseCase
import com.ivy.ui.navigation.MainTab
import com.ivy.ui.navigation.MainScreen
import com.ivy.ui.navigation.MainTabState
import com.ivy.ui.navigation.Navigation
import com.ivy.data.model.CreateAccountData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val mainTabState: MainTabState,
    private val nav: Navigation,
    private val syncExchangeRatesUseCase: SyncExchangeRatesUseCase,
    private val createAccountWithBalanceUseCase: CreateAccountWithBalanceUseCase,
    private val getBaseCurrency: GetBaseCurrencyUseCase,
) : ViewModel() {

    private val _currency = MutableStateFlow("")
    val currency: StateFlow<String> = _currency.asStateFlow()

    fun start(screen: MainScreen) {
        nav.registerScreenBackHandler(screen) {
            if (mainTabState.selectedTab == MainTab.ACCOUNTS) {
                mainTabState.select(MainTab.HOME)
                true
            } else {
                // Exiting (the backstack will close the app)
                false
            }
        }

        viewModelScope.launch {

            val baseCurrency = getBaseCurrency()
            _currency.value = baseCurrency.code

            withContext(Dispatchers.IO) {
                // Sync exchange rates
                syncExchangeRatesUseCase.sync(baseCurrency)
            }

        }
    }

    fun selectedTab(): MainTab = mainTabState.selectedTab

    fun selectTab(tab: MainTab) {
        mainTabState.select(tab)
    }

    fun createAccount(data: CreateAccountData) {
        viewModelScope.launch {
            createAccountWithBalanceUseCase(data)
        }
    }
}
