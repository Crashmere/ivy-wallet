package com.ivy.domain.transaction.legacy
import com.ivy.data.model.legacy.LegacyTransaction

import com.ivy.data.model.TransactionType
import com.ivy.data.model.legacy.LegacyAccount
import com.ivy.domain.exchange.ExchangeEffect
import java.math.BigDecimal

object LegacyWalletValueFunctions {
    data class Argument(
        val accounts: List<LegacyAccount>,
        val baseCurrency: String,
        val exchange: ExchangeEffect
    )

    suspend fun income(
        transaction: LegacyTransaction,
        arg: Argument
    ): BigDecimal = with(transaction) {
        when (type) {
            TransactionType.INCOME -> LegacyExchangeTransactions.exchangeInBaseCurrency(
                transaction = this,
                accounts = arg.accounts,
                baseCurrency = arg.baseCurrency,
                exchange = arg.exchange
            )

            else -> BigDecimal.ZERO
        }
    }

    suspend fun transferIncome(
        transaction: LegacyTransaction,
        arg: Argument
    ): BigDecimal = with(transaction) {
        val condition = arg.accounts.any { it.id == this.toAccountId }
        when {
            type == TransactionType.TRANSFER && condition ->
                LegacyExchangeTransactions.exchangeInBaseCurrency(
                    transaction = this.copy(
                        amount = this.toAmount,
                        accountId = this.toAccountId ?: this.accountId
                    ), // Do not remove copy()
                    accounts = arg.accounts,
                    baseCurrency = arg.baseCurrency,
                    exchange = arg.exchange
                )

            else -> BigDecimal.ZERO
        }
    }

    suspend fun expense(
        transaction: LegacyTransaction,
        arg: Argument
    ): BigDecimal = with(transaction) {
        when (type) {
            TransactionType.EXPENSE -> LegacyExchangeTransactions.exchangeInBaseCurrency(
                transaction = this,
                accounts = arg.accounts,
                baseCurrency = arg.baseCurrency,
                exchange = arg.exchange
            )

            else -> BigDecimal.ZERO
        }
    }

    suspend fun transferExpenses(
        transaction: LegacyTransaction,
        arg: Argument
    ): BigDecimal = with(transaction) {
        val condition = arg.accounts.any { it.id == this.accountId }
        when {
            type == TransactionType.TRANSFER && condition -> LegacyExchangeTransactions.exchangeInBaseCurrency(
                transaction = this,
                accounts = arg.accounts,
                baseCurrency = arg.baseCurrency,
                exchange = arg.exchange
            )

            else -> BigDecimal.ZERO
        }
    }
}
