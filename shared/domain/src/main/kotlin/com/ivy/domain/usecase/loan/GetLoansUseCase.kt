package com.ivy.domain.usecase.loan

import com.ivy.data.api.LoanStore
import com.ivy.data.model.Loan
import javax.inject.Inject

class GetLoansUseCase @Inject internal constructor(
    private val loanStore: LoanStore,
) {
    suspend operator fun invoke(): List<Loan> {
        return loanStore.findAll()
    }
}
