package com.ivy.domain.usecase.home

import com.ivy.data.model.legacy.ClosedTimeRange
import com.ivy.domain.time.DOMAIN_INSTANT_MIN_SAFE
import com.ivy.domain.transaction.isOverdue
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
                from = DOMAIN_INSTANT_MIN_SAFE,
                to = toRange
            ),
            baseCurrency = baseCurrency,
            dueFilter = ::isOverdue
        )
    }
}
