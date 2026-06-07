package com.ivy.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivy.data.repository.CurrencyRepository
import com.ivy.domain.usecase.exchange.SyncExchangeRatesUseCase
import com.ivy.frp.test.TestIdlingResource
import com.ivy.legacy.domain.logic.AccountCreator
import com.ivy.base.legacy.asLiveData
import com.ivy.base.legacy.ioThread
import com.ivy.navigation.MainTab
import com.ivy.navigation.MainScreen
import com.ivy.navigation.MainTabState
import com.ivy.navigation.Navigation
import com.ivy.legacy.domain.model.CreateAccountData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val mainTabState: MainTabState,
    private val nav: Navigation,
    private val syncExchangeRatesUseCase: SyncExchangeRatesUseCase,
    private val accountCreator: AccountCreator,
    private val currencyRepository: CurrencyRepository,
) : ViewModel() {

    private val _currency = MutableLiveData<String>()
    val currency = _currency.asLiveData()

    fun start(screen: MainScreen) {
        nav.onBackPressed[screen] = {
            if (mainTabState.selectedTab == MainTab.ACCOUNTS) {
                mainTabState.select(MainTab.HOME)
                true
            } else {
                // Exiting (the backstack will close the app)
                false
            }
        }

        viewModelScope.launch {
            TestIdlingResource.increment()

            val baseCurrency = currencyRepository.getBaseCurrency()
            _currency.value = baseCurrency.code

            ioThread {
                // Sync exchange rates
                syncExchangeRatesUseCase.sync(baseCurrency)
            }

            TestIdlingResource.decrement()
        }
    }

    fun selectedTab(): MainTab = mainTabState.selectedTab

    fun selectTab(tab: MainTab) {
        mainTabState.select(tab)
    }

    fun createAccount(data: CreateAccountData) {
        viewModelScope.launch {
            TestIdlingResource.increment()

            accountCreator.createAccount(data) {}

            TestIdlingResource.decrement()
        }
    }
}
