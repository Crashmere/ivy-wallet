package com.ivy.domain.usecase.home

import com.ivy.base.time.ivyMinTime
import com.ivy.data.model.legacy.ClosedTimeRange
import com.ivy.domain.transaction.legacy.isOverdue
import java.time.Instant
import javax.inject.Inject

class GetOverdueTransactionsInfoUseCase @Inject constructor(
    private val calculateDueTransactionsInfoUseCase: CalculateDueTransactionsInfoUseCase
) {
    suspend operator fun invoke(
        toRange: Instant,
        baseCurrency: String
    ): DueTransactionsInfo {
        return calculateDueTransactionsInfoUseCase(
            range = ClosedTimeRange(
                from = ivyMinTime(),
                to = toRange
            ),
            baseCurrency = baseCurrency,
            dueFilter = ::isOverdue
        )
    }
}
