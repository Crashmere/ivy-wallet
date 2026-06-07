package com.ivy.domain.usecase.account

import arrow.core.nonEmptyListOf
import com.ivy.base.time.TimeProvider
import com.ivy.data.model.Account
import com.ivy.data.model.legacy.ClosedTimeRange
import com.ivy.domain.transaction.AccountValueFunctions
import com.ivy.domain.transaction.foldTransactions
import java.math.BigDecimal
import javax.inject.Inject

class CalculateAccountBalanceUseCase @Inject constructor(
    private val getAccountTransactionsUseCase: GetAccountTransactionsUseCase,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke(
        account: Account,
        range: ClosedTimeRange? = null
    ): BigDecimal {
        val transactions = getAccountTransactionsUseCase(
            accountId = account.id,
            range = range ?: ClosedTimeRange.allTimeIvy(timeProvider.utcNow())
        )
        return foldTransactions(
            transactions = transactions,
            arg = account.id.value,
            valueFunctions = nonEmptyListOf(AccountValueFunctions::balance)
        ).head
    }
}
