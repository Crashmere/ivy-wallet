package com.ivy.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivy.domain.usecase.account.CreateAccountWithBalanceUseCase
import com.ivy.domain.usecase.currency.GetBaseCurrencyUseCase
import com.ivy.domain.usecase.exchange.SyncExchangeRatesUseCase
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
    private val syncExchangeRatesUseCase: SyncExchangeRatesUseCase,
    private val createAccountWithBalanceUseCase: CreateAccountWithBalanceUseCase,
    private val getBaseCurrency: GetBaseCurrencyUseCase,
) : ViewModel() {

    var selectedTab by mutableStateOf(MainTab.HOME)
        private set

    private val _currency = MutableStateFlow("")
    val currency: StateFlow<String> = _currency.asStateFlow()

    fun start() {
        viewModelScope.launch {

            val baseCurrency = getBaseCurrency()
            _currency.value = baseCurrency.code

            withContext(Dispatchers.IO) {
                // Sync exchange rates
                syncExchangeRatesUseCase.sync(baseCurrency)
            }

        }
    }

    fun handleBack(): Boolean {
        return if (selectedTab == MainTab.ACCOUNTS) {
            selectTab(MainTab.HOME)
            true
        } else {
            // Exiting (the backstack will close the app)
            false
        }
    }

    fun selectTab(tab: MainTab) {
        selectedTab = tab
    }

    fun createAccount(data: CreateAccountData) {
        viewModelScope.launch {
            createAccountWithBalanceUseCase(data)
        }
    }
}
