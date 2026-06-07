package com.ivy.domain.usecase.exchange

import com.ivy.base.model.legacy.Transaction
import com.ivy.data.api.AccountStore
import com.ivy.data.api.ExchangeRateStore
import com.ivy.data.model.legacy.Account
import com.ivy.data.model.legacy.PlannedPaymentRule
import com.ivy.data.model.primitive.AssetCode
import com.ivy.domain.mapper.legacy.toLegacyDomain
import java.util.UUID
import javax.inject.Inject

class LegacyExchangeRatesUseCase @Inject constructor(
    private val exchangeRateStore: ExchangeRateStore
) {
    suspend fun amountBaseCurrency(
        plannedPayment: PlannedPaymentRule,
        baseCurrency: String,
        accounts: List<Account> // helper
    ): Double {
        return amountBaseCurrency(
            amount = plannedPayment.amount,
            accountId = plannedPayment.accountId,
            baseCurrency = baseCurrency,
            accounts = accounts
        )
    }

    suspend fun amountBaseCurrency(
        transaction: Transaction,
        baseCurrency: String,
        accounts: List<Account> // helper
    ): Double {
        return amountBaseCurrency(
            amount = transaction.amount.toDouble(),
            accountId = transaction.accountId,
            baseCurrency = baseCurrency,
            accounts = accounts
        )
    }

    suspend fun toAmountBaseCurrency(
        transaction: Transaction,
        baseCurrency: String,
        accounts: List<Account> // helper
    ): Double {
        val amount = transaction.toAmount ?: transaction.amount
        val toCurrency = accounts.find { it.id == transaction.toAccountId }?.currency
            ?: return amount.toDouble() // no conversion

        return amountBaseCurrency(
            amount = amount.toDouble(),
            amountCurrency = toCurrency,
            baseCurrency = baseCurrency
        )
    }

    private suspend fun amountBaseCurrency(
        amount: Double,
        accountId: UUID,
        baseCurrency: String,
        accounts: List<Account> // helper
    ): Double {
        val trnCurrency = accounts.find { it.id == accountId }?.currency
            ?: return amount // no conversion

        return amountBaseCurrency(
            amount = amount,
            amountCurrency = trnCurrency,
            baseCurrency = baseCurrency
        )
    }

    suspend fun amountBaseCurrency(
        amount: Double,
        amountCurrency: String,
        baseCurrency: String
    ): Double {
        return if (amountCurrency != baseCurrency) {
            // convert to base currency
            amount / exchangeRate(baseCurrency = baseCurrency, currency = amountCurrency)
        } else {
            // no conversion needed, return amount
            amount
        }
    }

    suspend fun convertAmount(
        baseCurrency: String,
        amount: Double,
        fromCurrency: String,
        toCurrency: String
    ): Double {
        if (fromCurrency == toCurrency) return amount

        val amountBaseCurrency =
            amount / exchangeRate(baseCurrency = baseCurrency, currency = fromCurrency)
        return amountBaseCurrency * exchangeRate(baseCurrency = baseCurrency, currency = toCurrency)
    }

    /**
     * base = BGN, currency = EUR => rate = 0.51
     */
    private suspend fun exchangeRate(
        baseCurrency: String,
        currency: String
    ): Double {
        val base = AssetCode.from(baseCurrency).getOrNull() ?: return 1.0
        val target = AssetCode.from(currency).getOrNull() ?: return 1.0
        val rate = exchangeRateStore.findByBaseCurrencyAndCurrency(
            baseCurrency = base,
            currency = target
        )?.rate?.value ?: return 1.0
        if (rate <= 0) {
            return 1.0
        }
        return rate
    }
}

suspend fun Iterable<Transaction>.sumInBaseCurrency(
    exchangeRatesLogic: LegacyExchangeRatesUseCase,
    baseCurrency: String,
    accountStore: AccountStore,
): Double {
    val accounts = accountStore.findAll().map { it.toLegacyDomain() }

    return sumOf {
        exchangeRatesLogic.amountBaseCurrency(
            transaction = it,
            baseCurrency = baseCurrency,
            accounts = accounts
        )
    }
}

suspend fun Iterable<PlannedPaymentRule>.sumByDoublePlannedInBaseCurrency(
    exchangeRatesLogic: LegacyExchangeRatesUseCase,
    baseCurrency: String,
    accountStore: AccountStore,
): Double {
    val accounts = accountStore.findAll().map { it.toLegacyDomain() }

    return sumOf {
        exchangeRatesLogic.amountBaseCurrency(
            plannedPayment = it,
            baseCurrency = baseCurrency,
            accounts = accounts
        )
    }
}
