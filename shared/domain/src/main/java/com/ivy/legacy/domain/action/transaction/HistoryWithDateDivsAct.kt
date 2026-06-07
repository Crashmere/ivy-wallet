package com.ivy.legacy.domain.action.transaction

import com.ivy.base.model.legacy.TransactionHistoryItem
import com.ivy.base.frp.action.FPAction
import com.ivy.base.frp.then
import com.ivy.legacy.domain.pure.data.ClosedTimeRange
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import javax.inject.Inject

class HistoryWithDateDivsAct @Inject constructor(
    private val historyTrnsAct: HistoryTrnsAct,
    private val trnsWithDateDivsAct: TrnsWithDateDivsAct
) : FPAction<HistoryWithDateDivsAct.Input, ImmutableList<TransactionHistoryItem>>() {

    override suspend fun Input.compose(): suspend () -> ImmutableList<TransactionHistoryItem> =
        suspend {
            range
        } then historyTrnsAct then { trns ->
            TrnsWithDateDivsAct.Input(
                baseCurrency = baseCurrency,
                transactions = trns
            )
        } then trnsWithDateDivsAct then { it.toImmutableList() }

    data class Input(
        val range: ClosedTimeRange,
        val baseCurrency: String
    )
}
