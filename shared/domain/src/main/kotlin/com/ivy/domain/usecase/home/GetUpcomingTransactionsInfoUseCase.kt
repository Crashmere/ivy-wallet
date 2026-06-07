package com.ivy.domain.usecase.home

import com.ivy.data.model.legacy.ClosedTimeRange
import com.ivy.domain.transaction.isUpcoming
import javax.inject.Inject

class GetUpcomingTransactionsInfoUseCase @Inject constructor(
    private val calculateDueTransactionsInfoUseCase: CalculateDueTransactionsInfoUseCase
) {
    suspend operator fun invoke(
        range: ClosedTimeRange,
        baseCurrency: String
    ): DueTransactionsInfo {
        return calculateDueTransactionsInfoUseCase(
            range = range,
            baseCurrency = baseCurrency,
            dueFilter = ::isUpcoming
        )
    }
}
