package com.ivy.exchangerates

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.ivy.data.model.ExchangeRate
import com.ivy.data.model.primitive.AssetCode
import com.ivy.data.model.primitive.PositiveDouble
import com.ivy.domain.usecase.currency.GetBaseCurrencyUseCase
import com.ivy.domain.usecase.exchange.DeleteExchangeRateUseCase
import com.ivy.domain.usecase.exchange.ObserveExchangeRatesUseCase
import com.ivy.domain.usecase.exchange.SaveExchangeRateUseCase
import com.ivy.domain.usecase.exchange.SyncExchangeRatesUseCase
import com.ivy.exchangerates.data.RateUi
import com.ivy.ui.ComposeViewModel
import com.ivy.ui.platform.Toaster
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@Stable
@HiltViewModel
class ExchangeRatesViewModel @Inject constructor(
    private val syncExchangeRatesUseCase: SyncExchangeRatesUseCase,
    private val getBaseCurrency: GetBaseCurrencyUseCase,
    private val observeExchangeRatesUseCase: ObserveExchangeRatesUseCase,
    private val saveExchangeRateUseCase: SaveExchangeRateUseCase,
    private val deleteExchangeRateUseCase: DeleteExchangeRateUseCase,
    private val toaster: Toaster,
) : ComposeViewModel<RatesState, RatesEvent>() {
    private var searchQuery by mutableStateOf("")
    private var baseCurrency by mutableStateOf<AssetCode?>(null)

    private fun toUi(exchangeRate: ExchangeRate): RateUi = RateUi(
        from = exchangeRate.baseCurrency.code,
        to = exchangeRate.currency.code,
        rate = exchangeRate.rate.value
    )

    @Composable
    override fun uiState(): RatesState {
        LaunchedEffect(Unit) {
            baseCurrency = getBaseCurrency().also {
                viewModelScope.launch {
                    syncExchangeRatesUseCase.sync(it)
                }
            }
        }

        val rates = getRates()

        return RatesState(
            baseCurrency = baseCurrency?.code ?: "",
            manual = rates.filter { it.manualOverride }.map(::toUi).toImmutableList(),
            automatic = rates.filter { !it.manualOverride }.map(::toUi).toImmutableList()
        )
    }

    @Composable
    private fun getRates(): List<ExchangeRate> {
        val rates by remember { observeExchangeRatesUseCase() }
            .collectAsState(initial = emptyList())

        return rates.filter {
            if (searchQuery.isNotBlank()) {
                it.currency.code.contains(searchQuery, ignoreCase = true)
            } else {
                true
            }
        }.filter { baseCurrency == it.baseCurrency }
    }

    // region Event Handling
    override fun onEvent(event: RatesEvent) {
        viewModelScope.launch {
            when (event) {
                is RatesEvent.RemoveOverride -> handleRemoveOverride(event)
                is RatesEvent.Search -> handleSearch(event)
                is RatesEvent.UpdateRate -> handleUpdateRate(event)
                is RatesEvent.AddRate -> handleAddRate(event)
            }
        }
    }

    private suspend fun handleRemoveOverride(event: RatesEvent.RemoveOverride) {
        withContext(Dispatchers.IO) {
            val baseCurrency = AssetCode.from(event.rate.from)
                .fold({ toaster.show(it); return@withContext }, { it })
            val currency = AssetCode.from(event.rate.to)
                .fold({ toaster.show(it); return@withContext }, { it })

            deleteExchangeRateUseCase(
                baseCurrency = baseCurrency,
                currency = currency
            )
            this@ExchangeRatesViewModel.baseCurrency?.let { syncExchangeRatesUseCase.sync(it) }
        }
    }

    private fun handleSearch(event: RatesEvent.Search) {
        searchQuery = event.query.trim()
    }

    private suspend fun handleUpdateRate(event: RatesEvent.UpdateRate) {
        withContext(Dispatchers.IO) {
            val baseCurrency = AssetCode.from(event.rate.from)
                .fold({ toaster.show(it); return@withContext }, { it })
            val currency = AssetCode.from(event.rate.to)
                .fold({ toaster.show(it); return@withContext }, { it })
            val rate = PositiveDouble.from(event.newRate)
                .fold({ toaster.show(it); return@withContext }, { it })

            saveExchangeRateUseCase(
                ExchangeRate(
                    baseCurrency = baseCurrency,
                    currency = currency,
                    rate = rate,
                    manualOverride = true
                )
            )
        }
    }

    private suspend fun handleAddRate(event: RatesEvent.AddRate) {
        withContext(Dispatchers.IO) {
            val baseCurrency = AssetCode.from(event.rate.from)
                .fold({ toaster.show(it); return@withContext }, { it })
            val currency = AssetCode.from(event.rate.to)
                .fold({ toaster.show(it); return@withContext }, { it })
            val rate = PositiveDouble.from(event.rate.rate)
                .fold({ toaster.show(it); return@withContext }, { it })

            saveExchangeRateUseCase(
                ExchangeRate(
                    baseCurrency = baseCurrency,
                    currency = currency,
                    rate = rate,
                    manualOverride = true
                )
            )
        }
    }
    // endregion
}
