package com.ivy.domain.usecase.transaction

import com.ivy.data.model.legacy.TransactionHistoryItem
import com.ivy.data.model.legacy.ClosedTimeRange
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import javax.inject.Inject

class GetTransactionHistoryItemsUseCase @Inject constructor(
    private val getTransactionsBetweenUseCase: GetTransactionsBetweenUseCase,
    private val buildTransactionHistoryItemsUseCase: BuildTransactionHistoryItemsUseCase,
) {
    suspend operator fun invoke(
        range: ClosedTimeRange,
        baseCurrency: String,
    ): ImmutableList<TransactionHistoryItem> {
        return buildTransactionHistoryItemsUseCase(
            baseCurrency = baseCurrency,
            transactions = getTransactionsBetweenUseCase(range)
        ).toImmutableList()
    }
}
