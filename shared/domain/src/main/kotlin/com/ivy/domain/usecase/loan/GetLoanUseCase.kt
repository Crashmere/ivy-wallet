package com.ivy.domain.usecase.loan

import com.ivy.data.api.LoanStore
import com.ivy.data.model.Loan
import java.util.UUID
import javax.inject.Inject

class GetLoanUseCase @Inject constructor(
    private val loanStore: LoanStore,
) {
    suspend operator fun invoke(loanId: UUID): Loan? {
        return loanStore.findById(loanId)
    }
}
