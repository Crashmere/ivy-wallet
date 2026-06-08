package com.ivy.domain.usecase.loan

import com.ivy.data.api.LoanStore
import com.ivy.data.model.Loan
import javax.inject.Inject

class ReorderLoansUseCase @Inject internal constructor(
    private val loanStore: LoanStore,
) {
    suspend operator fun invoke(loans: List<Loan>) {
        loanStore.saveMany(
            loans.mapIndexed { index, loan ->
                loan.copy(orderNum = index.toDouble())
            }
        )
    }
}
