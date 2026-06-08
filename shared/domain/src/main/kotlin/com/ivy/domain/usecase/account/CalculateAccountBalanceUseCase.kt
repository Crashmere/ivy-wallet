package com.ivy.domain.usecase.account

import arrow.core.nonEmptyListOf
import com.ivy.data.model.Account
import com.ivy.data.model.ClosedTimeRange
import com.ivy.domain.transaction.AccountValueFunctions
import com.ivy.domain.transaction.foldTransactions
import com.ivy.domain.time.nowUtc
import java.math.BigDecimal
import javax.inject.Inject

class CalculateAccountBalanceUseCase @Inject internal constructor(
    private val getAccountTransactionsUseCase: GetAccountTransactionsUseCase,
) {
    suspend operator fun invoke(
        account: Account,
        range: ClosedTimeRange? = null
    ): BigDecimal {
        val transactions = getAccountTransactionsUseCase(
            accountId = account.id,
            range = range ?: ClosedTimeRange.allTimeIvy(nowUtc())
        )
        return foldTransactions(
            transactions = transactions,
            arg = account.id.value,
            valueFunctions = nonEmptyListOf(AccountValueFunctions::balance)
        ).head
    }
}
