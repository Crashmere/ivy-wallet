package com.ivy.domain.exchange

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.math.BigDecimal

class ExchangeTest {

    @Test
    fun `returns amount when currencies match`() = runTest {
        val amount = BigDecimal("10")
        var rateRequested = false

        val result = exchange(
            data = ExchangeData.fromCurrencyCode(
                baseCurrency = "USD",
                fromCurrency = "USD",
                toCurrency = "USD"
            ),
            amount = amount,
            getExchangeRate = { _, _ ->
                rateRequested = true
                null
            }
        )

        result.getOrNull() shouldBe amount
        rateRequested shouldBe false
    }

    @Test
    fun `converts from base currency to target currency`() = runTest {
        val result = exchange(
            data = ExchangeData.fromCurrencyCode(
                baseCurrency = "USD",
                fromCurrency = "USD",
                toCurrency = "EUR"
            ),
            amount = BigDecimal("10"),
            getExchangeRate = { baseCurrency, toCurrency ->
                if (baseCurrency == "USD" && toCurrency == "EUR") {
                    BigDecimal("2")
                } else {
                    null
                }
            }
        )

        result.getOrNull() shouldBe BigDecimal("20")
    }

    @Test
    fun `converts from target currency to base currency`() = runTest {
        val result = exchange(
            data = ExchangeData.fromCurrencyCode(
                baseCurrency = "USD",
                fromCurrency = "EUR",
                toCurrency = "USD"
            ),
            amount = BigDecimal("10"),
            getExchangeRate = { baseCurrency, toCurrency ->
                if (baseCurrency == "USD" && toCurrency == "EUR") {
                    BigDecimal("2")
                } else {
                    null
                }
            }
        )

        result.getOrNull() shouldBe BigDecimal("5")
    }

    @Test
    fun `rejects non-positive exchange rates`() = runTest {
        val result = exchange(
            data = ExchangeData.fromCurrencyCode(
                baseCurrency = "USD",
                fromCurrency = "USD",
                toCurrency = "EUR"
            ),
            amount = BigDecimal("10"),
            getExchangeRate = { _, _ -> BigDecimal.ZERO }
        )

        result.getOrNull() shouldBe null
    }
}
