package com.ivy.domain.usecase.account

import com.ivy.data.api.TransactionStore
import com.ivy.data.model.Account
import com.ivy.data.model.Expense
import com.ivy.data.model.Income
import com.ivy.data.model.PositiveValue
import com.ivy.data.model.Transaction
import com.ivy.data.model.TransactionId
import com.ivy.data.model.TransactionMetadata
import com.ivy.data.model.TransactionType
import com.ivy.data.model.primitive.NotBlankTrimmedString
import com.ivy.data.model.primitive.PositiveDouble
import com.ivy.domain.time.nowUtc
import java.math.BigDecimal
import java.util.UUID
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.absoluteValue

internal class AdjustAccountBalanceUseCase @Inject internal constructor(
    private val transactionStore: TransactionStore,
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
        adjustmentTransaction(
            account = account,
            type = type,
            amount = amount,
            title = title
        )?.let {
            transactionStore.save(it)
        }
    }

    private fun adjustmentTransaction(
        account: Account,
        type: TransactionType,
        amount: BigDecimal,
        title: String,
    ): Transaction? {
        val positiveAmount = PositiveDouble.from(amount.toDouble()).getOrNull() ?: return null
        val value = PositiveValue(
            amount = positiveAmount,
            asset = account.asset
        )
        val transactionTitle = NotBlankTrimmedString.from(title).getOrNull()
        val metadata = TransactionMetadata(
            recurringRuleId = null,
            paidForDateTime = null,
            loanId = null,
            loanRecordId = null
        )
        val transactionId = TransactionId(UUID.randomUUID())

        return when (type) {
            TransactionType.INCOME -> Income(
                id = transactionId,
                title = transactionTitle,
                description = null,
                category = null,
                time = nowUtc(),
                settled = true,
                metadata = metadata,
                tags = emptyList(),
                value = value,
                account = account.id,
            )

            TransactionType.EXPENSE -> Expense(
                id = transactionId,
                title = transactionTitle,
                description = null,
                category = null,
                time = nowUtc(),
                settled = true,
                metadata = metadata,
                tags = emptyList(),
                value = value,
                account = account.id,
            )

            TransactionType.TRANSFER -> null
        }
    }
}
