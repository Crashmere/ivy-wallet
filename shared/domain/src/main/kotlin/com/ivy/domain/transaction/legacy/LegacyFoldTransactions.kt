package com.ivy.domain.transaction.legacy
import com.ivy.data.model.legacy.LegacyTransaction

import arrow.core.NonEmptyList
import arrow.core.nonEmptyListOf
import com.ivy.domain.util.mapIndexedNelSuspend
import com.ivy.domain.util.nonEmptyListOfZeros
import java.math.BigDecimal

internal object LegacyFoldTransactions {
    suspend fun <Arg> foldTransactionsSuspend(
        transactions: List<LegacyTransaction>,
        valueFunctions: NonEmptyList<suspend (LegacyTransaction, Arg) -> BigDecimal>,
        arg: Arg
    ): NonEmptyList<BigDecimal> = sumTransactionsSuspendInternal(
        transactions = transactions,
        valueFunctions = valueFunctions,
        valueFunctionArgument = arg
    )

    internal tailrec suspend fun <A> sumTransactionsSuspendInternal(
        transactions: List<LegacyTransaction>,
        valueFunctionArgument: A,
        valueFunctions: NonEmptyList<suspend (LegacyTransaction, A) -> BigDecimal>,
        sum: NonEmptyList<BigDecimal> = nonEmptyListOfZeros(n = valueFunctions.size)
    ): NonEmptyList<BigDecimal> {
        return if (transactions.isEmpty()) {
            sum
        } else {
            sumTransactionsSuspendInternal(
                valueFunctionArgument = valueFunctionArgument,
                transactions = transactions.drop(1),
                valueFunctions = valueFunctions,
                sum = sum.mapIndexedNelSuspend { index, sumValue ->
                    val valueFunction = valueFunctions[index]
                    sumValue + valueFunction(transactions.first(), valueFunctionArgument)
                }
            )
        }
    }

    suspend fun <A> sumTransactions(
        transactions: List<LegacyTransaction>,
        valueFunction: suspend (LegacyTransaction, A) -> BigDecimal,
        argument: A
    ): BigDecimal {
        return sumTransactionsSuspendInternal(
            transactions = transactions,
            valueFunctionArgument = argument,
            valueFunctions = nonEmptyListOf(valueFunction)
        ).head
    }
}
