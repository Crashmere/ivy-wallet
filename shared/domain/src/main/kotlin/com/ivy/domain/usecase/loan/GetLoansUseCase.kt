package com.ivy.domain.usecase.loan

import com.ivy.data.api.LoanStore
import com.ivy.data.model.legacy.Loan
import javax.inject.Inject

class GetLoansUseCase @Inject constructor(
    private val loanStore: LoanStore,
) {
    suspend operator fun invoke(): List<Loan> {
        return loanStore.findAll()
    }
}
