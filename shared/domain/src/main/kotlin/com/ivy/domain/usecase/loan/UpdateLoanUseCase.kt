package com.ivy.domain.usecase.loan

import com.ivy.data.api.LoanStore
import com.ivy.data.model.Loan
import javax.inject.Inject

class UpdateLoanUseCase @Inject constructor(
    private val loanStore: LoanStore,
) {
    suspend operator fun invoke(loan: Loan): Boolean {
        if (loan.name.isBlank()) return false
        if (loan.amount <= 0.0) return false

        return try {
            loanStore.save(loan)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
