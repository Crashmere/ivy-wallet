package com.ivy.domain.usecase.exchange

import arrow.core.Either
import arrow.core.right
import com.ivy.data.api.ExchangeRateStore
import com.ivy.data.model.ExchangeRate
import com.ivy.data.model.primitive.AssetCode
import com.ivy.data.model.primitive.PositiveDouble
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SyncExchangeRatesUseCaseTest {

    private val store = FakeExchangeRateStore(
        eurRates = listOf(
            exchangeRate(baseCurrency = AssetCode.EUR, currency = AssetCode.EUR, rate = 1.0),
            exchangeRate(baseCurrency = AssetCode.EUR, currency = AssetCode.USD, rate = 1.2),
            exchangeRate(baseCurrency = AssetCode.EUR, currency = AssetCode.GBP, rate = 0.8),
        ),
    )
    private val useCase = SyncExchangeRatesUseCase(store)

    @Test
    fun syncsExchangeRatesForSelectedBaseCurrency() = runTest {
        // when
        val res = useCase.sync(AssetCode.USD)

        // then
        res.shouldBeRight()
        store.savedRates.shouldNotBeEmpty()
        store.savedRates.map { it.currency } shouldContain AssetCode.EUR
        store.savedRates.map { it.currency } shouldContain AssetCode.GBP
        store.savedRates.first { it.currency == AssetCode.EUR }.rate.value shouldBeGreaterThan 0.0
    }

    @Test
    fun keepsManuallyOverriddenRatesUntouched() = runTest {
        // given
        val manualRate = exchangeRate(
            baseCurrency = AssetCode.USD,
            currency = AssetCode.GBP,
            rate = 2.0,
            manualOverride = true,
        )
        store.manuallyOverriddenRates = listOf(manualRate)

        // when
        val res = useCase.sync(AssetCode.USD)

        // then
        res.shouldBeRight()
        store.savedRates.any { it.currency == AssetCode.GBP } shouldBe false
    }

    private fun exchangeRate(
        baseCurrency: AssetCode,
        currency: AssetCode,
        rate: Double,
        manualOverride: Boolean = false,
    ): ExchangeRate = ExchangeRate(
        baseCurrency = baseCurrency,
        currency = currency,
        rate = PositiveDouble.unsafe(rate),
        manualOverride = manualOverride,
    )

    private class FakeExchangeRateStore(
        private val eurRates: List<ExchangeRate>,
    ) : ExchangeRateStore {
        private val ratesFlow = MutableStateFlow<List<ExchangeRate>>(emptyList())

        var manuallyOverriddenRates: List<ExchangeRate> = emptyList()
        val savedRates: List<ExchangeRate>
            get() = ratesFlow.value

        override suspend fun fetchEurExchangeRates(): Either<String, List<ExchangeRate>> = eurRates.right()

        override fun findAll(): Flow<List<ExchangeRate>> = ratesFlow

        override suspend fun findAllManuallyOverridden(): List<ExchangeRate> = manuallyOverriddenRates

        override suspend fun findByBaseCurrencyAndCurrency(
            baseCurrency: AssetCode,
            currency: AssetCode,
        ): ExchangeRate? {
            return ratesFlow.value.firstOrNull {
                it.baseCurrency == baseCurrency && it.currency == currency
            }
        }

        override suspend fun save(value: ExchangeRate) {
            ratesFlow.value = ratesFlow.value
                .filterNot {
                    it.baseCurrency == value.baseCurrency && it.currency == value.currency
                } + value
        }

        override suspend fun saveManyRates(values: List<ExchangeRate>) {
            ratesFlow.value = values
        }

        override suspend fun deleteAll() {
            ratesFlow.value = emptyList()
        }

        override suspend fun deleteByBaseCurrencyAndCurrency(
            baseCurrency: AssetCode,
            currency: AssetCode,
        ) {
            ratesFlow.value = ratesFlow.value.filterNot {
                it.baseCurrency == baseCurrency && it.currency == currency
            }
        }
    }
}
