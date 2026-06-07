package com.ivy.domain.usecase.loan

import com.ivy.data.api.LoanRecordStore
import com.ivy.data.model.legacy.LoanRecord
import javax.inject.Inject

class DeleteLoanRecordUseCase @Inject constructor(
    private val loanRecordStore: LoanRecordStore,
) {
    suspend operator fun invoke(loanRecord: LoanRecord): Boolean {
        return try {
            loanRecordStore.deleteById(loanRecord.id)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
