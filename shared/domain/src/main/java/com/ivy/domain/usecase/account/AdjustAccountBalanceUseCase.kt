package com.ivy.domain.usecase.account

import arrow.core.getOrElse
import com.ivy.base.model.TransactionType
import com.ivy.base.time.TimeProvider
import com.ivy.data.api.AccountStore
import com.ivy.data.api.TransactionStore
import com.ivy.domain.usecase.currency.GetBaseCurrencyUseCase
import com.ivy.domain.mapper.legacy.toDomain
import java.math.BigDecimal
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.absoluteValue
import com.ivy.base.model.legacy.Transaction as LegacyTransaction
import com.ivy.data.model.legacy.Account as LegacyAccount

class AdjustAccountBalanceUseCase @Inject constructor(
    private val transactionRepository: TransactionStore,
    private val accountStore: AccountStore,
    private val calculateAccountBalanceUseCase: CalculateAccountBalanceUseCase,
    private val getBaseCurrency: GetBaseCurrencyUseCase,
    private val timeProvider: TimeProvider
) {
    suspend operator fun invoke(
        account: LegacyAccount,
        actualBalance: Double? = null,
        newBalance: Double,
        adjustTransactionTitle: String = "Adjust balance",
        isFiat: Boolean? = null,
    ) {
        val currentBalance = actualBalance ?: calculateAccountBalance(account)
        val diff = currentBalance - newBalance
        val finalDiff = if (isFiat == true && abs(diff) < 0.009) 0.0 else diff

        when {
            finalDiff < 0 -> saveAdjustmentTransaction(
                account = account,
                type = TransactionType.INCOME,
                amount = diff.absoluteValue.toBigDecimal(),
                title = adjustTransactionTitle
            )

            finalDiff > 0 -> saveAdjustmentTransaction(
                account = account,
                type = TransactionType.EXPENSE,
                amount = diff.absoluteValue.toBigDecimal(),
                title = adjustTransactionTitle
            )
        }
    }

    private suspend fun calculateAccountBalance(account: LegacyAccount): Double {
        val baseCurrency = getBaseCurrency()
        val domainAccount = account.toDomainAccount(baseCurrency)
            .getOrElse { return 0.0 }

        return calculateAccountBalanceUseCase(domainAccount).toDouble()
    }

    private suspend fun saveAdjustmentTransaction(
        account: LegacyAccount,
        type: TransactionType,
        amount: BigDecimal,
        title: String
    ) {
        LegacyTransaction(
            type = type,
            title = title,
            amount = amount,
            toAmount = amount,
            dateTime = timeProvider.utcNow(),
            accountId = account.id,
        ).toDomain(accountStore)?.let {
            transactionRepository.save(it)
        }
    }
}
