package com.ivy.legacy.domain.action.transaction

import com.ivy.base.model.legacy.TransactionHistoryItem
import com.ivy.domain.usecase.transaction.GetTransactionsBetweenUseCase
import com.ivy.legacy.frp.action.FPAction
import com.ivy.data.model.legacy.ClosedTimeRange
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import javax.inject.Inject

class HistoryWithDateDivsAct @Inject constructor(
    private val getTransactionsBetweenUseCase: GetTransactionsBetweenUseCase,
    private val trnsWithDateDivsAct: TrnsWithDateDivsAct
) : FPAction<HistoryWithDateDivsAct.Input, ImmutableList<TransactionHistoryItem>>() {

    override suspend fun Input.compose(): suspend () -> ImmutableList<TransactionHistoryItem> = suspend {
        trnsWithDateDivsAct(
            TrnsWithDateDivsAct.Input(
                baseCurrency = baseCurrency,
                transactions = getTransactionsBetweenUseCase(range)
            )
        ).toImmutableList()
    }

    data class Input(
        val range: ClosedTimeRange,
        val baseCurrency: String
    )
}
