package com.ivy.domain.usecase.loan

import com.ivy.data.api.LoanRecordStore
import com.ivy.data.model.LoanRecord
import javax.inject.Inject

class UpdateLoanRecordUseCase @Inject constructor(
    private val loanRecordStore: LoanRecordStore,
) {
    suspend operator fun invoke(loanRecord: LoanRecord): Boolean {
        if (loanRecord.amount <= 0.0) return false

        return try {
            loanRecordStore.save(loanRecord)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
