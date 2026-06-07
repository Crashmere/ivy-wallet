package com.ivy.domain.usecase.loan

import com.ivy.data.api.LoanStore
import com.ivy.data.model.legacy.Loan
import javax.inject.Inject

class DeleteLoanUseCase @Inject constructor(
    private val loanStore: LoanStore,
) {
    suspend operator fun invoke(loan: Loan): Boolean {
        return try {
            loanStore.deleteById(loan.id)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
