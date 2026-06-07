package com.ivy.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivy.base.coroutines.ioThread
import com.ivy.domain.usecase.currency.GetBaseCurrencyUseCase
import com.ivy.domain.usecase.exchange.SyncExchangeRatesUseCase
import com.ivy.legacy.domain.logic.AccountCreator
import com.ivy.ui.navigation.MainTab
import com.ivy.ui.navigation.MainScreen
import com.ivy.ui.navigation.MainTabState
import com.ivy.ui.navigation.Navigation
import com.ivy.data.model.legacy.CreateAccountData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val mainTabState: MainTabState,
    private val nav: Navigation,
    private val syncExchangeRatesUseCase: SyncExchangeRatesUseCase,
    private val accountCreator: AccountCreator,
    private val getBaseCurrency: GetBaseCurrencyUseCase,
) : ViewModel() {

    private val _currency = MutableLiveData<String>()
    val currency: LiveData<String> = _currency

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

            ioThread {
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

            accountCreator.createAccount(data) {}

        }
    }
}
