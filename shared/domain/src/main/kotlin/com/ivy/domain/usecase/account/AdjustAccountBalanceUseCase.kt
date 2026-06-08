package com.ivy.domain.usecase.account

import com.ivy.data.api.AccountStore
import com.ivy.data.api.TransactionStore
import com.ivy.data.model.Account
import com.ivy.data.model.TransactionType
import com.ivy.data.model.legacy.LegacyTransaction
import com.ivy.domain.mapper.legacy.toTransaction
import com.ivy.domain.time.nowUtc
import java.math.BigDecimal
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.absoluteValue

internal class AdjustAccountBalanceUseCase @Inject internal constructor(
    private val transactionStore: TransactionStore,
    private val accountStore: AccountStore,
    private val calculateAccountBalanceUseCase: CalculateAccountBalanceUseCase,
) {
    suspend operator fun invoke(
        account: Account,
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

    private suspend fun calculateAccountBalance(account: Account): Double {
        return calculateAccountBalanceUseCase(account).toDouble()
    }

    private suspend fun saveAdjustmentTransaction(
        account: Account,
        type: TransactionType,
        amount: BigDecimal,
        title: String
    ) {
        LegacyTransaction(
            type = type,
            title = title,
            amount = amount,
            toAmount = amount,
            dateTime = nowUtc(),
            accountId = account.id.value,
        ).toTransaction(accountStore)?.let {
            transactionStore.save(it)
        }
    }
}
